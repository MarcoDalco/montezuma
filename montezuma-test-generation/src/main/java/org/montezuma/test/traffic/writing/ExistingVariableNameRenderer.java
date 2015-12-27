package org.montezuma.test.traffic.writing;

public class ExistingVariableNameRenderer extends NewVariableNameRenderer {

	public ExistingVariableNameRenderer(int identityHashCode, Class<?> varClass, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope) {
		super(identityHashCode, varClass, importsContainer, objectDeclarationScope);
	}

	@Override
	protected String getName() {
	return NewVariableNameRenderer.variableNamesRenderers.get(identityHashCode).getName();
	}

//	@Override
//	public String render() {
//		return NewVariableNameRenderer.variableNamesRenderers.get(identityHashCode).getName();
//	}

}
