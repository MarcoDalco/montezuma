package org.montezuma.test.traffic.writing.serialisation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
import org.montezuma.test.traffic.writing.ObjectDeclarationScope;
import org.montezuma.test.traffic.writing.StructuredTextRenderer;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class KryoSerialisationRenderer implements SerialisationRenderer {
	private Serialiser	serialiser	= SerialisationRendererFactory.getSerialiser();

	@Override
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, IdentityHashCodeGenerator identityHashCodeGenerator) {
		try {
			return internalGetDeserialisationCodeChunkFor(codeChunkNeedingDeserialisation, object, importsContainer, objectDeclarationScope, identityHashCodeGenerator);
		}
		catch (IOException ioe) {
			throw new IllegalStateException("This shouldn't happen and anyway there is nothing to do in this case", ioe);
		}
	}

	public ExpressionRenderer internalGetDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, final ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, IdentityHashCodeGenerator identityHashCodeGenerator) throws IOException {
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.Kryo"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser"));

		final int kryoInstanceID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer kryoVariableNameRenderer = new NewGeneratedVariableNameRenderer(kryoInstanceID, Kryo.class, importsContainer, objectDeclarationScope, "k");
		InitCodeChunk initCodeChunk = new InitCodeChunk(kryoInstanceID, codeChunkNeedingDeserialisation) {
			@Override
			public void generateRequiredInits() {
				// FIXME: if required twice in a method, this causes a compile error, as it declares Kryo twice.
				ClassNameRenderer kryoClassNameRenderer = new ClassNameRenderer(Kryo.class, importsContainer);
				VariableDeclarationRenderer kryoVariableDeclarationRenderer = new VariableDeclarationRenderer("%s %s = new %s();", Kryo.class, kryoClassNameRenderer, kryoVariableNameRenderer, kryoClassNameRenderer);
				codeRenderers.add(kryoVariableDeclarationRenderer);
				addDeclaredObject(kryoInstanceID, kryoVariableDeclarationRenderer);

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
		NewGeneratedVariableNameRenderer tmpObjectVariableNameRenderer = new NewGeneratedVariableNameRenderer(createdObjectID, Object.class, importsContainer, codeChunkNeedingDeserialisation, "deser");
		ClassNameRenderer baisClassNameRenderer = new ClassNameRenderer(ByteArrayInputStream.class, importsContainer);
		final int baisID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer baisVarNameRenderer = new NewGeneratedVariableNameRenderer(baisID, ByteArrayInputStream.class, importsContainer, codeChunkNeedingDeserialisation, "tmp");
		ExpressionRenderer baObjectInitCode = ExpressionRenderer.stringRenderer(getSerialisedObjectSourceCode(object));
		ClassNameRenderer kryoInputClassNameRenderer = new ClassNameRenderer(Input.class, importsContainer);
		final int inputID = identityHashCodeGenerator.generateIdentityHashCode();
		NewGeneratedVariableNameRenderer kryoInputVarNameRenderer = new NewGeneratedVariableNameRenderer(inputID, Input.class, importsContainer, codeChunkNeedingDeserialisation, "tmp");
		VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s", Object.class, new ClassNameRenderer(Object.class, importsContainer), tmpObjectVariableNameRenderer);
		VariableDeclarationRenderer baisVariableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = new %s(%s)", ByteArrayInputStream.class, baisClassNameRenderer, baisVarNameRenderer, baisClassNameRenderer, baObjectInitCode);
		VariableDeclarationRenderer kryoInputVariableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = new %s(%s)", Input.class, kryoInputClassNameRenderer, kryoInputVarNameRenderer, kryoInputClassNameRenderer, baisVarNameRenderer);
		ExpressionRenderer renderer =
				new StructuredTextRenderer(
						"%s;\n" + "try (%s;\n" + "     %s) {\n" + "  %s = %s.readClassAndObject(%s);\n" + "}", variableDeclarationRenderer, baisVariableDeclarationRenderer, kryoInputVariableDeclarationRenderer, tmpObjectVariableNameRenderer, kryoVariableNameRenderer, kryoInputVarNameRenderer);
		codeChunkNeedingDeserialisation.codeRenderers.add(renderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(createdObjectID, variableDeclarationRenderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(inputID, kryoInputVariableDeclarationRenderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(baisID, baisVariableDeclarationRenderer);

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
