package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.writing.serialisation.SerialisationRendererFactory;

import java.util.ArrayList;
import java.util.List;

public class RenderersStrategy {

	NewGeneratedVariableNameRenderer getMockedFieldNameRenderer(Class<?> clazz, int id) {
		return new NewGeneratedVariableNameRenderer(id, clazz, "mocked");
	}

	StructuredTextRenderer addRealParameter(CodeChunk codeChunk, Class<?> argClass, Object arg, int argID, ImportsContainer importsContainer) {
		final ClassNameRenderer classNameRenderer = new ClassNameRenderer(argClass, importsContainer);
		final StructuredTextRenderer renderer =
				new StructuredTextRenderer("final %s %s = (%s) %s;", classNameRenderer, new NewGeneratedVariableNameRenderer(argID, argClass, "given"), classNameRenderer, getDeserialisationRenderer(
						codeChunk, arg, importsContainer));
		codeChunk.addDeclaredIdentityHashCode(argID);
		return renderer;
	}

	private ExpressionRenderer getDeserialisationRenderer(CodeChunk codeChunk, Object object, ImportsContainer importsContainer) {
		return SerialisationRendererFactory.getSerialisationRenderer().getDeserialisationCodeChunkFor(codeChunk, object, importsContainer);
	}

	void addMock(int identityHashCode, Class<?> argClass, final NewGeneratedVariableNameRenderer newGeneratedVariableNameRenderer, ImportsContainer importsContainer, TestClassWriter testClassWriter) {
		// TODO - add mocks to a "(Mocked)FieldContainer" instead of the testClassWriter
		// TODO - get the argClass simpleName lazily from the ImportContainer
		// TODO - use @Injectable for stubbing instances, but @Mocked for stubbing static methods.
		importsContainer.addImport(new Import("mockit.Mocked"));
		importsContainer.addImport(new Import(argClass.getCanonicalName()));
		testClassWriter.addField(identityHashCode, new StructuredTextRenderer("@Mocked private %s %s;", new ClassNameRenderer(argClass, importsContainer), newGeneratedVariableNameRenderer));
		testClassWriter.addDeclaredIdentityHashCode(identityHashCode);
	}

	StructuredTextRenderer buildInvocationParameters(CodeChunk maincodeChunk, Object[] args, String[] argTypes, int[] argIDs, ImportsContainer importsContainer, MockingStrategy mockingStrategy, TestClassWriter testClassWriter) {
	
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
	
				final InitCodeChunk variableCodeChunk = createInitCodeChunk(arg, argClass, argID, "given", importsContainer, mockingStrategy, testClassWriter);
				maincodeChunk.requiredInits.put(argID, variableCodeChunk);
	
				// TODO - consider argTypes[i] for potential cast to specify the correct signature in case the target class has
				// overloaded methods where one parameter is more specific in one method than the other, e.g.: valueOf(Number)
				// and valueOf(Long). It will require this method to take the Declaring Type as an extra parameter.
				// In the above process, consider the class type of each argument as given in the argTypes String array,
				// i.e.: int.class versus Integer.class
				expressionRenderers.add(new NewGeneratedVariableNameRenderer(argID, argClass, "given"));
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

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix, ImportsContainer importsContainer, MockingStrategy mockingStrategy, TestClassWriter testClassWriter) {
		return new StandardInitCodeChunk(argID, arg, argClass, argID, variableNamePrefix, importsContainer, mockingStrategy, this, testClassWriter);
	}

}
