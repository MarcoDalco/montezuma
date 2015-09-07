package org.montezuma.test.traffic.writing;

public abstract class InitCodeChunk extends CodeChunk {
	public final int	identityHashCode;

	public InitCodeChunk(int identityHashCode) {
		this.identityHashCode = identityHashCode;
	}

	@Override
	public String toString() {
		return "InitCodeChunk [identityHashCode=" + identityHashCode + ", superClass:{" + super.toString() + "}]";
	}

	public abstract void generateRequiredInits();

	protected TextRenderer getRenderer() {
		return this;
	}

	@Override
	public void preprocess() {
		generateRequiredInits();
		super.preprocess();
	}
}
