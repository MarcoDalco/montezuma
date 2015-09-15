package org.montezuma.test.traffic.writing.serialisation;

import org.montezuma.test.traffic.serialisers.Serialiser;
import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExpressionRenderer;
import org.montezuma.test.traffic.writing.Import;
import org.montezuma.test.traffic.writing.ImportsContainer;

import java.io.IOException;

public class StandardJavaSerialisationRenderer implements SerialisationRenderer {
	private Serialiser	serialiser	= SerialisationRendererFactory.getSerialiser();

	@Override
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, ImportsContainer importsContainer) {
		try {
			return internalGetDeserialisationCodeChunkFor(codeChunkNeedingDeserialisation, object, importsContainer);
		}
		catch (IOException ioe) {
			throw new IllegalStateException("This shouldn't happen and anyway there is nothing to do in this case", ioe);
		}
	}

	public ExpressionRenderer internalGetDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, ImportsContainer importsContainer) throws IOException {
		// new ObjectInputStream(new ByteArrayInputStream(new byte[] {})).readObject();
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.ObjectInputStream"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.ByteArrayInputStream"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.IOException"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.lang.ClassNotFoundException"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import(object.getClass().getName()));
		codeChunkNeedingDeserialisation.declaredThrowables.add(IOException.class);
		codeChunkNeedingDeserialisation.declaredThrowables.add(ClassNotFoundException.class);
		StringBuffer sb = new StringBuffer();
		sb.append("new ObjectInputStream(new ByteArrayInputStream(new byte [] {");
		final byte[] serialisedArg = serialiser.serialise(object);
		for (byte aByte : serialisedArg) {
			sb.append(aByte);
			sb.append(",");
		}
		if (serialisedArg.length > 0) {
			sb.setLength(sb.length() - 1);
		}
		sb.append("})).readObject()");
		final String renderedCode = sb.toString();

		return ExpressionRenderer.stringRenderer(renderedCode);
	}

}
