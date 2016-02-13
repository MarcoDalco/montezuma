package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class VariableDeclarationRenderer extends StructuredTextRenderer {
	private final Set<Class<?>> desiredClasses = new LinkedHashSet<>();
	private final NewVariableNameRenderer variableNameRenderer;
	private final ImportsContainer importsContainer;

	public VariableDeclarationRenderer(String formattedText, int identityHashCode, ObjectDeclarationScope objectDeclarationScope, NewVariableNameRenderer variableNameRenderer, Class<?> desiredClass, ImportsContainer importsContainer, ComputableClassNameRendererPlaceholder classNameRenderer, NewVariableNameRendererPlaceholder newVariableNameRendererPlaceholder, ExpressionRenderer... renderers) {
		super(formattedText, joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder.instance, NewVariableNameRendererPlaceholder.instance, renderers));

		this.desiredClasses.add(desiredClass);
		this.variableNameRenderer = variableNameRenderer;
		this.importsContainer = importsContainer;
	}

	public VariableDeclarationRenderer(String formattedText, int identityHashCode, String namePrefix, Class<?> desiredClass, ImportsContainer importsContainer, ComputableClassNameRendererPlaceholder classNameRenderer, NewVariableNameRendererPlaceholder newVariableNameRendererPlaceholder, ExpressionRenderer... renderers) {
		super(formattedText, joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder.instance, NewVariableNameRendererPlaceholder.instance, renderers));

		NewVariableNameRenderer variableNameRenderer = new NewGeneratedVariableNameRenderer(identityHashCode, desiredClass, importsContainer, namePrefix);

		this.desiredClasses.add(desiredClass);
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
			return null;
		}

		@Override
		public String render() {
			return null;
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

	private static ExpressionRenderer [] joinAndInitialiseExpressionRenderers(ComputableClassNameRendererPlaceholder classNameRendererPlaceholder, NewVariableNameRenderer variableNameRenderer, ExpressionRenderer... expressionRenderers) {
		ExpressionRenderer [] array = new ExpressionRenderer[expressionRenderers.length + 2];

		array[0] = classNameRendererPlaceholder;
		array[1] = variableNameRenderer;
		System.arraycopy(expressionRenderers, 0, array, 2, expressionRenderers.length);

		return array;
	}

	public NewVariableNameRenderer getVariableNameRenderer() {
		return variableNameRenderer;
	}

	@Override
	public String render() {
		String rendering = "";
		ExpressionRenderer [] expressionRenderersMaster = expressionRenderers;
		expressionRenderers = new ExpressionRenderer[expressionRenderersMaster.length];
		
		boolean first = true;
		for (Class<?> desiredClass : desiredClasses) {
			// clone the renderers now, once per desiredClass, before their "render()" method is run, then proceed to render them all.
			for (int i=0; i<expressionRenderersMaster.length; i++) {
				ExpressionRenderer expressionRenderer = expressionRenderersMaster[i];
				if (expressionRenderer == ComputableClassNameRendererPlaceholder.instance)
					expressionRenderer = new ComputableClassNameRenderer(desiredClass, importsContainer) {

					Class<?> getRenderedClass() {
						return desiredClass;
					}

				};
				else
				if (expressionRenderer == NewVariableNameRendererPlaceholder.instance)
					expressionRenderer = new ExpressionRenderer() {
						
						@Override
						public String render() {
							return variableNameRenderer.getName(desiredClass);
						}
					};
				
				expressionRenderers[i] = expressionRenderer;
			}

			if (!first)
				rendering += StructuredTextFileWriter.EOL;

			first = false;

			rendering += super.render();
		}

		return rendering;
	}

	public boolean declaresClass(Class<?> requiredClass) {
		for (Class<?> alreadyDesiredClass : desiredClasses) {
			// An already-desired class is either the same as the requiredClass or is a sub-type (subclass or subinterface), so it already declares the requiredClass: nothing to do.
			if (requiredClass.isAssignableFrom(alreadyDesiredClass))
				return true;
			if (alreadyDesiredClass.isAssignableFrom(requiredClass)) {
				desiredClasses.remove(alreadyDesiredClass);
				// Go to add the requiredClass
				break;
			}
		}

		desiredClasses.add(requiredClass);

		return true;
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

			for (Class<?> declaredClass : VariableDeclarationRenderer.this.desiredClasses) {
				if (desiredClass.isAssignableFrom(declaredClass)) {
					name = names.get(declaredClass);

					if (name != null) {
						names.put(desiredClass, name);
						return name;
					}

					final String className = declaredClass.getSimpleName();
					final String classNameForVarName = (declaredClass.isArray() ? className.replace("[]", "Array") : className);
					name = namePrefix + classNameForVarName + (TestMethodsWriter.globalVariableNumber++);

					names.put(desiredClass, name);
					
					return name;
				}
			}

			throw new IllegalStateException("A suitable declared class should have already been found.");
		}

		@Override
		public String render() {
			return getName(varClass);
		}

	}

	public void preprocess() {
		for (Class<?> desiredClass : desiredClasses) {
			if (!desiredClass.isPrimitive() && !desiredClass.isArray() && !desiredClass.getPackage().equals(Package.getPackage("java.lang"))) {
				importsContainer.addImport(new Import(desiredClass.getCanonicalName()));
			}
		}
	}

}
