package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.TrafficReader;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;
import org.montezuma.test.traffic.writing.serialisation.SerialisationRendererFactory;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TestMethodsWriter {
	private final List<InvocationData>	invocationDataList;
	private final Class<?>							testClass;
	private final TestClassWriter				testClassWriter;
	private final boolean								amTestingTheStaticPart;
	public static int										globalVariableNumber	= 0;
	private static int									fakeIdentityHashCode	= 1000000;
	private List<String>								dontMockRegexList;
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
		this.dontMockRegexList = dontMockRegexList;
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
					generateInstantiation(currentTestMethod, methodArgs, argTypes, invocationData.argIDs, invocationData.calls);
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
			CodeChunk currentMethodPart = new CodeChunk();
			final List<CodeChunk> expectationChunks = buildExpectations(invocationData.calls);
			currentMethodPart.methodPartsBeforeLines.addAll(expectationChunks);
			final byte[] serialisedReturnValue = invocationData.serialisedReturnValue;
			ExpressionRenderer cutVariableOrClassNameRenderer = (amTestingTheStaticPart ? new ClassNameRenderer(this.testClass, importsContainer) : ExpressionRenderer.stringRenderer("cut"));
			StructuredTextRenderer instantiatedInvocationParametersRenderer = buildInvocationParameters(currentMethodPart, methodArgs, argTypes, invocationData.argIDs);
			final ExpressionRenderer invocationRenderer = new StructuredTextRenderer("%s." + methodName + "(%s)", cutVariableOrClassNameRenderer, instantiatedInvocationParametersRenderer);
			// TODO - distinguish between methods that return 'void' and methods who actually returned 'null'. If the method
			// returns void, the recording aspect currently serialises 'null', which means 'serialisedReturnValue' is a
			// 'byte[] {0}'
			boolean invokedMethodReturnValueIsVoid = (serialisedReturnValue == null);
			final ExpressionRenderer instantiationRenderer;
			if (invokedMethodReturnValueIsVoid) {
				instantiationRenderer = new StructuredTextRenderer("%s;", invocationRenderer);
			} else {
				final int returnValueID = invocationData.returnValueID;
				// FIXME - add condition to if that, if the return value is not mocked, the assert should be "assertEquals" with
				// a different (made-up) identityHashCode, because mutable objects like Lists might have been changed by the
				// invoked method (the one under test, in this case), in which case the changes can't be detected by assertSame.
				// Mind you: all the objects held by the returned value might need to be handled the same way, recursively: new
				// identityHashCode, to be recreated independently
				if (returnValueID == this.instanceId) {
					ExpressionRenderer returnValueNameRenderer = ExpressionRenderer.stringRenderer("cut");
					currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertSame"));
					instantiationRenderer = new StructuredTextRenderer("assertSame(%s, %s);", returnValueNameRenderer, invocationRenderer);
				} else {
					Method invokedMethod = this.testClass.getDeclaredMethod(methodName, buildParameterTypes(argTypes));
					ExpressionRenderer returnValueNameRenderer = buildExpectedReturnValue(currentMethodPart, serialisedReturnValue, invokedMethod.getReturnType(), returnValueID);
					if (returnValueNameRenderer instanceof VariableNameRenderer) {
						// TODO - when the returned values are primitive wrappers (instances of java.lang.Number descendants), cast
						// the first argument to their original class (the primitive or the wrapper/Object) basing on the return
						// value of the signature of the method corresponding to this 'cut' invocation
						currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertEquals"));
						instantiationRenderer = new StructuredTextRenderer("assertEquals(%s, %s);", returnValueNameRenderer, invocationRenderer);
					} else {
						currentMethodPart.requiredImports.addImport(new Import("org.junit.Assert", "assertNull"));
						instantiationRenderer = new StructuredTextRenderer("assertNull(%s);", invocationRenderer);
					}
				}
			}
			currentMethodPart.addExpressionRenderer(instantiationRenderer);
			currentTestMethod.codeChunks.add(currentMethodPart);
			justInstantiated = false;
		}
		if (testClassWriter.testNumber > initialTestNumber) {
			testMethods.add(closeTestMethod(currentTestMethod));
		}

		return testMethods;
	}

	private TestMethod getNewTestMethodOpening(int testNumber) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		TestMethod currentTestMethod = new TestMethod();
		final TestMethodOpening testMethodOpening = new TestMethodOpening("void", "test" + testNumber);
		testMethodOpening.annotations.add("@Test");
		testMethodOpening.modifiers.add("public");
		currentTestMethod.opening = testMethodOpening;
		return currentTestMethod;
	}

	protected void generateInstantiation(TestMethod currentTestMethod, Object[] methodArgs, String[] argTypes, int[] argIDs, Queue<CallInvocationData> calls) throws ClassNotFoundException, IOException, NoSuchMethodException {
		CodeChunk instantiationMethodPart = new CodeChunk();
		instantiationMethodPart.methodPartsBeforeLines.addAll(buildExpectations(calls));

		final StructuredTextRenderer invocationParametersRenderer = buildInvocationParameters(instantiationMethodPart, methodArgs, argTypes, argIDs);

		final ClassNameRenderer classNameRenderer = new ClassNameRenderer(testClass, importsContainer);
		instantiationMethodPart.addExpressionRenderer(new StructuredTextRenderer(
				"final %s cut = new %s(%s);" + StructuredTextFileWriter.EOL, classNameRenderer, classNameRenderer, invocationParametersRenderer));

		currentTestMethod.instantiationMethodPart = instantiationMethodPart;
	}

	private List<CodeChunk> buildExpectations(Queue<CallInvocationData> calls) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
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
			if (shouldMock(targetClazz)) {
				expectationParts.add(getStrictExpectationPart(callData));
			}
		}

		return expectationParts;
	}

	private TestMethod closeTestMethod(TestMethod testMethod) {
		CodeChunk mainMethodPart = new CodeChunk();
		mainMethodPart.addExpressionRenderer(ExpressionRenderer.stringRenderer("} // Closing test"));
		testMethod.closure = mainMethodPart;
		return testMethod;
	}

	private void buildList(InitCodeChunk maincodeChunk, List<Object> rebuiltRuntimeList, String[] listElementTypes, int[] listElementIDs, VariableNameRenderer listNameRenderer) {
		int i = 0;
		for (Iterator<?> runtimeObjectsIterator = rebuiltRuntimeList.iterator(); runtimeObjectsIterator.hasNext(); i++) {
			Object element = runtimeObjectsIterator.next();

			if (element == null) {
				maincodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(null);", listNameRenderer));
			} else {
				final Class<?> elementClass = (element instanceof MustMock ? ((MustMock) element).clazz : element.getClass());
				final int elementID = listElementIDs[i];

				final InitCodeChunk variableCodeChunk = createInitCodeChunk(element, elementClass, elementID, "given");
				maincodeChunk.requiredInits.put(elementID, variableCodeChunk);

				maincodeChunk.codeRenderers.add(new StructuredTextRenderer("%s.add(%s);", listNameRenderer, new VariableNameRenderer(elementID, elementClass, "given")));
			}
		}
	}

	private StructuredTextRenderer buildInvocationParameters(CodeChunk maincodeChunk, Object[] args, String[] argTypes, int[] argIDs) {

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

				final InitCodeChunk variableCodeChunk = createInitCodeChunk(arg, argClass, argID, "given");
				maincodeChunk.requiredInits.put(argID, variableCodeChunk);

				// TODO - consider argTypes[i] for potential cast to specify the correct signature in case the target class has
				// overloaded methods where one parameter is more specific in one method than the other, e.g.: valueOf(Number)
				// and valueOf(Long). It will require this method to take the Declaring Type as an extra parameter.
				// In the above process, consider the class type of each argument as given in the argTypes String array,
				// i.e.: int.class versus Integer.class
				expressionRenderers.add(new VariableNameRenderer(argID, argClass, "given"));
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

	private InitCodeChunk createInitCodeChunk(final Object arg, final Class<?> argClass, final int argID, final String variableNamePrefix) {
		return new InitCodeChunk(argID) {
			@Override
			public void generateRequiredInits() {
				// final VariableNameRenderer variableNameRenderer;
				// TODO - exclude the mocking case too, as it already adds the class to the imports
				if (!argClass.isPrimitive() && !argClass.isArray() && !argClass.getPackage().equals(Package.getPackage("java.lang"))) {
					requiredImports.addImport(new Import(argClass.getCanonicalName()));
				}
				// maincodeChunk.requiredInits.add(variableCodeChunk);
				if ((arg instanceof Number) || (arg instanceof Boolean)) {
					if (argClass.equals(BigDecimal.class)) {
						codeRenderers.add(new StructuredTextRenderer("final %s %s = %s;", new ClassNameRenderer(argClass, importsContainer), new VariableNameRenderer(argID, argClass, variableNamePrefix), new ExpressionRenderer() {
							@Override
							public String render() {
								return getBigDecimalInitialiser(arg);
							}
						}));
					} else {
						final ExpressionRenderer initExpressionRenderer;
						if (argClass.equals(int.class) || argClass.equals(Integer.class)) {
							initExpressionRenderer = new ExpressionRenderer() {
								@Override
								public String render() {
									return "" + arg;
								}
							};
						} else if (argClass.equals(long.class) || argClass.equals(Long.class)) {
							initExpressionRenderer = new ExpressionRenderer() {
								@Override
								public String render() {
									return arg + "L";
								}
							};
						} else if (argClass.equals(double.class) || argClass.equals(Double.class)) {
							initExpressionRenderer = new ExpressionRenderer() {
								@Override
								public String render() {
									return arg + "D";
								}
							};
						} else if (argClass.equals(boolean.class) || argClass.equals(Boolean.class)) {
							initExpressionRenderer = new ExpressionRenderer() {
								@Override
								public String render() {
									return "" + arg;
								}
							};
						} else {
							initExpressionRenderer = new ExpressionRenderer() {
								@Override
								public String render() {
									return arg + "TODO";
								}
							};
						}
						final String actualArgType = argClass.getCanonicalName();
						final String declaredArgClassName = (actualArgType.startsWith("java") ? argClass.getSimpleName() : actualArgType);
						codeRenderers.add(
								new StructuredTextRenderer("final " + declaredArgClassName + " %s = %s;",
										new VariableNameRenderer(argID, argClass, variableNamePrefix), initExpressionRenderer)
						);
					}
				} else if (argClass == String.class) {
					codeRenderers.add(
							new StructuredTextRenderer("final %s %s = \"%s\";",
									new ClassNameRenderer(argClass, importsContainer),
									new VariableNameRenderer(argID, argClass, variableNamePrefix),
									new ExpressionRenderer() {
										@Override
										public String render() {
											return ((String) arg).replaceAll("\n", "\\n").replaceAll("\r", "\\r").replaceAll("\\\\", "\\\\\\\\");
										}
									}
							)
					);
				} else if (argClass.isAssignableFrom(List.class) && argClass.getPackage().getName().startsWith("java.util")) {
					@SuppressWarnings("unchecked") final List<Object> rebuiltRuntimeList = (List<Object>) arg;
					final int listSize = rebuiltRuntimeList.size();
					String[] listElementTypes = new String[listSize];
					int[] listElementIDs = new int[listSize];
					int i = 0;
					for (Object element : rebuiltRuntimeList) {
						listElementTypes[i] = element.getClass().getCanonicalName();
						listElementIDs[i] = TestMethodsWriter.generateIdentityHashCode(); // TODO: store the real object ID?
						i++;
					}
					final ClassNameRenderer declaredClassNameRenderer = new ClassNameRenderer(argClass, importsContainer);
					final VariableNameRenderer listNameRenderer = new VariableNameRenderer(argID, argClass, variableNamePrefix);
					final ClassNameRenderer actualClassNameRenderer = new ClassNameRenderer(arg.getClass(), importsContainer);
					codeRenderers.add(new StructuredTextRenderer("final %s %s = new %s();", declaredClassNameRenderer, listNameRenderer, actualClassNameRenderer));
					buildList(this, rebuiltRuntimeList, listElementTypes, listElementIDs, listNameRenderer);
				} else if (argClass.isAssignableFrom(Set.class)) {} else if (argClass.isAssignableFrom(Map.class)) {} else if (argClass.isArray()) {
					final Object[] serialisedObjectsArray = (Object[]) arg;
					final Object[] rebuiltRuntimeArray = new Object[serialisedObjectsArray.length];
					for (int i = 0; i < rebuiltRuntimeArray.length; i++) {
						try {
							rebuiltRuntimeArray[i] = deserialiser.deserialise((byte[]) serialisedObjectsArray[i]);
						}
						catch (ClassNotFoundException | IOException e) {
							throw new RuntimeException(e);
						}
					}
					final Class<?> arrayBaseType = argClass.getComponentType();
					String[] arrayArgTypes = new String[rebuiltRuntimeArray.length];
					int[] arrayArgIDs = new int[rebuiltRuntimeArray.length];
					final String arrayBaseTypeCanonicalName = arrayBaseType.getCanonicalName();
					for (int l = 0; l < rebuiltRuntimeArray.length; l++) {
						arrayArgTypes[l] = arrayBaseTypeCanonicalName;
						arrayArgIDs[l] = TestMethodsWriter.generateIdentityHashCode(); // TODO: store the real object ID?
					}
					StructuredTextRenderer arrayObjectsRenderer = buildInvocationParameters(this, rebuiltRuntimeArray, arrayArgTypes, arrayArgIDs);
					final ClassNameRenderer classNameRenderer = new ClassNameRenderer(argClass, importsContainer);
					codeRenderers.add(new StructuredTextRenderer(
							"final %s %s = new %s {%s};", classNameRenderer, new VariableNameRenderer(argID, argClass, variableNamePrefix), classNameRenderer, arrayObjectsRenderer));
				} else {
					// Using mocks:
					if (mustMock(arg) || shouldMock(argClass)) {
						addMock(argID, argClass, getMockedFieldNameRenderer(argClass, argID));
					} else {
						codeRenderers.add(addRealParameter(this, argClass, arg, argID));
					}
				}
			}
		};
	}

	private StructuredTextRenderer addRealParameter(CodeChunk codeChunk, Class<?> argClass, Object arg, int argID) {
		final ClassNameRenderer classNameRenderer = new ClassNameRenderer(argClass, importsContainer);
		return new StructuredTextRenderer("final %s %s = (%s) %s;", classNameRenderer, new VariableNameRenderer(argID, argClass, "given"), classNameRenderer, getDeserialisationRenderer(codeChunk, arg));
	}

	private ExpressionRenderer getDeserialisationRenderer(CodeChunk codeChunk, Object object) {
		return SerialisationRendererFactory.getSerialisationRenderer().getDeserialisationCodeChunkFor(codeChunk, object, importsContainer);
	}

	private CodeChunk getStrictExpectationPart(CallInvocationData callData) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		this.testClassWriter.addImport("mockit.StrictExpectations");
		final StrictExpectationsCodeChunk codeChunk = new StrictExpectationsCodeChunk();
		String methodSignature = callData.signature;
		final int indexOfSeparatorBetweenMethodNameAndArgs = methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
		final String argTypesSubstring = methodSignature.substring(1 + indexOfSeparatorBetweenMethodNameAndArgs);
		final String[] argTypes = (argTypesSubstring.length() == 0 ? new String[] {} : argTypesSubstring.split(Common.ARGS_SEPARATOR));
		Class<?>[] parameterTypes = buildParameterTypes(argTypes);

		String methodName = methodSignature.substring(0, indexOfSeparatorBetweenMethodNameAndArgs);
		final Class<?> declaringType = callData.declaringType;
		final boolean isConstructorInvocation = methodName.equals("<init>");
		Class<?> targetClazzOrDeclaringType = callData.targetClazz;
		targetClazzOrDeclaringType = (targetClazzOrDeclaringType == null ? callData.declaringType : targetClazzOrDeclaringType);
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
		final int identityHashCode = isStaticMethod ? TestMethodsWriter.generateIdentityHashCodeForStaticClass(declaringType) : id;
		final VariableNameRenderer mockedFieldNameRenderer = getMockedFieldNameRenderer(targetClazzOrDeclaringType, identityHashCode);
		// TODO - invoke addMock with a different Class<?> than targetClazzOrDeclaringType if targetClazzOrDeclaringType is
		// not visible (e.g.: private class), like we already do for expected return values.
		addMock(identityHashCode, targetClazzOrDeclaringType, mockedFieldNameRenderer);
		Object[] methodArgs = TrafficReader.getDeserialisedArgs(callData.serialisedArgs);
		final StructuredTextRenderer invocationParameters = buildInvocationParameters(codeChunk, methodArgs, argTypes, callData.argIDs);
		final ExpressionRenderer invocationExpressionRenderer =
				isConstructorInvocation ? new StructuredTextRenderer("new %s(%s);", new ClassNameRenderer(declaringType, importsContainer), invocationParameters) : new StructuredTextRenderer("%s.%s(%s);", isStaticMethod
						? new ClassNameRenderer(declaringType, importsContainer) : new VariableNameRenderer(callData.id, targetClazzOrDeclaringType, "given"), ExpressionRenderer.stringRenderer(methodName), invocationParameters);
		final ExpressionRenderer timesExpressionRenderer = ExpressionRenderer.stringRenderer(" times = 1;");
		final byte[] serialisedReturnValue = callData.serialisedReturnValue;
		// TODO - implement behaviour of when a Throwable is thrown rather than a result returned: serialisedReturnValue is
		// null, but returnValueID and serialisedThrowable aren't. Return the throwable.
		final Class<?> returnType = (isConstructorInvocation ? targetClazzOrDeclaringType : ((Method) declaredMethod).getReturnType());
		final ExpressionRenderer resultExpressionRenderer =
				(serialisedReturnValue == null ? ExpressionRenderer.nullRenderer() : new StructuredTextRenderer(" result = %s;", buildExpectedReturnValue(
						codeChunk, serialisedReturnValue, returnType, callData.returnValueID)));
		StructuredTextRenderer structuredTextRenderer = new StructuredTextRenderer("%s%s%s", invocationExpressionRenderer, timesExpressionRenderer, resultExpressionRenderer);
		codeChunk.addExpressionRenderer(structuredTextRenderer);

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

	private boolean mustMock(final Object arg) {
		return (arg instanceof MustMock);
	}

	private boolean shouldMock(final Class<?> targetClazz) {
		if (targetClazz == testClass) {
			return false;
		}

		boolean shouldMock = true;
		for (String dontMockPattern : dontMockRegexList) {
			if (targetClazz.getCanonicalName().matches(dontMockPattern)) {
				shouldMock = false;
				break;
			}
		}
		return shouldMock;
	}

	private VariableNameRenderer getMockedFieldNameRenderer(Class<?> clazz, int id) {
		return new VariableNameRenderer(id, clazz, "mocked");
	}

	private void addMock(int identityHashCode, Class<?> argClass, final VariableNameRenderer variableNameRenderer) {
		// TODO - add imports to an "ImportContainer" instead of the testClassWriter
		// TODO - add mocks to a "(Mocked)FieldContainer" instead of the testClassWriter
		// TODO - get the argClass simpleName lazily from the ImportContainer
		// TODO - use @Injectable for stubbing instances, but @Mocked for stubbing static methods.
		testClassWriter.addImport("mockit.Mocked");
		testClassWriter.addImport(argClass.getCanonicalName());
		testClassWriter.addField(identityHashCode, new StructuredTextRenderer("@Mocked private %s %s;", new ClassNameRenderer(argClass, importsContainer), variableNameRenderer));
	}

	private String getBigDecimalInitialiser(Object arg) {
		final String bigIntInit;
		if (BigDecimal.ZERO.equals(arg)) {
			bigIntInit = "BigDecimal.ZERO";
		} else if (BigDecimal.ONE.equals(arg)) {
			bigIntInit = "BigDecimal.ONE";
		} else if (BigDecimal.TEN.equals(arg)) {
			bigIntInit = "BigDecimal.TEN";
		} else {
			// TODO - to be improved, constructing it exactly equivalent to the original
			bigIntInit = "new BigDecimal(\"" + arg + "\")";
		}
		return bigIntInit;
	}

	private ExpressionRenderer buildExpectedReturnValue(CodeChunk codeChunk, byte[] serialisedReturnValue, Class<?> returnValueDeclaredType, int identityHashCode) throws ClassNotFoundException, IOException {
		Object returnValue = deserialiser.deserialise(serialisedReturnValue);
		if (returnValue == null) {
			return ExpressionRenderer.stringRenderer("null");
		}
		// final Class<? extends Object> returnValueClass = (returnValue instanceof MustMock ? ((MustMock)
		// returnValue).clazz : returnValue.getClass());
		// TODO - To be checked with downcast invocations, i.e. when the object - say it's returned by an expected
		// invocation - is then cast by the cut to a more specific type and a method from that type is invoked. That would
		// be a good reason to use returnValueClass (returnValue.getClass()), but such class might not be visible (private
		// inner class).
		final VariableNameRenderer returnValueNameRenderer = new VariableNameRenderer(identityHashCode, returnValueDeclaredType, "expected");

		final InitCodeChunk returnValueInitCodeChunk = createInitCodeChunk(returnValue, returnValueDeclaredType, identityHashCode, "expected");
		codeChunk.requiredInits.put(identityHashCode, returnValueInitCodeChunk);
		return returnValueNameRenderer;
	}

	// TODO - check all the calling methods, to see where instance detection can be improved (or rather "introduced"!!)
	private static int generateIdentityHashCode() {
		return fakeIdentityHashCode++;
	}

	private static int generateIdentityHashCodeForStaticClass(Class<?> clazz) {
		return System.identityHashCode(clazz);
	}

}
