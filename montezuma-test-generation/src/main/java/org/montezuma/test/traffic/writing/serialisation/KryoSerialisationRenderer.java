package org.montezuma.test.traffic.writing.serialisation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import org.montezuma.test.traffic.serialisers.Serialiser;
import org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser;
import org.montezuma.test.traffic.writing.ClassNameRenderer;
import org.montezuma.test.traffic.writing.CodeChunk;
import org.montezuma.test.traffic.writing.ExistingVariableNameRenderer;
import org.montezuma.test.traffic.writing.ExpressionRenderer;
import org.montezuma.test.traffic.writing.IdentityHashCodeGenerator;
import org.montezuma.test.traffic.writing.Import;
import org.montezuma.test.traffic.writing.ImportsContainer;
import org.montezuma.test.traffic.writing.InitCodeChunk;
import org.montezuma.test.traffic.writing.ObjectDeclarationScope;
import org.montezuma.test.traffic.writing.StructuredTextRenderer;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer.ComputableClassNameRendererPlaceholder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class KryoSerialisationRenderer implements SerialisationRenderer {
	private Serialiser	serialiser	= SerialisationRendererFactory.getSerialiser();

	@Override
	public ExpressionRenderer getDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, IdentityHashCodeGenerator identityHashCodeGenerator) throws ClassNotFoundException {
		try {
			return internalGetDeserialisationCodeChunkFor(codeChunkNeedingDeserialisation, object, importsContainer, objectDeclarationScope, identityHashCodeGenerator);
		}
		catch (IOException ioe) {
			throw new IllegalStateException("This shouldn't happen and anyway there is nothing to do in this case", ioe);
		}
	}

	public ExpressionRenderer internalGetDeserialisationCodeChunkFor(CodeChunk codeChunkNeedingDeserialisation, Object object, final ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, IdentityHashCodeGenerator identityHashCodeGenerator) throws IOException, ClassNotFoundException {
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.Kryo"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser"));

		final int kryoInstanceID = identityHashCodeGenerator.generateIdentityHashCode();
		ExistingVariableNameRenderer existingKryoVariableNameRenderer = new ExistingVariableNameRenderer(kryoInstanceID, Kryo.class, importsContainer, objectDeclarationScope);
		InitCodeChunk initCodeChunk = new InitCodeChunk(kryoInstanceID, codeChunkNeedingDeserialisation) {
			@Override
			public void generateRequiredInits() {
				// FIXME: if required twice in a method, this causes a compile error, as it declares Kryo twice.
				VariableDeclarationRenderer kryoVariableDeclarationRenderer = new VariableDeclarationRenderer("%s %s = %s;", kryoInstanceID, "k", Kryo.class, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s()", ComputableClassNameRendererPlaceholder.instance));
				codeRenderers.add(kryoVariableDeclarationRenderer);
				addDeclaredObject(kryoInstanceID, kryoVariableDeclarationRenderer);

				ClassNameRenderer kryoRegisteredSerialiserClassNameRenderer = new ClassNameRenderer(KryoRegisteredSerialiser.class, importsContainer);
				codeRenderers.add(new StructuredTextRenderer("%s.setDefaultSerializer(%s.class);", existingKryoVariableNameRenderer, kryoRegisteredSerialiserClassNameRenderer));
			}
		};
		codeChunkNeedingDeserialisation.requiredInits.put(kryoInstanceID, initCodeChunk);
		initCodeChunk.generateRequiredInits();
		initCodeChunk.declaresIdentityHashCode(kryoInstanceID, Kryo.class);

		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("java.io.ByteArrayInputStream"));
		codeChunkNeedingDeserialisation.requiredImports.addImport(new Import("com.esotericsoftware.kryo.io.Input"));
		codeChunkNeedingDeserialisation.declaredThrowables.add(ClassNotFoundException.class);
		codeChunkNeedingDeserialisation.declaredThrowables.add(IOException.class);
		final int createdObjectID = identityHashCodeGenerator.generateIdentityHashCode();
		ExistingVariableNameRenderer existingTmpObjectVariableNameRenderer = new ExistingVariableNameRenderer(createdObjectID, Object.class, importsContainer, codeChunkNeedingDeserialisation);
		final int baisID = identityHashCodeGenerator.generateIdentityHashCode();
		ExistingVariableNameRenderer existingBaisVarNameRenderer = new ExistingVariableNameRenderer(baisID, ByteArrayInputStream.class, importsContainer, codeChunkNeedingDeserialisation);
		ExpressionRenderer baObjectInitCode = ExpressionRenderer.stringRenderer(getSerialisedObjectSourceCode(object));
		final int inputID = identityHashCodeGenerator.generateIdentityHashCode();
		ExistingVariableNameRenderer existingKryoInputVarNameRenderer = new ExistingVariableNameRenderer(inputID, Input.class, importsContainer, codeChunkNeedingDeserialisation);
		VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s", createdObjectID, "deser", Object.class, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, null);
		initCodeChunk.declaresIdentityHashCode(createdObjectID, Object.class);
		VariableDeclarationRenderer baisVariableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s", baisID, "tmp", ByteArrayInputStream.class, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s(%s)", ComputableClassNameRendererPlaceholder.instance, baObjectInitCode));
		initCodeChunk.declaresIdentityHashCode(baisID, ByteArrayInputStream.class);
		VariableDeclarationRenderer kryoInputVariableDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s", inputID, "tmp", Input.class, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("new %s(%s)", ComputableClassNameRendererPlaceholder.instance, existingBaisVarNameRenderer));
		initCodeChunk.declaresIdentityHashCode(inputID, Input.class);
		ExpressionRenderer renderer =
				new StructuredTextRenderer(
						"%s;\n" + "try (%s;\n" + "     %s) {\n" + "  %s = %s.readClassAndObject(%s);\n" + "}", variableDeclarationRenderer, baisVariableDeclarationRenderer, kryoInputVariableDeclarationRenderer, existingTmpObjectVariableNameRenderer, existingKryoVariableNameRenderer, existingKryoInputVarNameRenderer);
		codeChunkNeedingDeserialisation.codeRenderers.add(renderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(createdObjectID, variableDeclarationRenderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(inputID, kryoInputVariableDeclarationRenderer);
		codeChunkNeedingDeserialisation.addDeclaredObject(baisID, baisVariableDeclarationRenderer);

		return existingTmpObjectVariableNameRenderer;
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
