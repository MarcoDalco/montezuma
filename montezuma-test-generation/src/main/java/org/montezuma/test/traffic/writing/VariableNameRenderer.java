package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.Map;

public class VariableNameRenderer implements ExpressionRenderer {
	private static Map<Integer, String>	generatedVariableNames	= new HashMap<>();
	private final int										identityHashCode;
	private final Class<?>							varClass;
	private final String								namePrefix;

	public VariableNameRenderer(int identityHashCode, Class<?> varClass, String namePrefix) {
		this.identityHashCode = identityHashCode;
		this.varClass = varClass;
		this.namePrefix = namePrefix;
	}

	@Override
	public String render() {
		String name = generatedVariableNames.get(identityHashCode);
		if (name != null) {
			return name;
		}

		final String className = varClass.getSimpleName();
		final String classNameForVarName = (varClass.isArray() ? className.replace("[]", "Array") : className);
		name = namePrefix + classNameForVarName + (TestMethodsWriter.globalVariableNumber++);
		generatedVariableNames.put(identityHashCode, name);
		return name;
	}

}
