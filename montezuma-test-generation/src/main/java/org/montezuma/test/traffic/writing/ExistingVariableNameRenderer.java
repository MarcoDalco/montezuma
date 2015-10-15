package org.montezuma.test.traffic.writing;

public class ExistingVariableNameRenderer implements ExpressionRenderer {
	public final int										identityHashCode;

	public ExistingVariableNameRenderer(int identityHashCode) {
		this.identityHashCode = identityHashCode;
	}

	@Override
	public String render() {
		return NewVariableNameRenderer.variableNamesRenderers.get(identityHashCode).getName();
	}

}
