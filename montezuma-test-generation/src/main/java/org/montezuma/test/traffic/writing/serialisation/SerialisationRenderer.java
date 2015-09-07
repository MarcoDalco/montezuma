package org.montezuma.test.traffic.writing.serialisation;

import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExpressionRenderer;

public interface SerialisationRenderer {
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunk, Object object);
}
