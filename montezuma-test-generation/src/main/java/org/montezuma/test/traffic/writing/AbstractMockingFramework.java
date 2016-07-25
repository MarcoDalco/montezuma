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

public abstract class AbstractMockingFramework implements MockingFramework {

	@Override
	public CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		final StrictExpectationsCodeChunk codeChunk = new StrictExpectationsCodeChunk(objectDeclarationScope);
		String methodSignature = callData.signature;
		final int indexOfSeparatorBetweenMethodNameAndArgs = methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
		final String argTypesSubstring = methodSignature.substring(1 + indexOfSeparatorBetweenMethodNameAndArgs);
		final String[] argTypes = (argTypesSubstring.length() == 0 ? new String[] {} : argTypesSubstring.split(Common.ARGS_SEPARATOR));
		Class<?>[] parameterTypes = ReflectionUtils.buildParameterTypes(argTypes);
	
		String methodName = methodSignature.substring(0, indexOfSeparatorBetweenMethodNameAndArgs);
		final Class<?> declaringType = callData.declaringType;
		final boolean isConstructorInvocation = methodName.equals("<init>");
		// "getMethod" returns the method corresponding to that signature, even if declared in superclasses or
		// (super)interfaces, but not private ones, and I'm not sure how it matches non-public ones not accessible from this
		// very class/package,
		// while "getDeclaredMethod" returns only methods declared by that specified class, but considers private methods
		// too. Private methods should not be tested or intercepted, hence the choice to use "getMethod" instead, but look
		// out for "NoSuchMethodException" exceptions
		// TODO - This getMethod/getDeclaredMethod inversion is actually a workaround as AspectJ is returning
		// java.sql.PreparedStatement as the Declaring Class of "close()", when it's actually java.sql.Statement from
		// "Autocloseable". Understand and fix better.
		final Executable declaredMethod = (isConstructorInvocation ? declaringType.getDeclaredConstructor(parameterTypes) : declaringType.getMethod(methodName, parameterTypes));
		@SuppressWarnings("unchecked") final Class<? extends Throwable>[] exceptionTypes = (Class<? extends Throwable>[]) declaredMethod.getExceptionTypes();
		List<Class<? extends Throwable>> exceptionTypesList = Arrays.asList(exceptionTypes);
		codeChunk.declaredThrowables.addAll(exceptionTypesList);
	
		final int id = isConstructorInvocation ? callData.returnValueID : callData.id;
		// TODO - handle null pointers: id == 0 for non-static invocations to null pointers too!
		final boolean isStaticMethod = Modifier.isStatic(callData.modifiers);
		final int identityHashCode = isStaticMethod ? testClassWriter.identityHashCodeGenerator.generateIdentityHashCodeForStaticClass(declaringType) : id;
		Class<?> declaredClass = ReflectionUtils.getVisibleSuperClass(declaringType, testClassWriter.testedClass);
		ObjectDeclarationScope stubDeclarationScope = getStubDeclarationScope(objectDeclarationScope, testClassWriter);
		if (!stubDeclarationScope.declaresOrCanSeeIdentityHashCode(identityHashCode, declaredClass)) {
			MockingFrameworkFactory.getMockingFramework().addStub(isStaticMethod || isConstructorInvocation, isConstructorInvocation, identityHashCode, declaredClass, renderersStrategy, importsContainer, testMethod, codeChunk);
			stubDeclarationScope.declaresIdentityHashCode(identityHashCode, declaredClass); // Just to tell the VariableDeclarationRenderer that there is one reference to the variable
		}
		Object[] methodArgs = TrafficReader.getDeserialisedArgs(callData.serialisedArgs);
		final StructuredTextRenderer invocationParameters =
				renderersStrategy.buildInvocationParameters(codeChunk, methodArgs, argTypes, callData.argIDs, importsContainer, mockingStrategy, testClassWriter, testMethod);
		final byte[] serialisedReturnValue = callData.serialisedReturnValue;
		writeExpectation(
				callData, objectDeclarationScope, testClassWriter, testMethod, renderersStrategy, importsContainer, mockingStrategy, deserialiser, codeChunk, methodName, declaringType,
				isConstructorInvocation, declaredMethod, isStaticMethod, invocationParameters, serialisedReturnValue);
	
		return codeChunk;
	}

	protected abstract ObjectDeclarationScope getStubDeclarationScope(ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter);

	protected abstract void writeExpectation(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser, final StrictExpectationsCodeChunk codeChunk, String methodName, final Class<?> declaringType, final boolean isConstructorInvocation, final Executable declaredMethod, final boolean isStaticMethod, final StructuredTextRenderer invocationParameters, final byte[] serialisedReturnValue) throws ClassNotFoundException, IOException;

	protected ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, byte[] serialisedReturnValue, Class<?> returnValueDeclaredType, int identityHashCode, ObjectDeclarationScope objectDeclarationScope, Deserialiser deserialiser, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy) throws ClassNotFoundException, IOException {
		Object returnValue = deserialiser.deserialise(serialisedReturnValue);
		return renderersStrategy.buildExpectedReturnValue(codeChunk, returnValue, returnValueDeclaredType, identityHashCode, importsContainer, mockingStrategy, renderersStrategy, testClassWriter, testMethod);
	}
}
