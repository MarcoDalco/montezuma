package org.montezuma.test.traffic.writing;

import java.util.Collections;
import java.util.List;

public class StrictExpectationsCodeChunk extends CodeChunk {
	public StrictExpectationsCodeChunk() {}

	@Override
	public String toString() {
		return "StrictExpectationsCodeChunk [toString()=" + super.toString() + "]";
	}

	@Override
	public List<ExpressionRenderer> getExpressionRenderers() {
		final StringBuffer text = new StringBuffer("new StrictExpectations() {{");
		text.append(StructuredTextFileWriter.EOL);
		for (int i = 0; i < codeRenderers.size(); i++) {
			text.append(StructuredTextFileWriter.INDENTATION_UNIT + "%s");
			text.append(StructuredTextFileWriter.EOL);
		}
		text.append("}};");
		text.append(StructuredTextFileWriter.EOL);

		return Collections.singletonList(new StructuredTextRenderer(text.toString(), codeRenderers.toArray(new ExpressionRenderer[codeRenderers.size()])));
	}

	public boolean canCombineWith(CodeChunk previous) {
		return (previous instanceof StrictExpectationsCodeChunk); // By default!!
	}

	public CodeChunk combineWith(CodeChunk codeChunk) {
		if (!(codeChunk instanceof StrictExpectationsCodeChunk)) {
			throw new IllegalStateException("BUG - this should have been invoked with a StrictExpectationCodeChunk instead of just this kind of parameter:" + codeChunk);
		}

		StrictExpectationsCodeChunk newChunk = new StrictExpectationsCodeChunk();

		newChunk.mergeAllFrom(this);
		newChunk.mergeAllFrom((StrictExpectationsCodeChunk) codeChunk);

		return newChunk;
	}

	private void mergeAllFrom(StrictExpectationsCodeChunk strictExpectationsCodeChunk) {
		super.mergeAllFrom(strictExpectationsCodeChunk);
	}
}
