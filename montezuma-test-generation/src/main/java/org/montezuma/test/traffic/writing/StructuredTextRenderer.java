package org.montezuma.test.traffic.writing;

import java.util.Arrays;

public class StructuredTextRenderer implements DynamicExpressionRenderer {

	protected String						formattedText;
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

	@Override
	public String toString() {
		return "StructuredTextRenderer [formattedText=" + formattedText + ", masterExpressionRenderers=" + Arrays.toString(masterExpressionRenderers) + ", expressionRenderers="
				+ Arrays.toString(expressionRenderers) + "]";
	}

}
