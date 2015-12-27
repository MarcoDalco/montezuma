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

	public abstract void generateRequiredInits();

	protected TextRenderer getRenderer() {
		throw new RuntimeException("UNEXPECTED INVOCATION"); // TODO - Was "return this;": is it safe to remove?
	}

	@Override
	public void preprocess() {
		generateRequiredInits();
		super.preprocess();
	}

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		if (chunkOverridingDeclaration != null)
			return;

		super.render(structuredTextFileWriter);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode) {
		if (chunkOverridingDeclaration != null) {
			return chunkOverridingDeclaration.getVisibleDeclarationRenderer(identityHashCode);
		}

		return super.getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode) {
		if (chunkOverridingDeclaration != null) {
			return chunkOverridingDeclaration.getVisibleDeclarationRenderer(identityHashCode);
		}

		return super.getVisibleDeclarationRenderer(identityHashCode);
	}

	boolean shouldBeRendered() {
		return (chunkOverridingDeclaration == null);
	}
}
