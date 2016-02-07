package org.montezuma.test.traffic.writing;


public class NewGeneratedVariableNameRenderer extends NewVariableNameRenderer {
	protected final ImportsContainer importsContainer;
	private final ObjectDeclarationScope objectDeclarationScope;
	private final String								namePrefix;
	private String					name;

	public NewGeneratedVariableNameRenderer(int identityHashCode, Class<?> varClass, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, String namePrefix) {
		super(identityHashCode, varClass);
		this.importsContainer = importsContainer;
		this.namePrefix = namePrefix;
		this.objectDeclarationScope = objectDeclarationScope;
	}

	@Override
	protected String getName(Class<?> desiredClass) {
		if (name != null)
			return name;

		final String className = varClass.getSimpleName();
		final String classNameForVarName = (varClass.isArray() ? className.replace("[]", "Array") : className);
		this.name = namePrefix + classNameForVarName + (TestMethodsWriter.globalVariableNumber++);
		return name;
	}

	@Override
	public String render() {
		return getName(varClass);
	}

}
