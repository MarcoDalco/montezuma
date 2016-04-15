package org.montezuma.test.traffic.writing;

public class StructuredTextRenderer implements DynamicExpressionRenderer {

	protected final String						formattedText;
	protected ExpressionRenderer[]	masterExpressionRenderers;
	protected ExpressionRenderer[]	expressionRenderers;

	public StructuredTextRenderer(String formattedText, ExpressionRenderer... expressionRenderers) {
		this.formattedText = formattedText;
		this.masterExpressionRenderers = expressionRenderers;
	}

	@Override
	public String render() {
		ExpressionRenderer [] expressionRenderers = (this.expressionRenderers != null ? this.expressionRenderers : masterExpressionRenderers);
		final int numberOfRenderers = expressionRenderers.length;
		String[] renderedArgs = new String[numberOfRenderers];

		for (int i = 0; i < numberOfRenderers; i++) {
			renderedArgs[i] = expressionRenderers[i].render();
		}
		return String.format(formattedText, renderedArgs);
	}

	@Override
	public ExpressionRenderer[] getMasterRenderers() {
		return masterExpressionRenderers;
	}

	@Override
	public void setRenderers(ExpressionRenderer[] expressionRenderers) {
		this.expressionRenderers = expressionRenderers;
	}

}
