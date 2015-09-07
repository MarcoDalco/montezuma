package org.montezuma.test.traffic.writing;

public class StructuredTextRenderer implements ExpressionRenderer {

	private String								formattedText;
	private ExpressionRenderer[]	expressionRenderers;

	public StructuredTextRenderer(String formattedText, ExpressionRenderer... classNameRenderers) {
		this.formattedText = formattedText;
		this.expressionRenderers = classNameRenderers;
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
