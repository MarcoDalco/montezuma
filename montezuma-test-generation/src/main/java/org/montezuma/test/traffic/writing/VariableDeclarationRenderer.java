package org.montezuma.test.traffic.writing;

import java.util.Map;

public class VariableDeclarationRenderer extends StructuredTextRenderer {
	private final Class<?> declaredClass;
	private final NewVariableNameRenderer variableNameRenderer;
	Map.Entry<String, String> e;

	public VariableDeclarationRenderer(String formattedText, Class<?> declaredClass, ClassNameRenderer classNameRenderer, NewVariableNameRenderer variableNameRenderer, ExpressionRenderer... renderers) {
		super(formattedText, joinExpressionRenderers(classNameRenderer, variableNameRenderer, renderers));
		this.declaredClass = declaredClass;
		this.variableNameRenderer = variableNameRenderer;
	}

	private static ExpressionRenderer [] joinExpressionRenderers(ClassNameRenderer classNameRenderer, NewVariableNameRenderer variableNameRenderer, ExpressionRenderer... expressionRenderers) {
		ExpressionRenderer [] array = new ExpressionRenderer[expressionRenderers.length + 2];

		array[0] = classNameRenderer;
		array[1] = variableNameRenderer;
		System.arraycopy(expressionRenderers, 0, array, 2, expressionRenderers.length);

		return array;
	}

	Class<?> getDeclaredClass() {
		return declaredClass;
	}

	public NewVariableNameRenderer getVariableNameRenderer() {
		return variableNameRenderer;
	}

	@Override
	public String render() {
		return super.render();
	}

}
