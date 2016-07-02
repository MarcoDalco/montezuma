package org.montezuma.test.traffic.writing;

import java.util.Collections;
import java.util.List;

public class StrictExpectationsCodeChunk extends CodeChunk {
	public StrictExpectationsCodeChunk(ObjectDeclarationScope parentObjectDeclarationScope) {
		super(parentObjectDeclarationScope);
	}

	@Override
	public String toString() {
		return "StrictExpectationsCodeChunk [toString()=" + super.toString() + "]";
	}

	@Override
	public List<ExpressionRenderer> getExpressionRenderers() {
		return MockingFrameworkFactory.getMockingFramework().getStrictExpectationsRenderers(codeRenderers);
	}

	public boolean canCombineWith(CodeChunk previous) {
		// Should we check that they have the same parentObjectDeclarationScope? It should not be necessary, as already ensured otherwise.
		return (previous instanceof StrictExpectationsCodeChunk); // By default!!
	}

	public CodeChunk combineWith(CodeChunk codeChunk) {
		if (!(codeChunk instanceof StrictExpectationsCodeChunk)) {
			throw new IllegalStateException("BUG - this should have been invoked with a StrictExpectationCodeChunk instead of just this kind of parameter:" + codeChunk);
		}

		StrictExpectationsCodeChunk newChunk = new StrictExpectationsCodeChunk(parentObjectDeclarationScope);

		newChunk.mergeAllFrom(this);
		newChunk.mergeAllFrom((StrictExpectationsCodeChunk) codeChunk);

		return newChunk;
	}

	private void mergeAllFrom(StrictExpectationsCodeChunk strictExpectationsCodeChunk) {
		super.mergeAllFrom(strictExpectationsCodeChunk);
	}
}
