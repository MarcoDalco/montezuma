package org.montezuma.test.traffic.writing;

public abstract class InitCodeChunk extends CodeChunk {
	public final int	identityHashCode;
	InitCodeChunk chunkOverridingDeclaration;

	public InitCodeChunk(int identityHashCode, ObjectDeclarationScope parentObjectDeclarationScope) {
		super(parentObjectDeclarationScope);
		this.identityHashCode = identityHashCode;
	}

	@Override
	public String toString() {
		return "InitCodeChunk [identityHashCode=" + identityHashCode + ", superClass:{" + super.toString() + "}]";
	}

	public abstract void generateRequiredInits() throws ClassNotFoundException;

	@Override
	public void preprocess() {
		//generateRequiredInits();
		super.preprocess();
	}

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		if (chunkOverridingDeclaration != null)
			return;

		super.render(structuredTextFileWriter);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode, Class<?> requiredClass) {
		if (chunkOverridingDeclaration != null) {
			return chunkOverridingDeclaration.getVisibleDeclarationRenderer(identityHashCode, requiredClass);
		}

		return super.getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode, requiredClass);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode, Class<?> requiredClass) {
		if (chunkOverridingDeclaration != null) {
			return chunkOverridingDeclaration.getVisibleDeclarationRenderer(identityHashCode, requiredClass);
		}

		return super.getVisibleDeclarationRenderer(identityHashCode, requiredClass);
	}

	boolean shouldBeRendered() {
		return (chunkOverridingDeclaration == null);
	}
}
