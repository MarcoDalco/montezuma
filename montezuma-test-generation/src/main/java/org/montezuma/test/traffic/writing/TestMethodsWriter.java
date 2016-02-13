package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer.ComputableClassNameRendererPlaceholder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
			currentTestMethod.codeChunks.add(currentMethodPart);

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

				Method invokedMethod = this.testClass.getDeclaredMethod(methodName, ReflectionUtils.buildParameterTypes(argTypes));
				final Class<?> returnType = invokedMethod.getReturnType();
				int returnValueIdentityHascode = testClassWriter.identityHashCodeGenerator.generateIdentityHashCode();
//				final NewGeneratedVariableNameRenderer returnValueNameRenderer =
//						new NewGeneratedVariableNameRenderer(returnValueIdentityHascode, returnType, importsContainer, currentMethodPart, "returned");
				if (!currentMethodPart.declaresOrCanSeeIdentityHashCode(returnValueIdentityHascode, returnType)) {
					VariableDeclarationRenderer returnValueDeclarationRenderer = new VariableDeclarationRenderer("final %s %s = %s;", returnValueIdentityHascode, currentMethodPart, "returned", returnType, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, invocationRenderer);
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
				final boolean shouldAssertSame = (objectDeclarationScope = currentTestMethod).declaresIdentityHashCode(returnValueID, returnType) || (objectDeclarationScope = this.testClassWriter).declaresIdentityHashCode(returnValueID, returnType);
				if (shouldAssertSame) {
					ExpressionRenderer expectedValueNameRenderer = new ExistingVariableNameRenderer(returnValueID, returnType, importsContainer, objectDeclarationScope);
					currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertSame"));
					currentMethodPart.addExpressionRenderer(new StructuredTextRenderer("assertSame(%s, %s);", expectedValueNameRenderer, new ExistingVariableNameRenderer(returnValueIdentityHascode, returnType, importsContainer, currentMethodPart)));
				}
				// In any case:
				{
					Object returnValue = deserialiser.deserialise(serialisedReturnValue);
					if (returnValue == null) {
						currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertNull"));
						final ExpressionRenderer expressionRenderer;
						expressionRenderer = new StructuredTextRenderer("assertNull(%s);", invocationRenderer);
						currentMethodPart.addExpressionRenderer(expressionRenderer);
					} else {
						if (returnType.isPrimitive() || returnType.isArray() || Number.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)
								|| Map.class.isAssignableFrom(returnType) || !(mockingStrategy.mustStub(returnValue) || mockingStrategy.shouldStub(returnType))) {
							final int expectedReturnValueID = /* testClassWriter.identityHashCodeGenerator.generateIdentityHashCode(); */ shouldAssertSame ? returnValueIdentityHascode : returnValueID;
							VariableNameRenderer expectedValueNameRenderer = renderersStrategy.buildExpectedReturnValue(currentMethodPart, returnValue, returnType, expectedReturnValueID, currentMethodPart, importsContainer, mockingStrategy, renderersStrategy, testClassWriter);

							// TODO - when the returned values are primitive wrappers (instances of java.lang.Number descendants), cast
							// the first argument to their original class (the primitive or the wrapper/Object) basing on the return
							// value of the signature of the method corresponding to this 'cut' invocation
							// TODO - better check for "don't assertEquals if it's a mock"
							// The following 'if' condition means "don't assertEquals if it's a mock", but it definitely need
							// improvement! It mirrors the createInitCodeChunk() code's cases.
							currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertEquals"));
							final ExpressionRenderer expressionRenderer = new StructuredTextRenderer("assertEquals(%s, %s);", expectedValueNameRenderer, new ExistingVariableNameRenderer(returnValueIdentityHascode, returnType, importsContainer, currentMethodPart));
							currentMethodPart.addExpressionRenderer(expressionRenderer);
						}
					}
				}
			}
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

		VariableDeclarationRenderer declarationRenderer = new VariableDeclarationRenderer(
				"final %s %s = new %s(%s);" + StructuredTextFileWriter.EOL, identityHashCode, instantiationMethodPart, new NewVariableNameRenderer(identityHashCode, testClass) {
					@Override public String render() { return getName(testClass); }
					@Override protected String getName(Class<?> desiredClass) { return "cut"; }
					}, testClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, ComputableClassNameRendererPlaceholder.instance, invocationParametersRenderer);
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
				expectationParts.add(MockingFrameworkFactory.getMockingFramework().getStrictExpectationPart(callData, objectDeclarationScope, testClassWriter, renderersStrategy, importsContainer, mockingStrategy, deserialiser));
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

}