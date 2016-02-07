package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.Map;

public abstract class NewVariableNameRenderer extends VariableNameRenderer {
	protected final Class<?>											varClass;

	public NewVariableNameRenderer(int identityHashCode, Class<?> varClass) {
		super(identityHashCode);

		this.varClass = varClass;

		if (!variableNameRenderers.containsKey(identityHashCode))
			variableNameRenderers.put(identityHashCode, this);
	}
}
