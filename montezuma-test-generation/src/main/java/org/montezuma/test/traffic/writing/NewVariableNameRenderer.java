package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.Map;

public abstract class NewVariableNameRenderer implements ExpressionRenderer {
	static Map<Integer, NewVariableNameRenderer>	variableNamesRenderers	= new HashMap<>();
	protected final int					identityHashCode;

	public NewVariableNameRenderer(int identityHashCode) {
		this.identityHashCode = identityHashCode;
		if (!variableNamesRenderers.containsKey(identityHashCode))
			variableNamesRenderers.put(identityHashCode, this);
	}

	@Override
	public String render() {
		NewVariableNameRenderer renderer = variableNamesRenderers.get(identityHashCode);
		if (renderer != null)
			return renderer.getName();

		return getName();
	}

	protected abstract String getName();
}
