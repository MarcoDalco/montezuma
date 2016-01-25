package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.writing.serialisation.SerialisationRendererFactory;

import java.io.IOException;
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
				if ((variableCodeChunk == null) ||
						!(MockingFrameworkFactory.getMockingFramework().canStubMultipleTypeWithOneStub() || ((variableCodeChunk instanceof StandardInitCodeChunk) && (argClass.isAssignableFrom(((StandardInitCodeChunk) variableCodeChunk).argClass))))) {
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

	ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, Object returnValue, Class<?> returnValueDeclaredType, int identityHashCode, ObjectDeclarationScope objectDeclarationScope, ImportsContainer importsContainer, MockingStrategy mockingStrategy, RenderersStrategy renderersStrategy, TestClassWriter testClassWriter) throws ClassNotFoundException, IOException {
		if (returnValue == null) {
			return ExpressionRenderer.stringRenderer("null");
		}
	
		final Object arg = returnValue;
		final Class<?> argClass = returnValueDeclaredType;
		final int argID = identityHashCode;
	
		// Here I reuse a previous initialisation, to avoid replacing the existing one, which needs to be "preprocessed" for other objects to use it. NOT IDEAL.
		// TODO - when not mocked, should this be a reconstructed object, instead?
		if (codeChunk.declaresOrCanSeeIdentityHashCode(identityHashCode))
			return new ExistingVariableNameRenderer(identityHashCode, argClass, importsContainer, objectDeclarationScope);
	
		InitCodeChunk returnValueInitCodeChunk = codeChunk.requiredInits.get(identityHashCode);
		if (returnValueInitCodeChunk == null) {
			returnValueInitCodeChunk = new StandardInitCodeChunk(argID, arg, argClass, argID, "expected", importsContainer, mockingStrategy, renderersStrategy, testClassWriter, codeChunk);
			codeChunk.requiredInits.put(identityHashCode, returnValueInitCodeChunk);
		}
	
		// final Class<? extends Object> returnValueClass = (returnValue instanceof MustMock ? ((MustMock)
		// returnValue).clazz : returnValue.getClass());
		// TODO - To be checked with downcast invocations, i.e. when the object - say it's returned by an expected
		// invocation - is then cast by the 'cut' to a more specific type and a method from that type is invoked. That would
		// be a good reason to use returnValueClass (returnValue.getClass()), but such class might not be visible (private
		// inner class).
		return new NewGeneratedVariableNameRenderer(identityHashCode, returnValueDeclaredType, importsContainer, returnValueInitCodeChunk, "expected");
	}

}
