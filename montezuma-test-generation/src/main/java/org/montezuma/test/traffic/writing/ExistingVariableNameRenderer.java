package org.montezuma.test.traffic.writing;

public class ExistingVariableNameRenderer extends VariableNameRenderer {
	protected final Class<?>											varClass;
	protected final ImportsContainer importsContainer;
	private final ObjectDeclarationScope objectDeclarationScope;

	public ExistingVariableNameRenderer(int identityHashCode, Class<?> varClass, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope) {
		super(identityHashCode);
		this.varClass = varClass;
		this.importsContainer = importsContainer;
		this.objectDeclarationScope = objectDeclarationScope;
	}

	@Override
	protected String getName(Class<?> desiredClass) {
	return NewVariableNameRenderer.variableNameRenderers.get(identityHashCode).getName(desiredClass /* TO CHECK: or varClass? */);
	}

	@Override
	public String render() {
//		NewVariableNameRenderer renderer = variableNameRenderers.get(identityHashCode);
		VariableDeclarationRenderer variableDeclarationRenderer = objectDeclarationScope.getVisibleDeclarationRenderer(identityHashCode, varClass);
		NewVariableNameRenderer renderer = variableDeclarationRenderer.getVariableNameRenderer();
//		if (renderer != null)
		String name = renderer.getName(varClass);
		if ((renderer instanceof NewVariableNameRenderer) && varClass.isAssignableFrom(((NewVariableNameRenderer) renderer).varClass)) {
			return name;
		}

		return "((" + new ClassNameRenderer(varClass, importsContainer).render() + ") " + name + ")";
//		return getName();
	}

	@Override
	public String toString() {
		return "ExistingVariableNameRenderer [varClass=" + varClass + ", importsContainer=" + importsContainer.getClass().getName() + "@" + System.identityHashCode(importsContainer) + ", objectDeclarationScope=" + objectDeclarationScope.getClass().getName() + "@" + System.identityHashCode(objectDeclarationScope) + ", identityHashCode="
				+ identityHashCode + "]";
	}

}
