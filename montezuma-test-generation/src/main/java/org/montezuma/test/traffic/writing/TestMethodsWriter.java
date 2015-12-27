package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TestMethodsWriter {
	private final List<InvocationData>	invocationDataList;
	private final Class<?>							testClass;
	private final TestClassWriter				testClassWriter;
	private final boolean								amTestingTheStaticPart;
	public static int										globalVariableNumber	= 0; // TODO - this should probably go into the testClassWriter where it could be reset to 0 for every class.
	private final MockingStrategy				mockingStrategy;
	private final RenderersStrategy			renderersStrategy;
	private final int										instanceId;
	private final boolean								oneTestPerInvocation;
	private final ImportsContainer			importsContainer;
	private Deserialiser								deserialiser					= SerialisationFactory.getDeserialiser();
	private final static boolean				log										= true;

	public TestMethodsWriter(List<InvocationData> invocationDataList, final Class<?> testClass, int instanceId, TestClassWriter testClassWriter, List<String> dontMockRegexList, ImmutablesChecker immutablesChecker, ImportsContainer importsContainer) {
		this.invocationDataList = invocationDataList;
		this.testClass = testClass;
		this.testClassWriter = testClassWriter;
		this.amTestingTheStaticPart = (instanceId == 0);
		// TODO - extend this to the whole package/boundary if it's for "behavioural test writing", currently only possible
		// for "behaviouralCapture"
		dontMockRegexList.add(testClass.getCanonicalName());
		this.mockingStrategy = new MockingStrategy(dontMockRegexList);
		this.renderersStrategy = new RenderersStrategy();
		this.instanceId = instanceId;
		this.oneTestPerInvocation = immutablesChecker.isImmutable(testClass);
		this.importsContainer = importsContainer;
	}

	public List<TestMethod> buildTestMethods() throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		importsContainer.addImport(new Import(testClass.getCanonicalName()));

		List<TestMethod> testMethods = new ArrayList<>();

		int initialTestNumber = testClassWriter.testNumber;
		TestMethod currentTestMethod = null;
		boolean justInstantiated = false;
		for (InvocationData invocationData : invocationDataList) {
			String methodSignature = invocationData.signature;
			final int indexOfSeparatorBetweenMethodNameAndArgs = methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
			String methodName = methodSignature.substring(0, indexOfSeparatorBetweenMethodNameAndArgs);
			final String argTypesSubstring = methodSignature.substring(1 + indexOfSeparatorBetweenMethodNameAndArgs);
			final String[] argTypes = (argTypesSubstring.length() == 0 ? new String[] {} : argTypesSubstring.split(Common.ARGS_SEPARATOR));
			if (log)
				System.out.println("WRITING TEST - METHOD NAME: " + methodName);
			Object[] methodArgs = TrafficReader.getDeserialisedArgs(invocationData.serialisedArgs);
			final boolean isInitMethod = methodName.equals("<init>");
			boolean mustStartANewTestMethod =
					(isInitMethod || ((!justInstantiated /* || lastInstantiationExpectsException */) && ((amTestingTheStaticPart && (testClassWriter.testNumber == 0)) || this.oneTestPerInvocation)));
			if (mustStartANewTestMethod) {
				if (testClassWriter.testNumber > initialTestNumber) {
					testMethods.add(closeTestMethod(currentTestMethod));
				}
				testClassWriter.testNumber++;
				if (isInitMethod) {
					currentTestMethod = getNewTestMethodOpening(testClassWriter.testNumber);
					generateInstantiation(currentTestMethod, this.instanceId, methodArgs, argTypes, invocationData.argIDs, invocationData.calls);
					justInstantiated = true;
					continue; // Fetch the next invocation
				} else if (!justInstantiated) {
					if (amTestingTheStaticPart && (testClassWriter.testNumber == initialTestNumber + 1))
						currentTestMethod = getNewTestMethodOpening(testClassWriter.testNumber);
					else if (this.oneTestPerInvocation)
						currentTestMethod = currentTestMethod.cloneOpening("test" + testClassWriter.testNumber);
					else
						throw new IllegalStateException("At the moment this just can't happen.");
				} else
					throw new IllegalStateException("At the moment this just can't happen.");
				justInstantiated = true;
				if (isInitMethod)
					continue;
			}
			CodeChunk currentMethodPart = new CodeChunk(currentTestMethod);
			final List<CodeChunk> expectationChunks = buildExpectations(invocationData.calls, currentMethodPart);
			currentMethodPart.methodPartsBeforeLines.addAll(expectationChunks);
			final byte[] serialisedReturnValue = invocationData.serialisedReturnValue;
			ExpressionRenderer cutVariableOrClassNameRenderer = (amTestingTheStaticPart ? new ClassNameRenderer(this.testClass, importsContainer) : ExpressionRenderer.stringRenderer("cut"));
			StructuredTextRenderer instantiatedInvocationParametersRenderer =
					renderersStrategy.buildInvocationParameters(currentMethodPart, methodArgs, argTypes, invocationData.argIDs, importsContainer, mockingStrategy, testClassWriter);
			final ExpressionRenderer invocationRenderer = new StructuredTextRenderer("%s." + methodName + "(%s)", cutVariableOrClassNameRenderer, instantiatedInvocationParametersRenderer);
			// TODO - distinguish between methods that return 'void' and methods who actually returned 'null'. If the method
			// returns void, the recording aspect currently serialises 'null', which means 'serialisedReturnValue' is a
			// 'byte[] {0}'
			boolean invokedMethodReturnValueIsVoid = (serialisedReturnValue == null);
			if (invokedMethodReturnValueIsVoid) {
				final ExpressionRenderer expressionRenderer = new StructuredTextRenderer("%s;", invocationRenderer);
				currentMethodPart.addExpressionRenderer(expressionRenderer);
			} else {
				final int returnValueID = invocationData.returnValueID;
				// FIXME - add condition to if that, if the return value is not mocked, the assert should be "assertEquals" with
				// a different (made-up) identityHashCode, because mutable objects like Lists might have been changed by the
				// invoked method (the one under test, in this case), in which case the changes can't be detected by assertSame.
				// Mind you: all the objects held by the returned value might need to be handled the same way, recursively: new
				// identityHashCode, to be recreated independently

				Method invokedMethod = this.testClass.getDeclaredMethod(methodName, buildParameterTypes(argTypes));
				final Class<?> returnType = invokedMethod.getReturnType();
				int returnValueIdentityHascode = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode();
				final NewGeneratedVariableNameRenderer returnValueNameRenderer =
						new NewGeneratedVariableNameRenderer(returnValueIdentityHascode, returnType, importsContainer, currentMethodPart, "returned");
				if (!currentMethodPart.declaresOrCanSeeIdentityHashCode(returnValueIdentityHascode)) {
					VariableDeclarationRenderer returnValueDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", returnType, new ClassNameRenderer(returnType, importsContainer), returnValueNameRenderer, invocationRenderer);
					currentMethodPart.addExpressionRenderer(returnValueDeclarationRenderer);
					currentMethodPart.addDeclaredObject(returnValueIdentityHascode, returnValueDeclarationRenderer);
				}

				// final InitCodeChunk returnValueInitCodeChunk = createInitCodeChunk(returnValue, returnValueDeclaredType,
				// identityHashCode, "expected");
				// codeChunk.requiredInits.put(identityHashCode, returnValueInitCodeChunk);
				// return returnValueNameRenderer;
				// final ExpressionRenderer instantiationRenderer = new StructuredTextRenderer("assertSame(%s, %s);",
				// returnValueNameRenderer, invocationRenderer);

				// currentMethodPart.addExpressionRenderer(instantiationRenderer);
				// TODO - TOCHECK - that where it matters it asserts both Equals and Same. If it doesn't, it's caused by another "TOCHECK" change
				ObjectDeclarationScope objectDeclarationScope;
				final boolean shouldAssertSame = (objectDeclarationScope = currentTestMethod).declaresIdentityHashCode(returnValueID) || (objectDeclarationScope = this.testClassWriter).declaresIdentityHashCode(returnValueID);
				if (shouldAssertSame) {
					ExpressionRenderer expectedValueNameRenderer = new ExistingVariableNameRenderer(returnValueID, returnType, importsContainer, objectDeclarationScope);
					currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertSame"));
					currentMethodPart.addExpressionRenderer(new StructuredTextRenderer("assertSame(%s, %s);", expectedValueNameRenderer, returnValueNameRenderer));
				}
				// In any case:
				{
					Object returnValue = deserialiser.deserialise(serialisedReturnValue);
					final int expectedReturnValueID = /* testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); */ shouldAssertSame ? returnValueIdentityHascode : returnValueID;
					ExpressionRenderer expectedValueNameRenderer = buildExpectedReturnValue(currentMethodPart, returnValue, returnType, expectedReturnValueID, currentMethodPart);
					final ExpressionRenderer expressionRenderer;
					if (expectedValueNameRenderer instanceof NewVariableNameRenderer) {
						// TODO - when the returned values are primitive wrappers (instances of java.lang.Number descendants), cast
						// the first argument to their original class (the primitive or the wrapper/Object) basing on the return
						// value of the signature of the method corresponding to this 'cut' invocation
						// TODO - better check for "don't assertEquals if it's a mock"
						// The following 'if' condition means "don't assertEquals if it's a mock", but it definitely need
						// improvement! It mirrors the createInitCodeChunk() code's cases.
						if (returnType.isPrimitive() || returnType.isArray() || Number.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)
								|| Map.class.isAssignableFrom(returnType) || !(mockingStrategy.mustStub(returnValue) || mockingStrategy.shouldStub(returnType))) {
							currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertEquals"));
							expressionRenderer = new StructuredTextRenderer("assertEquals(%s, %s);", expectedValueNameRenderer, returnValueNameRenderer);
							currentMethodPart.addExpressionRenderer(expressionRenderer);
						}
					} else {
						currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertNull"));
						expressionRenderer = new StructuredTextRenderer("assertNull(%s);", invocationRenderer);
						currentMethodPart.addExpressionRenderer(expressionRenderer);
					}
				}
			}
			currentTestMethod.codeChunks.add(currentMethodPart);
			justInstantiated = false;
		}
		if (testClassWriter.testNumber > initialTestNumber) {
			testMethods.add(closeTestMethod(currentTestMethod));
		}

		return testMethods;
	}

	private TestMethod getNewTestMethodOpening(int testNumber) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		TestMethod currentTestMethod = new TestMethod(testClassWriter);
		final TestMethodOpening testMethodOpening = new TestMethodOpening("void", "test" + testNumber);
		testMethodOpening.annotations.add("@Test");
		testMethodOpening.modifiers.add("public");
		currentTestMethod.opening = testMethodOpening;
		return currentTestMethod;
	}

	protected void generateInstantiation(TestMethod currentTestMethod, int identityHashCode, Object[] methodArgs, String[] argTypes, int[] argIDs, Queue<CallInvocationData> calls) throws ClassNotFoundException, IOException, NoSuchMethodException {
		CodeChunk instantiationMethodPart = new CodeChunk(currentTestMethod);
		instantiationMethodPart.methodPartsBeforeLines.addAll(buildExpectations(calls, instantiationMethodPart));

		final StructuredTextRenderer invocationParametersRenderer =
				renderersStrategy.buildInvocationParameters(instantiationMethodPart, methodArgs, argTypes, argIDs, importsContainer, mockingStrategy, testClassWriter);

		final ClassNameRenderer classNameRenderer = new ClassNameRenderer(testClass, importsContainer);
		NewVariableNameRenderer cutVariableNameRenderer = new NewVariableNameRenderer(identityHashCode, testClass, importsContainer, instantiationMethodPart) { @Override protected String getName() { return "cut"; } };
		VariableDeclarationRenderer declarationRenderer = new VariableDeclarationRenderer(
				"final %s %s = new %s(%s);" + StructuredTextFileWriter.EOL, testClass, classNameRenderer, cutVariableNameRenderer, classNameRenderer, invocationParametersRenderer);
		instantiationMethodPart.addExpressionRenderer(declarationRenderer);
		instantiationMethodPart.addDeclaredObject(identityHashCode, declarationRenderer);

		currentTestMethod.instantiationMethodPart = instantiationMethodPart;
	}

	private List<CodeChunk> buildExpectations(Queue<CallInvocationData> calls, ObjectDeclarationScope objectDeclarationScope) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		List<CodeChunk> expectationParts = new ArrayList<>();

		for (CallInvocationData callData : calls) {
			if (!Modifier.isStatic(callData.modifiers) && ((callData.id == 0) && (!callData.signature.startsWith("<init>"))))
				continue; // This is a non-static invocation on a null pointer: don't mock - the NPE will be thrown naturally.

			Class<?> targetClazz = callData.targetClazz;
			if (targetClazz == null) { // static method invocation or constructor
				targetClazz = callData.declaringType;
			}
			// TODO - check if the target is a MustMock, but at the moment CallInvocationData does not serialise the target
			// class, so I can't determine if it should be a MustMock.
			if (mockingStrategy.shouldStub(targetClazz)) {
				expectationParts.add(getStrictExpectationPart(callData, objectDeclarationScope));
			}
		}

		return expectationParts;
	}

	private TestMethod closeTestMethod(TestMethod testMethod) {
		CodeChunk mainMethodPart = new CodeChunk(testMethod);
		mainMethodPart.addExpressionRenderer(ExpressionRenderer.stringRenderer("} // Closing test"));
		testMethod.closure = mainMethodPart;
		return testMethod;
	}

	private CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		this.testClassWriter.addImport("mockit.StrictExpectations");
		final StrictExpectationsCodeChunk codeChunk = new StrictExpectationsCodeChunk(objectDeclarationScope);
		String methodSignature = callData.signature;
		final int indexOfSeparatorBetweenMethodNameAndArgs = methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
		final String argTypesSubstring = methodSignature.substring(1 + indexOfSeparatorBetweenMethodNameAndArgs);
		final String[] argTypes = (argTypesSubstring.length() == 0 ? new String[] {} : argTypesSubstring.split(Common.ARGS_SEPARATOR));
		Class<?>[] parameterTypes = buildParameterTypes(argTypes);

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
		final NewGeneratedVariableNameRenderer mockedFieldNameRenderer = renderersStrategy.getStubbedFieldNameRenderer(targetClazzOrDeclaringType, importsContainer, testClassWriter, identityHashCode);
		// TODO - invoke addMock with a different Class<?> than targetClazzOrDeclaringType if targetClazzOrDeclaringType is
		// not visible (e.g.: private class), like we already do for expected return values.
		if (!testClassWriter.declaresIdentityHashCode(identityHashCode))
			renderersStrategy.addStub(isStaticMethod || isConstructorInvocation, identityHashCode, targetClazzOrDeclaringType, mockedFieldNameRenderer, importsContainer, testClassWriter);
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
										: new ExistingVariableNameRenderer(callData.id, declaringType, importsContainer, testClassWriter),
								ExpressionRenderer.stringRenderer(methodName),
								invocationParameters);
		final ExpressionRenderer timesExpressionRenderer = ExpressionRenderer.stringRenderer(" times = 1;");
		final byte[] serialisedReturnValue = callData.serialisedReturnValue;
		// TODO - implement behaviour of when a Throwable is thrown rather than a result returned: serialisedReturnValue is
		// null, but returnValueID and serialisedThrowable aren't. Return the throwable.
		final Class<?> returnType = (isConstructorInvocation ? targetClazzOrDeclaringType : ((Method) declaredMethod).getReturnType());
		final ExpressionRenderer resultExpressionRenderer =
				(serialisedReturnValue == null ? ExpressionRenderer.nullRenderer() : new StructuredTextRenderer(" result = %s;", buildExpectedReturnValue(
						codeChunk, serialisedReturnValue, returnType, callData.returnValueID, objectDeclarationScope)));
		codeChunk.addExpressionRenderer(new StructuredTextRenderer("%s%s%s", invocationExpressionRenderer, timesExpressionRenderer, resultExpressionRenderer));

		return codeChunk;
	}

	protected Class<?>[] buildParameterTypes(final String[] argTypes) throws ClassNotFoundException {
		Class<?>[] parameterTypes = new Class<?>[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			final String argTypeString = argTypes[i];
			Class<?> argClass = Common.primitiveClasses.get(argTypeString);
			if (argClass == null) {
				argClass = Class.forName(argTypeString);
			}
			parameterTypes[i] = argClass;
		}
		return parameterTypes;
	}

	private ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, byte[] serialisedReturnValue, Class<?> returnValueDeclaredType, int identityHashCode, ObjectDeclarationScope objectDeclarationScope) throws ClassNotFoundException, IOException {
		Object returnValue = deserialiser.deserialise(serialisedReturnValue);
		return buildExpectedReturnValue(codeChunk, returnValue, returnValueDeclaredType, identityHashCode, objectDeclarationScope);
	}

	private ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, Object returnValue, Class<?> returnValueDeclaredType, int identityHashCode, ObjectDeclarationScope objectDeclarationScope) throws ClassNotFoundException, IOException {
		if (returnValue == null) {
			return ExpressionRenderer.stringRenderer("null");
		}

		final Object arg = returnValue;
		final Class<?> argClass = returnValueDeclaredType;
		final int argID = identityHashCode;

		// Here I reuse a previous initialisation, to avoid replacing the existing one, which needs to be "preprocessed" for other objects to use it. NOT IDEAL.
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
		// invocation - is then cast by the cut to a more specific type and a method from that type is invoked. That would
		// be a good reason to use returnValueClass (returnValue.getClass()), but such class might not be visible (private
		// inner class).
		return new NewGeneratedVariableNameRenderer(identityHashCode, returnValueDeclaredType, importsContainer, returnValueInitCodeChunk, "expected");
	}

}