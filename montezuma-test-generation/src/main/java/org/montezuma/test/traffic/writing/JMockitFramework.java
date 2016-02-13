package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.TrafficReader;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer.ComputableClassNameRendererPlaceholder;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class JMockitFramework implements MockingFramework {

	@Override
	public String getRunwithClassName() {
		return "JMockit";
	}

	@Override
	public String getRunwithClassCanonicalName() {
		return "mockit.integration.junit4.JMockit";
	}

	@Override
	public boolean canStubMultipleTypeWithOneStub() {
		return true;
	}

	@Override
	public VariableDeclarationRenderer addStub(boolean isStaticStub, int identityHashCode, Class<?> declaredClass, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, TestClassWriter testClassWriter) {
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
		importsContainer.addImport(new Import(declaredClass.getCanonicalName()));
		if (testClassWriter.declaresIdentityHashCode(identityHashCode, declaredClass))
			return testClassWriter.getVisibleDeclarationRenderer(identityHashCode, declaredClass);

		VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer(annotation + " private %s %s;", identityHashCode, "mocked", declaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance);
		testClassWriter.addField(identityHashCode, variableDeclarationRenderer);
		testClassWriter.addDeclaredObject(identityHashCode, variableDeclarationRenderer);
		
		return variableDeclarationRenderer;
	}

	@Override
	public CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		testClassWriter.addImport("mockit.StrictExpectations");
		final StrictExpectationsCodeChunk codeChunk = new StrictExpectationsCodeChunk(objectDeclarationScope);
		String methodSignature = callData.signature;
		final int indexOfSeparatorBetweenMethodNameAndArgs = methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
		final String argTypesSubstring = methodSignature.substring(1 + indexOfSeparatorBetweenMethodNameAndArgs);
		final String[] argTypes = (argTypesSubstring.length() == 0 ? new String[] {} : argTypesSubstring.split(Common.ARGS_SEPARATOR));
		Class<?>[] parameterTypes = ReflectionUtils.buildParameterTypes(argTypes);
	
		String methodName = methodSignature.substring(0, indexOfSeparatorBetweenMethodNameAndArgs);
		final Class<?> declaringType = callData.declaringType;
		final boolean isConstructorInvocation = methodName.equals("<init>");
		// TODO - as part of the work to declare only visible classes: limiting the targetClazzOrDeclaredType to callData.declaringType would be correct, but at the moment, this could override the need for the object-on-which-the-method-is-invoked to have a more specific type definition (subclass or subinterface of callData.declaringType), so until a more sophisticated mechanism is in place to determine the actual type of a stub, this should not be in place - although it doesn't seem to mess up the generated tests.
		// Class<?> targetClazzOrDeclaringType = callData.declaringType; (and remove the ternary operator assigned just below, as redundant.
		Class<?> targetClazzOrDeclaringType = callData.targetClazz;
		targetClazzOrDeclaringType = (targetClazzOrDeclaringType == null ? declaringType : targetClazzOrDeclaringType);
		// "getMethod" returns the method corresponding to that signature, even if declared in superclasses or
		// (super)interfaces, but not private ones, and I'm not sure how it matches non-public ones not accessible from this
		// very class/package,
		// while "getDeclaredMethod" returns only methods declared by that specified class, but considers private methods
		// too. Private methods should not be tested or intercepted, hence the choice to use "getMethod" instead, but look
		// out for "NoSuchMethodException" exceptions
		// TODO - This getMethod/getDeclaredMethod inversion is actually a workaround as AspectJ is returning
		// java.sql.PreparedStatement as the Declaring Class of "close()", when it's actually java.sql.Statement from
		// "Autocloseable". Understand and fix better.
		final Executable declaredMethod = (isConstructorInvocation ? targetClazzOrDeclaringType.getDeclaredConstructor(parameterTypes) : declaringType.getMethod(methodName, parameterTypes));
		@SuppressWarnings("unchecked") final Class<? extends Throwable>[] exceptionTypes = (Class<? extends Throwable>[]) declaredMethod.getExceptionTypes();
		List<Class<? extends Throwable>> exceptionTypesList = Arrays.asList(exceptionTypes);
		codeChunk.declaredThrowables.addAll(exceptionTypesList);
	
		final int id = isConstructorInvocation ? callData.returnValueID : callData.id;
		// TODO - handle null pointers: id == 0 for non-static invocations to null pointers too!
		final boolean isStaticMethod = Modifier.isStatic(callData.modifiers);
		final int identityHashCode = isStaticMethod ? testClassWriter.identityHashCodeGenerator.generateIdentityHashCodeForStaticClass(declaringType) : id;
		Class<?> declaredClass = ReflectionUtils.getVisibleSuperClass(targetClazzOrDeclaringType, testClassWriter.testClass);
		if (!testClassWriter.declaresIdentityHashCode(identityHashCode, declaringType))
			MockingFrameworkFactory.getMockingFramework().addStub(isStaticMethod || isConstructorInvocation, identityHashCode, declaredClass, renderersStrategy, importsContainer, testClassWriter);
		Object[] methodArgs = TrafficReader.getDeserialisedArgs(callData.serialisedArgs);
		final StructuredTextRenderer invocationParameters =
				renderersStrategy.buildInvocationParameters(codeChunk, methodArgs, argTypes, callData.argIDs, importsContainer, mockingStrategy, testClassWriter);
		final ExpressionRenderer invocationExpressionRenderer =
				isConstructorInvocation ?
						new StructuredTextRenderer("new %s(%s);",
								new ClassNameRenderer(declaringType, importsContainer),
								invocationParameters)
						: new StructuredTextRenderer("%s.%s(%s);",
								isStaticMethod ?
										new ClassNameRenderer(declaringType, importsContainer)
										: new ExistingVariableNameRenderer(callData.id, /* TO CHECK - it might need to be the targetClass or its most visible superclass. Perhaps declaredClass */ declaringType, importsContainer, testClassWriter),
								ExpressionRenderer.stringRenderer(methodName),
								invocationParameters);
		final ExpressionRenderer timesExpressionRenderer = ExpressionRenderer.stringRenderer(" times = 1;");
		final byte[] serialisedReturnValue = callData.serialisedReturnValue;
		// TODO - implement behaviour of when a Throwable is thrown rather than a result returned: serialisedReturnValue is
		// null, but returnValueID and serialisedThrowable aren't. Return the throwable.
		final Class<?> returnType = (isConstructorInvocation ? targetClazzOrDeclaringType : ((Method) declaredMethod).getReturnType());
		final ExpressionRenderer resultExpressionRenderer =
				(serialisedReturnValue == null ? ExpressionRenderer.nullRenderer() : new StructuredTextRenderer(" result = %s;", buildExpectedReturnValue(
						codeChunk, serialisedReturnValue, returnType, callData.returnValueID, objectDeclarationScope, deserialiser, testClassWriter, renderersStrategy, importsContainer, mockingStrategy)));
		codeChunk.addExpressionRenderer(new StructuredTextRenderer("%s%s%s", invocationExpressionRenderer, timesExpressionRenderer, resultExpressionRenderer));
	
		return codeChunk;
	}

	private ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, byte[] serialisedReturnValue, Class<?> returnValueDeclaredType, int identityHashCode, ObjectDeclarationScope objectDeclarationScope, Deserialiser deserialiser, TestClassWriter testClassWriter, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy) throws ClassNotFoundException, IOException {
		Object returnValue = deserialiser.deserialise(serialisedReturnValue);
		return renderersStrategy.buildExpectedReturnValue(codeChunk, returnValue, returnValueDeclaredType, identityHashCode, objectDeclarationScope, importsContainer, mockingStrategy, renderersStrategy, testClassWriter);
	}
}
