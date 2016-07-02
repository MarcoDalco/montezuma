package org.montezuma.test.traffic.writing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariableDeclarationRenderer extends StructuredTextRenderer {
	private final Map<Class<?>, Integer> desiredClasses = new LinkedHashMap<>(); // Desired classes and number of times they are referenced
	private final NewVariableNameRenderer variableNameRenderer;
	private final ImportsContainer importsContainer;
	private boolean inlining;

	public VariableDeclarationRenderer(String formattedText, int identityHashCode, ObjectDeclarationScope objectDeclarationScope, NewVariableNameRenderer variableNameRenderer, Class<?> desiredClass, ImportsContainer importsContainer, ComputableClassNameRendererPlaceholder classNameRenderer, NewVariableNameRendererPlaceholder newVariableNameRendererPlaceholder, ExpressionRenderer valueRenderer) {
		super(formattedText, joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder.instance, NewVariableNameRendererPlaceholder.instance, valueRenderer));

		this.desiredClasses.put(desiredClass, Integer.valueOf(0));
		this.variableNameRenderer = variableNameRenderer;
		this.importsContainer = importsContainer;
	}

	public VariableDeclarationRenderer(String formattedText, int identityHashCode, String namePrefix, Class<?> desiredClass, ImportsContainer importsContainer, ComputableClassNameRendererPlaceholder classNameRenderer, NewVariableNameRendererPlaceholder newVariableNameRendererPlaceholder, ExpressionRenderer valueRenderer) {
		super(formattedText, joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder.instance, NewVariableNameRendererPlaceholder.instance, valueRenderer));

		NewVariableNameRenderer variableNameRenderer = new NewGeneratedVariableNameRenderer(identityHashCode, desiredClass, importsContainer, namePrefix);

		this.desiredClasses.put(desiredClass, Integer.valueOf(0));
		this.variableNameRenderer = variableNameRenderer;
		this.importsContainer = importsContainer;
	}

	public static class NewVariableNameRendererPlaceholder extends NewVariableNameRenderer {
		private NewVariableNameRendererPlaceholder() {
			super(-1, null);
		}

		public final static NewVariableNameRendererPlaceholder instance = new NewVariableNameRendererPlaceholder();

		@Override
		protected String getName(Class<?> desiredClass) {
			throw new UnsupportedOperationException("This should never be invoked");
		}

		@Override
		public String render() {
			throw new UnsupportedOperationException("This should never be invoked");
		}
	}

	public static class ComputableClassNameRendererPlaceholder extends ClassNameRenderer {
		private ComputableClassNameRendererPlaceholder() {
			super(null, null);
		}

		public final static ComputableClassNameRendererPlaceholder instance = new ComputableClassNameRendererPlaceholder();
	}

	class ComputableClassNameRenderer extends ClassNameRenderer {
		private ComputableClassNameRenderer(Class<?> clazz, ImportsContainer importsContainer) {
			super(clazz, importsContainer);
		}

		Class<?> getRenderedClass() {
			return clazz;
		}
	}

	private static ExpressionRenderer [] joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder classNameRendererPlaceholder, NewVariableNameRenderer variableNameRenderer, ExpressionRenderer expressionRenderer) {
		ExpressionRenderer [] array = new ExpressionRenderer[(expressionRenderer == null ? 2 : 3)];

		array[0] = classNameRendererPlaceholder;
		array[1] = variableNameRenderer;
		if (expressionRenderer != null)
			array[2] = expressionRenderer;

		return array;
	}

	public NewVariableNameRenderer getVariableNameRenderer() {
		return variableNameRenderer;
	}

	@Override
	public String render() {
		String rendering = "";
		
		ExpressionRenderer [] expressionRenderersMaster = masterExpressionRenderers;

		boolean first = true;

withTheNextDesiredClass:
		for (Map.Entry<Class<?>, Integer> desiredClassEntry : desiredClasses.entrySet()) {
			if (desiredClassEntry.getValue().intValue() == 1) {
				ExpressionRenderer [] expressionRenderers = (VariableDeclarationRenderer.this.expressionRenderers == null? VariableDeclarationRenderer.this.masterExpressionRenderers : VariableDeclarationRenderer.this.expressionRenderers);
				if (expressionRenderers.length > 2) {
					inlining = true;
					continue withTheNextDesiredClass;
				}
			}

			Class<?> desiredClass = desiredClassEntry.getKey();
			// clone the renderers now, once per desiredClass, before their "render()" method is run, then proceed to render them all.
			expressionRenderers = replaceRenderers(expressionRenderersMaster, desiredClass, variableNameRenderer.getName(desiredClass));

			if (!first)
				rendering += StructuredTextFileWriter.EOL;

			first = false;

			rendering += super.render();
		}

		return rendering;
	}

	private ExpressionRenderer [] replaceRenderers(ExpressionRenderer [] expressionRenderers, Class<?> desiredClass, String variableName) {
		ExpressionRenderer [] newExpressionRenderers = new ExpressionRenderer[expressionRenderers.length];
		for (int i=0; i<expressionRenderers.length; i++) {
			ExpressionRenderer expressionRenderer = expressionRenderers[i];
			if (expressionRenderer == ComputableClassNameRendererPlaceholder.instance)
				expressionRenderer = new ComputableClassNameRenderer(desiredClass, importsContainer) {

				Class<?> getRenderedClass() {
					return desiredClass;
				}

			};
			else
			if (expressionRenderer == NewVariableNameRendererPlaceholder.instance) {
				expressionRenderer = new ExpressionRenderer() {
					
					@Override
					public String render() {
						return variableName;
					}
				};
			} else if (expressionRenderer instanceof DynamicExpressionRenderer) {
				DynamicExpressionRenderer dynamicExpressionRenderer = (DynamicExpressionRenderer) expressionRenderer;
				dynamicExpressionRenderer.setRenderers(replaceRenderers(dynamicExpressionRenderer.getMasterRenderers(), desiredClass /* this should actually come from a placeholder */, variableName));
			}
			
			newExpressionRenderers[i] = expressionRenderer;
		}

		return newExpressionRenderers;
	}

	public boolean declaresClass(Class<?> requiredClass) {
		int value = 1;

		for (Map.Entry<Class<?>, Integer> alreadyDesiredClassEntry : desiredClasses.entrySet()) {
			Class<?> alreadyDesiredClass = alreadyDesiredClassEntry.getKey();
			// An already-desired class is either the same as the requiredClass or is a sub-type (subclass or subinterface), so it already declares the requiredClass: nothing to do.
			if (requiredClass.isAssignableFrom(alreadyDesiredClass)) {
				value = alreadyDesiredClassEntry.getValue().intValue();
				if (value < 2) // No need to update: it doesn't make any difference. We are not keeping statistics; we just want to see if it's used only once, so we can inline it, or more, so we cannot
					desiredClasses.put(alreadyDesiredClass, value + 1);
				return true;
			}
			// A more-specific-than-already-desired class is required, so let's remove it, for adding it later on.
			if (alreadyDesiredClass.isAssignableFrom(requiredClass)) {
				// TODO - WARNING: calling this with an invisible requiredClass changes the alreadyDesiredClass to an invisible subclass. It might happen from JMockitFramework.getStrictExpectationsPart
				value = desiredClasses.remove(alreadyDesiredClass) + 1;
				// Go to replace the requiredClass. There cannot be any more-specialised classes, as they would have all been removed by the "remove" above.
				break;
			}
		}

		desiredClasses.put(requiredClass, value);

		return true;
	}

	public boolean desiresNoMoreThanSuperOrSubClassesOf(Class<?> requiredClass) {
		Map.Entry<Class<?>, Integer> alreadyDesiredClassEntry = desiredClasses.entrySet().iterator().next();

		Class<?> alreadyDesiredClass = alreadyDesiredClassEntry.getKey();
		return ((requiredClass.isAssignableFrom(alreadyDesiredClass)) || (alreadyDesiredClass.isAssignableFrom(requiredClass)));
	}

	class NewGeneratedVariableNameRenderer extends NewVariableNameRenderer {
		protected final ImportsContainer			importsContainer;
		private final String									namePrefix;
		private final Map<Class<?>, String>		names = new HashMap<>();

		public NewGeneratedVariableNameRenderer(int identityHashCode, Class<?> varClass, ImportsContainer importsContainer, String namePrefix) {
			super(identityHashCode, varClass);
			this.importsContainer = importsContainer;
			this.namePrefix = namePrefix;
		}

		@Override
		protected String getName(Class<?> desiredClass) {
			String name = names.get(desiredClass);

			if (name != null)
				return name;

			for (Map.Entry<Class<?>, Integer> declaredClassEntry : VariableDeclarationRenderer.this.desiredClasses.entrySet()) {
				assert(!(VariableDeclarationRenderer.this.inlining && (declaredClassEntry.getValue() > 1)));

				Class<?> declaredClass = declaredClassEntry.getKey();
				if (desiredClass.isAssignableFrom(declaredClass)) {
					name = names.get(declaredClass);

					if (name != null) {
						names.put(desiredClass, name);
						return name;
					}

					if (declaredClassEntry.getValue() == 1) {
						ExpressionRenderer [] expressionRenderers = VariableDeclarationRenderer.this.masterExpressionRenderers;

						if (expressionRenderers.length > 2) {
							expressionRenderers = replaceRenderers(new ExpressionRenderer[] { expressionRenderers[2] }, desiredClass, null);
							return expressionRenderers[0].render();
						}
					}

					String className = declaredClass.getSimpleName();
					char firstChar = className.charAt(0);
					if (firstChar > 'Z')
						className = className.substring(0, 1).toUpperCase() + className.substring(1);
					final String classNameForVarName = (declaredClass.isArray() ? className.replace("[]", "Array") : className);
					name = namePrefix + classNameForVarName + (TestMethodsWriter.globalVariableNumber++);

					names.put(desiredClass, name);
					
					return name;
				}
			}

			throw new IllegalStateException("A suitable declared class should have already been found. Desired Class: " + desiredClass + ", declared classes: " + VariableDeclarationRenderer.this.desiredClasses);
		}

		@Override
		public String render() {
			throw new IllegalStateException("This method should never be invoked");
		}

	}

	public void preprocess() {
		for (Class<?> desiredClass : desiredClasses.keySet()) {
			if (!desiredClass.isPrimitive() && !desiredClass.isArray() && !desiredClass.getPackage().equals(Package.getPackage("java.lang"))) {
				importsContainer.addImport(new Import(desiredClass.getCanonicalName()));
			}
		}
	}

}
