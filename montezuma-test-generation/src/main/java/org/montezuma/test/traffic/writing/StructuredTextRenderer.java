package org.montezuma.test.traffic.writing;

public class StructuredTextRenderer implements ExpressionRenderer {

	private String								formattedText;
	private ExpressionRenderer[]	expressionRenderers;

	public StructuredTextRenderer(String formattedText, ExpressionRenderer... expressionRenderers) {
		this.formattedText = formattedText;
		this.expressionRenderers = expressionRenderers;
	}

	@Override
	public String render() {
		final int numberOfRenderers = expressionRenderers.length;
		String[] renderedArgs = new String[numberOfRenderers];

		for (int i = 0; i < numberOfRenderers; i++) {
			renderedArgs[i] = expressionRenderers[i].render();
		}
		return String.format(formattedText, renderedArgs);
	}

}
