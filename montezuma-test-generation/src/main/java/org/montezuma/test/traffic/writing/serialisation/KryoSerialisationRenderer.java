package org.montezuma.test.traffic.writing.serialisation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import org.montezuma.test.traffic.serialisers.Serialiser;
import org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser;
import org.montezuma.test.traffic.writing.ClassNameRenderer;
import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExpressionRenderer;
import org.montezuma.test.traffic.writing.IdentityHashCodeGenerator;
import org.montezuma.test.traffic.writing.Import;
import org.montezuma.test.traffic.writing.ImportsContainer;
import org.montezuma.test.traffic.writing.InitCodeChunk;
import org.montezuma.test.traffic.writing.NewGeneratedVariableNameRenderer;
import org.montezuma.test.traffic.writing.StructuredTextRenderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class KryoSerialisationRenderer implements SerialisationRenderer {
	private Serialiser	serialiser	= SerialisationRendererFactory.getSerialiser();

	@Override
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, ImportsContainer importsContainer, IdentityHashCodeGenerator identityHashCodeGenerator) {
		try {
			return internalGetDeserialisationCodeChunkFor(codeChunkNeedingDeserialisation, object, importsContainer, identityHashCodeGenerator);
		}
		catch (IOException ioe) {
			throw new IllegalStateException("This shouldn't happen and anyway there is nothing to do in this case", ioe);
		}
	}

	public ExpressionRenderer internalGetDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, final ImportsContainer importsContainer, IdentityHashCodeGenerator identityHashCodeGenerator) throws IOException {
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.Kryo"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser"));

		final int kryoInstanceID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer kryoVariableNameRenderer = new NewGeneratedVariableNameRenderer(kryoInstanceID, Kryo.class, "k");
		InitCodeChunk initCodeChunk = new InitCodeChunk(kryoInstanceID) {
			@Override
			public void generateRequiredInits() {
				// FIXME: if required twice in a method, this causes a compile error, as it declares Kryo twice.
				ClassNameRenderer kryoClassNameRenderer = new ClassNameRenderer(Kryo.class, importsContainer);
				codeRenderers.add(new StructuredTextRenderer("%s %s = new %s();", kryoClassNameRenderer, kryoVariableNameRenderer, kryoClassNameRenderer));
				addDeclaredIdentityHashCode(kryoInstanceID);

				ClassNameRenderer kryoRegisteredSerialiserClassNameRenderer = new ClassNameRenderer(KryoRegisteredSerialiser.class, importsContainer);
				codeRenderers.add(new StructuredTextRenderer("%s.setDefaultSerializer(%s.class);", kryoVariableNameRenderer, kryoRegisteredSerialiserClassNameRenderer));
			}
		};
		codeChunkNeedingDeserialisation.requiredInits.put(kryoInstanceID, initCodeChunk);

		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.ByteArrayInputStream"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.io.Input"));
		codeChunkNeedingDeserialisation.declaredThrowables.add(ClassNotFoundException.class);
		codeChunkNeedingDeserialisation.declaredThrowables.add(IOException.class);
		final int createdObjectID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer tmpObjectVariableNameRenderer = new NewGeneratedVariableNameRenderer(createdObjectID, Object.class, "deser");
		ClassNameRenderer baisClassNameRenderer = new ClassNameRenderer(ByteArrayInputStream.class, importsContainer);
		final int baisID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer baisVarNameRenderer = new NewGeneratedVariableNameRenderer(baisID, ByteArrayInputStream.class, "tmp");
		ExpressionRenderer baObjectInitCode = ExpressionRenderer.stringRenderer(getSerialisedObjectSourceCode(object));
		ClassNameRenderer kryoInputClassNameRenderer = new ClassNameRenderer(Input.class, importsContainer);
		final int inputID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer kryoInputVarNameRenderer = new NewGeneratedVariableNameRenderer(inputID, Input.class, "tmp");
		ExpressionRenderer renderer =
				new StructuredTextRenderer(
						"final Object %s;\n" + "try (final %s %s = new %s(%s);\n" + "     final %s %s = new %s(%s)) {\n" + "  %s = %s.readClassAndObject(%s);\n" + "}", tmpObjectVariableNameRenderer,
						baisClassNameRenderer, baisVarNameRenderer, baisClassNameRenderer, baObjectInitCode, kryoInputClassNameRenderer, kryoInputVarNameRenderer, kryoInputClassNameRenderer, baisVarNameRenderer,
						tmpObjectVariableNameRenderer, kryoVariableNameRenderer, kryoInputVarNameRenderer);
		codeChunkNeedingDeserialisation.codeRenderers.add(renderer);
		codeChunkNeedingDeserialisation.addDeclaredIdentityHashCode(createdObjectID);
		codeChunkNeedingDeserialisation.addDeclaredIdentityHashCode(inputID);
		codeChunkNeedingDeserialisation.addDeclaredIdentityHashCode(baisID);

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
