package org.montezuma.test.traffic.writing;

public class ClassNameRenderer implements ExpressionRenderer {
	private final Class<?>	clazz;
	private final ImportsContainer	importsContainer;

	public ClassNameRenderer(Class<?> clazz, ImportsContainer importsContainer) {
		this.clazz = clazz;
		this.importsContainer = importsContainer;
	}

	@Override
	public String render() {
		final String canonicalName = clazz.getCanonicalName();
		return importsContainer.imports(canonicalName) ? clazz.getSimpleName() : clazz.getCanonicalName();
	}

	Class<?> getRenderedClass() {
		return clazz;
	}
}
