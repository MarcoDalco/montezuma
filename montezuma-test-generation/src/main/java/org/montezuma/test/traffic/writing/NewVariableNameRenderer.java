package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.Map;

public abstract class NewVariableNameRenderer implements ExpressionRenderer {
	static Map<Integer, NewVariableNameRenderer>	variableNamesRenderers	= new HashMap<>();
	protected final int														identityHashCode;
	protected final Class<?>											varClass;
	protected final ImportsContainer importsContainer;
	private final ObjectDeclarationScope objectDeclarationScope;

	public NewVariableNameRenderer(int identityHashCode, Class<?> varClass, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope) {
		this.identityHashCode = identityHashCode;
		if (!variableNamesRenderers.containsKey(identityHashCode))
			variableNamesRenderers.put(identityHashCode, this);
		this.varClass = varClass;
		this.importsContainer = importsContainer;
		this.objectDeclarationScope = objectDeclarationScope;
	}

	@Override
	public String render() {
//		NewVariableNameRenderer renderer = variableNamesRenderers.get(identityHashCode);
		VariableDeclarationRenderer variableDeclarationRenderer = objectDeclarationScope.getVisibleDeclarationRenderer(identityHashCode);
		NewVariableNameRenderer renderer = variableDeclarationRenderer.getVariableNameRenderer();
//		if (renderer != null)
		String name = renderer.getName();
		if (varClass.isAssignableFrom(renderer.varClass)) {
			return name;
		}

		return "((" + new ClassNameRenderer(varClass, importsContainer).render() + ") " + name + ")";
//		return getName();
	}

	protected abstract String getName();
}
