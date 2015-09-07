package org.montezuma.test.traffic.writing;

public class ClassNameRenderer implements ExpressionRenderer {
	private final Class<?>	clazz;

	public ClassNameRenderer(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String render() {
		return clazz.getSimpleName();
	}

}
