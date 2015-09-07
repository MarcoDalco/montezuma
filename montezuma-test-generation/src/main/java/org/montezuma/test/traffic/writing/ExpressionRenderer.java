package org.montezuma.test.traffic.writing;

public interface ExpressionRenderer {
	public String render();

	public static ExpressionRenderer nullRenderer() {
		return stringRenderer("");
	}

	public static ExpressionRenderer stringRenderer(String string) {
		return new ExpressionRenderer() {

			@Override
			public String render() {
				return string;
			}
		};
	}
}
