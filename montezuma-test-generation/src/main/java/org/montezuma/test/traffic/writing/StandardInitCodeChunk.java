package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer.ComputableClassNameRendererPlaceholder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StandardInitCodeChunk extends InitCodeChunk {
	private final Object		arg;
	final Class<?>	argDeclaredClass;
	private final int				argID;
	private final String		variableNamePrefix;
	private final ImportsContainer	importsContainer;
	private Deserialiser						deserialiser	= SerialisationFactory.getDeserialiser();
	private final MockingStrategy		mockingStrategy;
	private final RenderersStrategy	renderersStrategy;
	private final TestClassWriter		testClassWriter;

	StandardInitCodeChunk(int identityHashCode, Object arg, Class<?> argClass, int argID, String variableNamePrefix, ImportsContainer importsContainer, MockingStrategy mockingStrategy, RenderersStrategy renderersStrategy, TestClassWriter testClassWriter, ObjectDeclarationScope parentObjectDeclarationScope) {
		super(identityHashCode, parentObjectDeclarationScope);
		this.arg = arg;
		this.argDeclaredClass = argClass;
		this.argID = argID;
		this.variableNamePrefix = variableNamePrefix;
		this.importsContainer = importsContainer;
		this.mockingStrategy = mockingStrategy;
		this.renderersStrategy = renderersStrategy;
		this.testClassWriter = testClassWriter;
	}

	@Override
	public void generateRequiredInits() throws ClassNotFoundException {
		final Class<?> argClass = arg.getClass();
		final Class<?> argActualClass = MustMock.class.isAssignableFrom(argClass) ? ((MustMock) arg).clazz : argClass;
		// final NewGeneratedVariableNameRenderer variableNameRenderer;
		// TODO - exclude the mocking case too, as it already adds the class to the imports
		if (!argDeclaredClass.isPrimitive() && !argDeclaredClass.isArray() && !argDeclaredClass.getPackage().equals(Package.getPackage("java.lang"))) {
			requiredImports.addImport(new Import(argDeclaredClass.getCanonicalName()));
		}
		// maincodeChunk.requiredInits.add(variableCodeChunk);
		if ((arg instanceof Number) || (arg instanceof Boolean)) {
			if (argActualClass.equals(BigDecimal.class)) {
				VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new ExpressionRenderer() {
					@Override
					public String render() {
						return getBigDecimalInitialiser(arg);
					}
				});
				codeRenderers.add(variableDeclarationRenderer);
				addDeclaredObject(argID, variableDeclarationRenderer);
			} else {
				final ExpressionRenderer initExpressionRenderer;
				if (argActualClass.equals(int.class) || argActualClass.equals(Integer.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return "" + arg;
						}
					};
				} else if (argActualClass.equals(long.class) || argActualClass.equals(Long.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return arg + "L";
						}
					};
				} else if (argActualClass.equals(double.class) || argActualClass.equals(Double.class)) {
					initExpressionRenderer = new ExpressionRenderer() {
						@Override
						public String render() {
							return arg + "D";
						}
					};
				} else if (argActualClass.equals(boolean.class) || argActualClass.equals(Boolean.class)) {
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
							return arg + "TODO - arg actual class: " + argActualClass + ", arg declared class: " + argDeclaredClass;
						}
					};
				}
				// TODO - TOCHECK - check how the class name is now rendered in case its package is java*
				// final String actualArgType = argClass.getCanonicalName();
				// final String declaredArgClassName = (actualArgType.startsWith("java") ? argClass.getSimpleName() : actualArgType);
				VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;",
						argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, initExpressionRenderer);
				codeRenderers.add(variableDeclarationRenderer);
				addDeclaredObject(argID, variableDeclarationRenderer);
			}
		} else if (argActualClass == String.class) {
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = \"%s\";",
					argID,
					variableNamePrefix,
					argDeclaredClass,
					importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new ExpressionRenderer() {
						@Override
						public String render() {
							return ((String) arg).replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\"", "\\\\\"");
						}
					}
			);
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
		} else if (List.class.isAssignableFrom(argActualClass) && argActualClass.getPackage().getName().startsWith("java.util")) {
			@SuppressWarnings("unchecked") final List<Object> rebuiltRuntimeList = (List<Object>) arg;
			final int listSize = rebuiltRuntimeList.size();
			String[] listElementTypes = new String[listSize];
			int[] listElementIDs = new int[listSize];
			int i = 0;
			for (Object element : rebuiltRuntimeList) {
				listElementTypes[i] = element.getClass().getCanonicalName();
				listElementIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the
																																																	// real object ID?
				i++;
			}
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = new %s();", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, actualClassNameRenderer);
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
			buildCollection(this, rebuiltRuntimeList, listElementTypes, listElementIDs, new ExistingVariableNameRenderer(argID, argDeclaredClass, importsContainer, this));
		} else if (Set.class.isAssignableFrom(argActualClass)) {
			@SuppressWarnings("unchecked") final Set<Object> rebuiltRuntimeSet = (Set<Object>) arg;
			final int setSize = rebuiltRuntimeSet.size();
			String[] setElementTypes = new String[setSize];
			int[] setElementIDs = new int[setSize];
			int i = 0;
			for (Object element : rebuiltRuntimeSet) {
				setElementTypes[i] = element.getClass().getCanonicalName();
				setElementIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the
																																																	// real object ID?
				i++;
			}
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = new %s();", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, actualClassNameRenderer);
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
//			declaresOrCanSeeIdentityHashCode(i, requiredClass)
			buildCollection(this, rebuiltRuntimeSet, setElementTypes, setElementIDs, new ExistingVariableNameRenderer(argID, argDeclaredClass, importsContainer, this));
		} else if (argActualClass.isAssignableFrom(Map.class)) {
			// Not implemented yet
		} else if (argActualClass.isArray()) {
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
			final Class<?> arrayBaseType = argDeclaredClass.getComponentType();
			String[] arrayArgTypes = new String[rebuiltRuntimeArray.length];
			int[] arrayArgIDs = new int[rebuiltRuntimeArray.length];
			final String arrayBaseTypeCanonicalName = arrayBaseType.getCanonicalName();
			for (int l = 0; l < rebuiltRuntimeArray.length; l++) {
				arrayArgTypes[l] = arrayBaseTypeCanonicalName;
				arrayArgIDs[l] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the real
																																																// object ID?
			}
			StructuredTextRenderer arrayObjectsRenderer =
					renderersStrategy.buildInvocationParameters(this, rebuiltRuntimeArray, arrayArgTypes, arrayArgIDs, importsContainer, mockingStrategy, testClassWriter);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer(
					"final %s %s = new %s {%s};", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, ComputableClassNameRendererPlaceholder.instance, arrayObjectsRenderer);
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
		} else {
			// Using mocks:
			if (mockingStrategy.mustStub(arg) || mockingStrategy.shouldStub(argActualClass)) {
				// TO CHECK - getting the visible superclass MIGHT not be necessary.
				final Class<?> declaredClass = ReflectionUtils.getVisibleSuperClass(argDeclaredClass, testClassWriter.testClass); // argClass or argActualClass, here?
				MockingFrameworkFactory.getMockingFramework().addStub(false, argID, declaredClass, renderersStrategy, importsContainer, testClassWriter);
			} else {
				codeRenderers.add(renderersStrategy.addRealParameter(this, argDeclaredClass, arg, argID, importsContainer, testClassWriter.identityHashCodeGenerator));
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

	private void buildCollection(InitCodeChunk mainCodeChunk, Collection<Object> rebuiltRuntimeCollection, String[] elementTypes, int[] elementIDs, ExistingVariableNameRenderer collectionNameRenderer) throws ClassNotFoundException {
		int i = 0;
		for (Iterator<?> runtimeObjectsIterator = rebuiltRuntimeCollection.iterator(); runtimeObjectsIterator.hasNext(); i++) {
			Object element = runtimeObjectsIterator.next();

			if (element == null) {
				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(null);", collectionNameRenderer));
			} else {
				final Class<?> elementClass = (element instanceof MustMock ? ((MustMock) element).clazz : element.getClass());
				final int elementID = elementIDs[i];

				// Here I reuse a previous initialisation, to avoid replacing the existing one, which needs to be "preprocessed" for other objects to use it. NOT IDEAL or is it correct? I'm now thinking the latter.
				InitCodeChunk variableCodeChunk = mainCodeChunk.requiredInits.get(elementID);
				if ((variableCodeChunk == null) || !(MockingFrameworkFactory.getMockingFramework().canStubMultipleTypeWithOneStub() || ((variableCodeChunk instanceof StandardInitCodeChunk) && (argDeclaredClass.isAssignableFrom(((StandardInitCodeChunk) variableCodeChunk).argDeclaredClass))))) {
					variableCodeChunk = createInitCodeChunk(element, elementClass, elementID, "given", mainCodeChunk);
					mainCodeChunk.requiredInits.put(elementID, variableCodeChunk);
					variableCodeChunk.generateRequiredInits();
				}
				// TODO - TOCHECK - The following should not be necessary, unless assumed by some other code to be there already, before variableCodeChunk is evaluated
				// maincodeChunk.addDeclaredObject(elementID, variableDeclarationRenderer); // Is it correct, here?

				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(%s);", collectionNameRenderer, new ExistingVariableNameRenderer(elementID, elementClass, importsContainer, mainCodeChunk)));
			}
		}
	}

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix, ObjectDeclarationScope parentObjectDeclarationScope) {
		return new StandardInitCodeChunk(argID, arg, argClass, argID, variableNamePrefix, importsContainer, mockingStrategy, renderersStrategy, testClassWriter, parentObjectDeclarationScope);
	}
}