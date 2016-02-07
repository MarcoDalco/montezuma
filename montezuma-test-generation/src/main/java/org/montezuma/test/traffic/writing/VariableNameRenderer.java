package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.Map;

public abstract class VariableNameRenderer implements ExpressionRenderer {
	static Map<Integer, NewVariableNameRenderer>	variableNameRenderers	= new HashMap<>();
	protected final int														identityHashCode;

	public VariableNameRenderer(int identityHashCode) {
		this.identityHashCode = identityHashCode;
	}

	protected abstract String getName(Class<?> desiredClass);
}
