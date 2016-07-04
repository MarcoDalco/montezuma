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
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;",
					argID,
					variableNamePrefix,
					argDeclaredClass,
					importsContainer,
					ComputableClassNameRendererPlaceholder.instance,
					VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance,
					new StructuredTextRenderer("\"%s\"",
							new ExpressionRenderer() {
									@Override
									public String render() {
										return ((String) arg).replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\"", "\\\\\"");
									}
							}
					)
			);
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
		} else if (List.class.isAssignableFrom(argActualClass) && argActualClass.getPackage().getName().startsWith("java.util")) {
			@SuppressWarnings("unchecked") final List<Object> rebuiltRuntimeList = (List<Object>) arg;
			final int listSize = rebuiltRuntimeList.size();
			int[] listElementIDs = new int[listSize];
			for (int i=0; i<rebuiltRuntimeList.size(); i++) {
				listElementIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the real object ID?
			}
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s()", actualClassNameRenderer));
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
			buildCollection(this, rebuiltRuntimeList, listElementIDs, new ExistingVariableNameRenderer(argID, argDeclaredClass, importsContainer, this), variableDeclarationRenderer);
		} else if (Set.class.isAssignableFrom(argActualClass)) {
			@SuppressWarnings("unchecked") final Set<Object> rebuiltRuntimeSet = (Set<Object>) arg;
			final int setSize = rebuiltRuntimeSet.size();
			int[] setElementIDs = new int[setSize];
			for (int i=0; i<rebuiltRuntimeSet.size(); i++) {
				setElementIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the real object ID?
			}
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s()", actualClassNameRenderer));
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
//			declaresOrCanSeeIdentityHashCode(i, requiredClass)
			buildCollection(this, rebuiltRuntimeSet, setElementIDs, new ExistingVariableNameRenderer(argID, argDeclaredClass, importsContainer, this), variableDeclarationRenderer);
		} else if (Map.class.isAssignableFrom(argActualClass)) {
			@SuppressWarnings("unchecked") final Map<Object, Object> rebuiltRuntimeMap = (Map<Object, Object>) arg;
			final int mapSize = rebuiltRuntimeMap.size();
			String[] keyTypes = new String[mapSize];
			String[] valueTypes = new String[mapSize];
			int[] mapKeyIDs = new int[mapSize];
			int[] mapValueIDs = new int[mapSize];
			int i = 0;
			for (Map.Entry<Object, Object> entry : rebuiltRuntimeMap.entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				keyTypes[i] = (key == null ? null : key.getClass().getCanonicalName());
				valueTypes[i] = (value == null ? null : value.getClass().getCanonicalName());
				mapKeyIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the real object ID?
				mapValueIDs[i] = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); // TODO: store the real object ID?
				i++;
			}
			final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s()", actualClassNameRenderer));
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
//			declaresOrCanSeeIdentityHashCode(i, requiredClass)
			buildMap(this, rebuiltRuntimeMap, keyTypes, valueTypes, mapKeyIDs, mapValueIDs, new ExistingVariableNameRenderer(argID, argDeclaredClass, importsContainer, this), variableDeclarationRenderer);
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
					"final %s %s = %s;", argID, variableNamePrefix, argDeclaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s {%s}", ComputableClassNameRendererPlaceholder.instance, arrayObjectsRenderer));
			codeRenderers.add(variableDeclarationRenderer);
			addDeclaredObject(argID, variableDeclarationRenderer);
		} else {
			// Using mocks:
			if (mockingStrategy.mustStub(arg) || mockingStrategy.shouldStub(argActualClass)) {
				// TO CHECK - getting the visible superclass MIGHT not be necessary.
				final Class<?> declaredClass = ReflectionUtils.getVisibleSuperClass(argDeclaredClass, testClassWriter.testedClass); // argClass or argActualClass, here?
				MockingFrameworkFactory.getMockingFramework().addStub(false, false, argID, declaredClass, renderersStrategy, importsContainer, testClassWriter, this);
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

	private void buildCollection(InitCodeChunk mainCodeChunk, Collection<Object> rebuiltRuntimeCollection, int[] elementIDs, ExistingVariableNameRenderer collectionNameRenderer, VariableDeclarationRenderer variableDeclarationRenderer) throws ClassNotFoundException {
		int i = 0;
		for (Iterator<?> runtimeObjectsIterator = rebuiltRuntimeCollection.iterator(); runtimeObjectsIterator.hasNext(); i++) {
			variableDeclarationRenderer.declaresClass(collectionNameRenderer.varClass); // To avoid inlining
			Object element = runtimeObjectsIterator.next();

			if (element == null) {
				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(null);", collectionNameRenderer));
			} else {
				final Class<?> elementClass = (element instanceof MustMock ? ((MustMock) element).clazz : element.getClass());
				final int elementID = elementIDs[i];

				writeVariableGenerationIfNecessary(mainCodeChunk, element, elementClass, elementID);
				// TODO - TOCHECK - The following should not be necessary, unless assumed by some other code to be there already, before variableCodeChunk is evaluated
				// maincodeChunk.addDeclaredObject(elementID, variableDeclarationRenderer); // Is it correct, here?

				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(%s);", collectionNameRenderer, new ExistingVariableNameRenderer(elementID, elementClass, importsContainer, mainCodeChunk)));
				mainCodeChunk.declaresOrCanSeeIdentityHashCode(elementID, elementClass); // To avoid inlining
			}
		}
	}

	private void buildMap(InitCodeChunk mainCodeChunk, Map<Object, Object> rebuiltRuntimeCollection, String[] keyTypes, String[] valueTypes, int[] keyIDs, int[] valueIDs, ExistingVariableNameRenderer collectionNameRenderer, VariableDeclarationRenderer variableDeclarationRenderer) throws ClassNotFoundException {
		int i = 0;
		for (Iterator<Map.Entry<Object, Object>> runtimeObjectsIterator = rebuiltRuntimeCollection.entrySet().iterator(); runtimeObjectsIterator.hasNext(); i++) {
			variableDeclarationRenderer.declaresClass(collectionNameRenderer.varClass); // To avoid inlining
			Map.Entry<Object, Object> entry = runtimeObjectsIterator.next();

			if (entry == null) {
				// TO CHECK: will it ever happen?
				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(null);", collectionNameRenderer));
			} else {
				final ExpressionRenderer existingKeyVariableNameRenderer;
				Object key = entry.getKey();
				if (key != null) {
					final Class<?> keyClass = (key instanceof MustMock ? ((MustMock) key).clazz : key.getClass());
					final int keyID = keyIDs[i];
					writeVariableGenerationIfNecessary(mainCodeChunk, key, keyClass, keyID);
					existingKeyVariableNameRenderer = new ExistingVariableNameRenderer(keyID, keyClass, importsContainer, mainCodeChunk);
					mainCodeChunk.declaresOrCanSeeIdentityHashCode(keyID, keyClass); // To avoid inlining
				} else {
					existingKeyVariableNameRenderer = ExpressionRenderer.stringRenderer("null");
				}

				final ExpressionRenderer existingValueVariableNameRenderer;
				Object value = entry.getValue();
				if (value != null) {
					final Class<?> valueClass = (value instanceof MustMock ? ((MustMock) value).clazz : value.getClass());
					final int valueID = valueIDs[i];
					writeVariableGenerationIfNecessary(mainCodeChunk, value, valueClass, valueID);
					existingValueVariableNameRenderer = new ExistingVariableNameRenderer(valueID, valueClass, importsContainer, mainCodeChunk);
					mainCodeChunk.declaresOrCanSeeIdentityHashCode(valueID, valueClass); // To avoid inlining
				} else {
					existingValueVariableNameRenderer = ExpressionRenderer.stringRenderer("null");
				}

				// TODO - TOCHECK - The following should not be necessary, unless assumed by some other code to be there already, before variableCodeChunk is evaluated
				// maincodeChunk.addDeclaredObject(keyID, variableDeclarationRenderer); // Is it correct, here?
				// maincodeChunk.addDeclaredObject(valueID, variableDeclarationRenderer); // Is it correct, here?

				mainCodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.put(%s, %s);", collectionNameRenderer, existingKeyVariableNameRenderer, existingValueVariableNameRenderer));
			}
		}
	}

	private void writeVariableGenerationIfNecessary(InitCodeChunk mainCodeChunk, Object entry, final Class<?> valueClass, final int valueID) throws ClassNotFoundException {
		// Here I reuse a previous initialisation, to avoid replacing the existing one, which needs to be "preprocessed" for other objects to use it. NOT IDEAL or is it correct? I'm now thinking the latter.
		InitCodeChunk variableCodeChunk = mainCodeChunk.requiredInits.get(valueID);
		if ((variableCodeChunk == null) || !(MockingFrameworkFactory.getMockingFramework().canStubMultipleTypeWithOneStub() || ((variableCodeChunk instanceof StandardInitCodeChunk) && (argDeclaredClass.isAssignableFrom(((StandardInitCodeChunk) variableCodeChunk).argDeclaredClass))))) {
			variableCodeChunk = createInitCodeChunk(entry, valueClass, valueID, "given", mainCodeChunk);
			mainCodeChunk.requiredInits.put(valueID, variableCodeChunk);
			variableCodeChunk.generateRequiredInits();
		}
	}

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix, ObjectDeclarationScope parentObjectDeclarationScope) {
		return new StandardInitCodeChunk(argID, arg, argClass, argID, variableNamePrefix, importsContainer, mockingStrategy, renderersStrategy, testClassWriter, parentObjectDeclarationScope);
	}
}