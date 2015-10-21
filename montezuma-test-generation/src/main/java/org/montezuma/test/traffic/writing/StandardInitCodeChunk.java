package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StandardInitCodeChunk extends InitCodeChunk {
	private final Object		arg;
	private final Class<?>	argClass;
	private final int				argID;
	private final String		variableNamePrefix;
	private final ImportsContainer	importsContainer;
	private Deserialiser						deserialiser	= SerialisationFactory.getDeserialiser();
	private final MockingStrategy		mockingStrategy;
	private final RenderersStrategy	renderersStrategy;
	private final TestClassWriter		testClassWriter;

	StandardInitCodeChunk(int identityHashCode, Object arg, Class<?> argClass, int argID, String variableNamePrefix, ImportsContainer importsContainer, MockingStrategy mockingStrategy, RenderersStrategy renderersStrategy, TestClassWriter testClassWriter) {
		super(identityHashCode);
		this.arg = arg;
		this.argClass = argClass;
		this.argID = argID;
		this.variableNamePrefix = variableNamePrefix;
		this.importsContainer = importsContainer;
		this.mockingStrategy = mockingStrategy;
		this.renderersStrategy = renderersStrategy;
		this.testClassWriter = testClassWriter;
	}

	@Override
	public void generateRequiredInits() {
		// final NewGeneratedVariableNameRenderer variableNameRenderer;
		// TODO - exclude the mocking case too, as it already adds the class to the imports
		if (!argClass.isPrimitive() && !argClass.isArray() && !argClass.getPackage().equals(Package.getPackage("java.lang"))) {
			requiredImports.addImport(new Import(argClass.getCanonicalName()));
		}
		// maincodeChunk.requiredInits.add(variableCodeChunk);
		if ((arg instanceof Number) || (arg instanceof Boolean)) {
			if (argClass.equals(BigDecimal.class)) {
				codeRenderers.add(new StructuredTextRenderer("final %s %s = %s;", new ClassNameRenderer(argClass, importsContainer), new NewGeneratedVariableNameRenderer(argID, argClass, variableNamePrefix), new ExpressionRenderer() {
					@Override
					public String render() {
						return getBigDecimalInitialiser(arg);
					}
				}));
				addDeclaredIdentityHashCode(argID);
			} else {
				final ExpressionRenderer initExpressionRenderer;
				if (argClass.equals(int.class) || argClass.equals(Integer.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return "" + arg;
						}
					};
				} else if (argClass.equals(long.class) || argClass.equals(Long.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return arg + "L";
						}
					};
				} else if (argClass.equals(double.class) || argClass.equals(Double.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return arg + "D";
						}
					};
				} else if (argClass.equals(boolean.class) || argClass.equals(Boolean.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return "" + arg;
						}
					};
				} else {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return arg + "TODO";
						}
					};
				}
				final String actualArgType = argClass.getCanonicalName();
				final String declaredArgClassName = (actualArgType.startsWith("java") ? argClass.getSimpleName() : actualArgType);
				codeRenderers.add(
						new StructuredTextRenderer("final " + declaredArgClassName + " %s = %s;",
								new NewGeneratedVariableNameRenderer(argID, argClass, variableNamePrefix), initExpressionRenderer)
				);
				addDeclaredIdentityHashCode(argID);
			}
		} else if (argClass == String.class) {
			codeRenderers.add(
					new StructuredTextRenderer("final %s %s = \"%s\";",
							new ClassNameRenderer(argClass, importsContainer),
							new NewGeneratedVariableNameRenderer(argID, argClass, variableNamePrefix),
							new ExpressionRenderer() {
								@Override
								public String render() {
									return ((String) arg).replaceAll("\n", "\\n").replaceAll("\r", "\\r").replaceAll("\\\\", "\\\\\\\\");
								}
							}
					)
			);
			addDeclaredIdentityHashCode(argID);
		} else if (argClass.isAssignableFrom(List.class) && argClass.getPackage().getName().startsWith("java.util")) {
			@SuppressWarnings("unchecked") final List<Object> rebuiltRuntimeList = (List<Object>) arg;
			final int listSize = rebuiltRuntimeList.size();
			String[] listElementTypes = new String[listSize];
			int[] listElementIDs = new int[listSize];
			int i = 0;
			for (Object element : rebuiltRuntimeList) {
				listElementTypes[i] = element.getClass().getCanonicalName();
				listElementIDs[i] = TestMethodsWriter.generateIdentityHashCode(); // TODO: store the real object ID?
				i++;
			}
			final ClassNameRenderer declaredClassNameRenderer = new ClassNameRenderer(argClass, importsContainer);
			final NewGeneratedVariableNameRenderer listNameRenderer = new NewGeneratedVariableNameRenderer(argID, argClass, variableNamePrefix);
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			codeRenderers.add(new StructuredTextRenderer("final %s %s = new %s();", declaredClassNameRenderer, listNameRenderer, actualClassNameRenderer));
			addDeclaredIdentityHashCode(argID);
			buildList(this, rebuiltRuntimeList, listElementTypes, listElementIDs, /* this could be an ExistingVariableNameRenderer */ listNameRenderer);
		} else if (argClass.isAssignableFrom(Set.class)) {} else if (argClass.isAssignableFrom(Map.class)) {} else if (argClass.isArray()) {
			final Object[] serialisedObjectsArray = (Object[]) arg;
			final Object[] rebuiltRuntimeArray = new Object[serialisedObjectsArray.length];
			for (int i = 0; i < rebuiltRuntimeArray.length; i++) {
				try {
					rebuiltRuntimeArray[i] = deserialiser.deserialise((byte[]) serialisedObjectsArray[i]);
				}
				catch (ClassNotFoundException | IOException e) {
					throw new RuntimeException(e);
				}
			}
			final Class<?> arrayBaseType = argClass.getComponentType();
			String[] arrayArgTypes = new String[rebuiltRuntimeArray.length];
			int[] arrayArgIDs = new int[rebuiltRuntimeArray.length];
			final String arrayBaseTypeCanonicalName = arrayBaseType.getCanonicalName();
			for (int l = 0; l < rebuiltRuntimeArray.length; l++) {
				arrayArgTypes[l] = arrayBaseTypeCanonicalName;
				arrayArgIDs[l] = TestMethodsWriter.generateIdentityHashCode(); // TODO: store the real object ID?
			}
			StructuredTextRenderer arrayObjectsRenderer =
					renderersStrategy.buildInvocationParameters(this, rebuiltRuntimeArray, arrayArgTypes, arrayArgIDs, importsContainer, mockingStrategy, testClassWriter);
			final ClassNameRenderer classNameRenderer = new ClassNameRenderer(argClass, importsContainer);
			codeRenderers.add(new StructuredTextRenderer(
					"final %s %s = new %s {%s};", classNameRenderer, new NewGeneratedVariableNameRenderer(argID, argClass, variableNamePrefix), classNameRenderer, arrayObjectsRenderer));
			addDeclaredIdentityHashCode(argID);
		} else {
			// Using mocks:
			if (mockingStrategy.mustMock(arg) || mockingStrategy.shouldMock(argClass)) {
				renderersStrategy.addMock(argID, argClass, renderersStrategy.getMockedFieldNameRenderer(argClass, argID), importsContainer, testClassWriter);
			} else {
				codeRenderers.add(renderersStrategy.addRealParameter(this, argClass, arg, argID, importsContainer));
			}
		}
	}

	private String getBigDecimalInitialiser(Object arg) {
		final String bigIntInit;
		if (BigDecimal.ZERO.equals(arg)) {
			bigIntInit = "BigDecimal.ZERO";
		} else if (BigDecimal.ONE.equals(arg)) {
			bigIntInit = "BigDecimal.ONE";
		} else if (BigDecimal.TEN.equals(arg)) {
			bigIntInit = "BigDecimal.TEN";
		} else {
			// TODO - to be improved, constructing it exactly equivalent to the original
			bigIntInit = "new BigDecimal(\"" + arg + "\")";
		}
		return bigIntInit;
	}

	private void buildList(InitCodeChunk maincodeChunk, List<Object> rebuiltRuntimeList, String[] listElementTypes, int[] listElementIDs, NewGeneratedVariableNameRenderer listNameRenderer) {
		int i = 0;
		for (Iterator<?> runtimeObjectsIterator = rebuiltRuntimeList.iterator(); runtimeObjectsIterator.hasNext(); i++) {
			Object element = runtimeObjectsIterator.next();

			if (element == null) {
				maincodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(null);", listNameRenderer));
			} else {
				final Class<?> elementClass = (element instanceof MustMock ? ((MustMock) element).clazz : element.getClass());
				final int elementID = listElementIDs[i];

				final InitCodeChunk variableCodeChunk = createInitCodeChunk(element, elementClass, elementID, "given");
				maincodeChunk.requiredInits.put(elementID, variableCodeChunk);
				maincodeChunk.addDeclaredIdentityHashCode(elementID);

				maincodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(%s);", listNameRenderer, new ExistingVariableNameRenderer(elementID)));
			}
		}
	}

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix) {
		return new StandardInitCodeChunk(argID, arg, argClass, argID, variableNamePrefix, importsContainer, mockingStrategy, renderersStrategy, testClassWriter);
	}
}