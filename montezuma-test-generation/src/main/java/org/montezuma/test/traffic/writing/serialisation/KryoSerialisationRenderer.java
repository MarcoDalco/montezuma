package org.montezuma.test.traffic.writing.serialisation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import org.montezuma.test.traffic.serialisers.Serialiser;
import org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser;
import org.montezuma.test.traffic.writing.ClassNameRenderer;
import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExpressionRenderer;
import org.montezuma.test.traffic.writing.Import;
import org.montezuma.test.traffic.writing.ImportsContainer;
import org.montezuma.test.traffic.writing.InitCodeChunk;
import org.montezuma.test.traffic.writing.StructuredTextRenderer;
import org.montezuma.test.traffic.writing.VariableNameRenderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class KryoSerialisationRenderer implements SerialisationRenderer {
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

	public ExpressionRenderer internalGetDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, final ImportsContainer importsContainer) throws IOException {
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.Kryo"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser"));

		VariableNameRenderer kryoVariableNameRenderer = new VariableNameRenderer(9876, Kryo.class, "");
		InitCodeChunk initCodeChunk = new InitCodeChunk(9876) {
			@Override
			public void generateRequiredInits() {
				// FIXME: if required twice in a method, this causes a compile error, as it declares Kryo twice.
				ClassNameRenderer kryoClassNameRenderer = new ClassNameRenderer(Kryo.class, importsContainer);
				codeRenderers.add(new StructuredTextRenderer("%s %s = new %s();", kryoClassNameRenderer, kryoVariableNameRenderer, kryoClassNameRenderer));

				ClassNameRenderer kryoRegisteredSerialiserClassNameRenderer = new ClassNameRenderer(KryoRegisteredSerialiser.class, importsContainer);
				codeRenderers.add(new StructuredTextRenderer("%s.setDefaultSerializer(%s.class);", kryoVariableNameRenderer, kryoRegisteredSerialiserClassNameRenderer));
			}
		};
		codeChunkNeedingDeserialisation.requiredInits.put(9876, initCodeChunk);

		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.ByteArrayInputStream"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.io.Input"));
		codeChunkNeedingDeserialisation.declaredThrowables.add(ClassNotFoundException.class);
		codeChunkNeedingDeserialisation.declaredThrowables.add(IOException.class);
		VariableNameRenderer tmpObjectVariableNameRenderer = new VariableNameRenderer(9873, Object.class, "");
		ClassNameRenderer baisClassNameRenderer = new ClassNameRenderer(ByteArrayInputStream.class, importsContainer);
		VariableNameRenderer baisVarNameRenderer = new VariableNameRenderer(9875, ByteArrayInputStream.class, "");
		ExpressionRenderer baObjectInitCode = ExpressionRenderer.stringRenderer(getSerialisedObjectSourceCode(object));
		ClassNameRenderer kryoInputClassNameRenderer = new ClassNameRenderer(Input.class, importsContainer);
		VariableNameRenderer kryoInputVarNameRenderer = new VariableNameRenderer(9874, Input.class, "");
		ExpressionRenderer renderer =
				new StructuredTextRenderer(
						"final Object %s;\n" + "try (final %s %s = new %s(%s);\n" + "     final %s %s = new %s(%s)) {\n" + "  %s = %s.readClassAndObject(%s);\n" + "}", tmpObjectVariableNameRenderer,
						baisClassNameRenderer, baisVarNameRenderer, baisClassNameRenderer, baObjectInitCode, kryoInputClassNameRenderer, kryoInputVarNameRenderer, kryoInputClassNameRenderer, baisVarNameRenderer,
						tmpObjectVariableNameRenderer, kryoVariableNameRenderer, kryoInputVarNameRenderer);
		codeChunkNeedingDeserialisation.codeRenderers.add(renderer);

		return tmpObjectVariableNameRenderer;
	}

	protected String getSerialisedObjectSourceCode(Object object) throws IOException {
		StringBuffer sb = new StringBuffer("new byte [] {");
		final byte[] serialisedArg = serialiser.serialise(object);
		for (byte aByte : serialisedArg) {
			sb.append(aByte);
			sb.append(",");
		}
		if (serialisedArg.length > 0) {
			sb.setLength(sb.length() - 1);
		}
		sb.append("}");
		return sb.toString();
	}

}
