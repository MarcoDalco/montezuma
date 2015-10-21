package org.montezuma.test.traffic.writing.serialisation;

import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExpressionRenderer;
import org.montezuma.test.traffic.writing.IdentityHashCodeGenerator;
import org.montezuma.test.traffic.writing.ImportsContainer;

public interface SerialisationRenderer {
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunk, Object object, ImportsContainer importsContainer, IdentityHashCodeGenerator identityHashCodeGenerator);
}
