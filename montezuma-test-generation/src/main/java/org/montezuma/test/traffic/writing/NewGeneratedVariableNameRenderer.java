package org.montezuma.test.traffic.writing;


public class NewGeneratedVariableNameRenderer extends NewVariableNameRenderer {
	private final Class<?>							varClass;
	private final String								namePrefix;
	private String					name;

	public NewGeneratedVariableNameRenderer(int identityHashCode, Class<?> varClass, String namePrefix) {
		super(identityHashCode);
		this.varClass = varClass;
		this.namePrefix = namePrefix;
	}

	@Override
	protected String getName() {
		if (name != null)
			return name;

		final String className = varClass.getSimpleName();
		final String classNameForVarName = (varClass.isArray() ? className.replace("[]", "Array") : className);
		this.name = namePrefix + classNameForVarName + (TestMethodsWriter.globalVariableNumber++);
		return name;
	}

}
