package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.writing.serialisation.SerialisationRendererFactory;

import java.util.ArrayList;
import java.util.List;

public class RenderersStrategy {

	NewGeneratedVariableNameRenderer getStubbedFieldNameRenderer(Class<?> clazz, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, int id) {
		return new NewGeneratedVariableNameRenderer(id, clazz, importsContainer, objectDeclarationScope, "mocked");
	}

	StructuredTextRenderer addRealParameter(CodeChunk codeChunk, Class<?> argClass, Object arg, int argID, ImportsContainer importsContainer, IdentityHashCodeGenerator identityHashCodeGenerator) {
		final ClassNameRenderer classNameRenderer = new ClassNameRenderer(argClass, importsContainer);
		final VariableDeclarationRenderer renderer =
				new VariableDeclarationRenderer("final %s %s = (%s) %s;", argClass, classNameRenderer, new NewGeneratedVariableNameRenderer(argID, argClass, importsContainer, codeChunk, "given"), classNameRenderer, getDeserialisationRenderer(
						codeChunk, arg, importsContainer, codeChunk, identityHashCodeGenerator));
		codeChunk.addDeclaredObject(argID, renderer);
		return renderer;
	}

	private ExpressionRenderer getDeserialisationRenderer(CodeChunk codeChunk, Object object, ImportsContainer importsContainer, ObjectDeclarationScope objectDeclarationScope, IdentityHashCodeGenerator identityHashCodeGenerator) {
		return SerialisationRendererFactory.getSerialisationRenderer().getDeserialisationCodeChunkFor(codeChunk, object, importsContainer, objectDeclarationScope, identityHashCodeGenerator);
	}

	VariableDeclarationRenderer addStub(boolean isStaticStub, int identityHashCode, Class<?> argClass, final NewGeneratedVariableNameRenderer newGeneratedVariableNameRenderer, ImportsContainer importsContainer, TestClassWriter testClassWriter) {
		// TODO - add mocks to a "(Stubbed)FieldContainer" instead of the testClassWriter
		// TODO - get the argClass simpleName lazily from the ImportContainer
		final Import requiredImport;
		final String annotation;
		if (isStaticStub) {
			requiredImport = new Import("mockit.Mocked");
			annotation = "@Mocked";
		} else {
			requiredImport = new Import("mockit.Injectable");
			annotation = "@Injectable";
		}
		importsContainer.addImport(requiredImport);
		importsContainer.addImport(new Import(argClass.getCanonicalName()));
		VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer(annotation + " private %s %s;", argClass, new ClassNameRenderer(argClass, importsContainer), newGeneratedVariableNameRenderer);
		testClassWriter.addField(identityHashCode, variableDeclarationRenderer);
		testClassWriter.addDeclaredObject(identityHashCode, variableDeclarationRenderer);
		
		return variableDeclarationRenderer;
	}

	StructuredTextRenderer buildInvocationParameters(CodeChunk mainCodeChunk, Object[] args, String[] argTypes, int[] argIDs, ImportsContainer importsContainer, MockingStrategy mockingStrategy, TestClassWriter testClassWriter) {
	
		List<ExpressionRenderer> expressionRenderers = new ArrayList<>();
		final String argSeparator = ", ";
	
		StringBuffer argumentNames = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				argumentNames.append("null");
			} else {
				final Class<?> argClass = (arg instanceof MustMock ? ((MustMock) arg).clazz : arg.getClass());
				final int argID = argIDs[i];
	
				// Here I reuse a previous initialisation, to avoid replacing the existing one, which needs to be "preprocessed" for other objects to use it. NOT IDEAL or is it correct? I'm now thinking the latter.
				InitCodeChunk variableCodeChunk = mainCodeChunk.requiredInits.get(argID);
				if (variableCodeChunk == null) {
					variableCodeChunk = createInitCodeChunk(arg, argClass, argID, "given", importsContainer, mockingStrategy, testClassWriter, mainCodeChunk);
					mainCodeChunk.requiredInits.put(argID, variableCodeChunk);
				}
	
				// TODO - consider argTypes[i] for potential cast to specify the correct signature in case the target class has
				// overloaded methods where one parameter is more specific in one method than the other, e.g.: valueOf(Number)
				// and valueOf(Long). It will require this method to take the Declaring Type as an extra parameter.
				// In the above process, consider the class type of each argument as given in the argTypes String array,
				// i.e.: int.class versus Integer.class
				expressionRenderers.add(new NewGeneratedVariableNameRenderer(argID, argClass, importsContainer, variableCodeChunk, "given"));
				argumentNames.append("%s");
			}
			argumentNames.append(argSeparator);
		}
	
		final int argumentNamesLength = argumentNames.length();
		if (argumentNamesLength > 0) {
			argumentNames.setLength(argumentNamesLength - argSeparator.length());
		}
	
		return new StructuredTextRenderer(argumentNames.toString(), expressionRenderers.toArray(new ExpressionRenderer[expressionRenderers.size()]));
	}

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix, ImportsContainer importsContainer, MockingStrategy mockingStrategy, TestClassWriter testClassWriter, ObjectDeclarationScope parentObjectDeclarationScope) {
		return new StandardInitCodeChunk(argID, arg, argClass, argID, variableNamePrefix, importsContainer, mockingStrategy, this, testClassWriter, parentObjectDeclarationScope);
	}

}
