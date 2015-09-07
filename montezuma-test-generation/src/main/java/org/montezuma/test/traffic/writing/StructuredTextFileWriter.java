package org.montezuma.test.traffic.writing;

import java.util.List;

public class StructuredTextFileWriter {
	// TODO: make EOL private and change the referring code to render differently
	static final String		EOL								= System.getProperty("line.separator");
	// TODO: make INDENTATION_UNIT private and change the referring code to render differently
	static final String		INDENTATION_UNIT	= "  ";
	private StringBuffer	indentation				= new StringBuffer();
	private StringBuffer	text							= new StringBuffer();

	public StructuredTextFileWriter() {
		this.indentation.append(INDENTATION_UNIT);
	}

	public void appendLine(int indentationLevel, String line) {
		final int indentationChars = indentationLevel * INDENTATION_UNIT.length();
		while (indentationChars > indentation.length()) {
			indentation.append(INDENTATION_UNIT);
		}

		text.append(indentation.subSequence(0, indentationChars));
		text.append(line);
		text.append(StructuredTextFileWriter.EOL);
	}

	public String toString() {
		return text.toString();
	}

	public void addEmptyLine() {
		text.append(StructuredTextFileWriter.EOL);
	}

	public void appendChunk(int i, CodeChunk codeChunk) {
		for (CodeChunk chunk : codeChunk.requiredInits.values()) {
			appendChunk(i, chunk);
		}

		List<CodeChunk> combinedBeforeLinesCodeChunks = CodeChunk.tryCombine(codeChunk.methodPartsBeforeLines);
		for (CodeChunk chunk : combinedBeforeLinesCodeChunks) {
			appendChunk(i, chunk);
		}

		for (ExpressionRenderer renderer : codeChunk.getExpressionRenderers()) {
			appendLine(i, renderer.render());
		}

		for (CodeChunk chunk : codeChunk.methodPartsAfterLines) {
			appendChunk(i, chunk);
		}
	}
}
