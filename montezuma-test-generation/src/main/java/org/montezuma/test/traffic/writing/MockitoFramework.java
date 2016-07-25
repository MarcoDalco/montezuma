package org.montezuma.test.traffic.writing;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.writing.VariableDeclarationRenderer.ComputableClassNameRendererPlaceholder;

public class MockitoFramework extends AbstractMockingFramework implements MockingFramework {
	private static final String PREPARE_FOR_TEST_ANNOTATION_CANONICAL_NAME = "org.powermock.core.classloader.annotations.PrepareForTest";

	@Override
	public CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		testClassWriter.addImport("org.powermock.api.mockito.PowerMockito", "when");
		testClassWriter.addImport("org.mockito.stubbing.OngoingStubbing", "*");
		return super.getStrictExpectationPart(callData, objectDeclarationScope, testClassWriter, testMethod, renderersStrategy, importsContainer, mockingStrategy, deserialiser);
	}

	@Override
	public void addStub(boolean isStaticStub, boolean isConstructorInvocation, int identityHashCode, Class<?> declaredClass, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, TestMethod testMethod, CodeChunk codeChunk) throws ClassNotFoundException {
		// TODO - add mocks to a "(Stubbed)FieldContainer" instead of the testClassWriter
		// TODO - get the argClass simpleName lazily from the ImportContainer
		if (isStaticStub && !isConstructorInvocation) {
			importsContainer.addImport(new Import("org.powermock.api.mockito.PowerMockito", "mockStatic"));
		} else {
			importsContainer.addImport(new Import("org.powermock.api.mockito.PowerMockito", "mock"));
			if (isConstructorInvocation) {
				importsContainer.addImport(new Import("org.powermock.api.mockito.PowerMockito", "whenNew"));
			}
		}
		importsContainer.addImport(new Import(declaredClass.getCanonicalName()));
		if (codeChunk.declaresIdentityHashCode(identityHashCode, declaredClass))
			return;

		if (isStaticStub && !isConstructorInvocation) {
			CodeChunk staticMockoedChunk = new CodeChunk(codeChunk);
			staticMockoedChunk.codeRenderers.add(
					new StructuredTextRenderer("mockStatic(%s.class);", new ClassNameRenderer(declaredClass, importsContainer)));
			codeChunk.methodPartsBeforeLines.add(staticMockoedChunk);
		} else {
			VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer("%s %s = %s;", identityHashCode, "mocked", declaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, new StructuredTextRenderer("mock(%s.class)", new ClassNameRenderer(declaredClass, importsContainer)));
			InitCodeChunk initCodeChunk = new InitCodeChunk(identityHashCode, codeChunk) {
				@Override
				public void generateRequiredInits() {
					codeRenderers.add(variableDeclarationRenderer);
					addDeclaredObject(identityHashCode, variableDeclarationRenderer);
				}
			};
			codeChunk.methodPartsBeforeLines.add(initCodeChunk);
			initCodeChunk.generateRequiredInits();
			codeChunk.addDeclaredObject(identityHashCode, variableDeclarationRenderer);
		}
	}

	@Override
	protected void writeExpectation(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser, StrictExpectationsCodeChunk codeChunk, String methodName, Class<?> declaringType, boolean isConstructorInvocation, Executable declaredMethod, boolean isStaticMethod, StructuredTextRenderer invocationParameters, byte[] serialisedReturnValue, Class<?> invokingClass) throws ClassNotFoundException, IOException {
		ClassNameRenderer classNameRenderer = null;
		final ExpressionRenderer invocationExpressionRenderer;
		if (isConstructorInvocation) {
			invocationExpressionRenderer =
					new StructuredTextRenderer("whenNew(%s.class)" + (((invocationParameters.expressionRenderers == null) || (invocationParameters.expressionRenderers.length == 0)) ? ".withNoArguments()" : ".withArguments(%s)"),
							classNameRenderer = new ClassNameRenderer(declaringType, importsContainer),
							invocationParameters);
			codeChunk.declaredThrowables.add(Exception.class);
		} else {
			invocationExpressionRenderer =
					new StructuredTextRenderer(serialisedReturnValue == null ? "%s.%s(%s)" : "when(%s.%s(%s))",
							isStaticMethod ?
									classNameRenderer = new ClassNameRenderer(declaringType, importsContainer)
									: new ExistingVariableNameRenderer(callData.id, /* TO CHECK - it might need to be the targetClass or its most visible superclass. Perhaps declaredClass */ declaringType, importsContainer, codeChunk),
							ExpressionRenderer.stringRenderer(methodName),
							invocationParameters);
			if (!isStaticMethod)
				codeChunk.declaresIdentityHashCode(callData.id, declaringType);
		}
		if (isConstructorInvocation || isStaticMethod) {
			testClassWriter.addImport(PREPARE_FOR_TEST_ANNOTATION_CANONICAL_NAME);
			StructuredTextRenderer annotationRenderer = (StructuredTextRenderer)testClassWriter.getAnnotation(PREPARE_FOR_TEST_ANNOTATION_CANONICAL_NAME);
			if (annotationRenderer == null) {
				ClassNameRenderer invokingClassNameRenderer = new ClassNameRenderer(invokingClass, importsContainer);
				ExpressionRenderer parametersRenderer = new StructuredTextRenderer("%s.class", invokingClassNameRenderer);
				annotationRenderer = new StructuredTextRenderer("@PrepareForTest({%s})", parametersRenderer);
			} else {
				// Add a renderer to annotationRenderer
				StructuredTextRenderer parametersRenderer = (StructuredTextRenderer)annotationRenderer.getMasterRenderers()[0];
				parametersRenderer.formattedText = parametersRenderer.formattedText + ", %s.class";
				ExpressionRenderer[] masterExpressionRenderers = parametersRenderer.masterExpressionRenderers;
				ExpressionRenderer [] newExpressionRenderers = new ExpressionRenderer[masterExpressionRenderers.length + 1];
				System.arraycopy(masterExpressionRenderers, 0, newExpressionRenderers, 0, masterExpressionRenderers.length);
				newExpressionRenderers[newExpressionRenderers.length - 1] = classNameRenderer;
				parametersRenderer.masterExpressionRenderers = newExpressionRenderers;
			}
			testClassWriter.addAnnotation(PREPARE_FOR_TEST_ANNOTATION_CANONICAL_NAME, annotationRenderer);
		}
		// TODO - implement behaviour of when a Throwable is thrown rather than a result returned: serialisedReturnValue is
		// null, but returnValueID and serialisedThrowable aren't. Return the throwable.
		final Class<?> returnType = (isConstructorInvocation ? declaringType : ((Method) declaredMethod).getReturnType());
		final ExpressionRenderer expectationExpressionRenderer;
		if (serialisedReturnValue == null) {
			expectationExpressionRenderer = new StructuredTextRenderer("%s;", invocationExpressionRenderer);
		} else {
			final ExpressionRenderer resultExpressionRenderer = buildExpectedReturnValue(
							codeChunk, serialisedReturnValue, returnType, callData.returnValueID, objectDeclarationScope, deserialiser, testClassWriter, testMethod, renderersStrategy, importsContainer, mockingStrategy);
			codeChunk.declaresIdentityHashCode(callData.returnValueID, returnType); // this is required for increasing the number of references to the return value, so that it's not inlined if referenced more than once
			expectationExpressionRenderer = new StructuredTextRenderer("%s.thenReturn(%s);", invocationExpressionRenderer, resultExpressionRenderer);
		}
		codeChunk.addExpressionRenderer(expectationExpressionRenderer);
	}

	@Override
	public String getRunwithClassName() {
		return "PowerMockRunner";
	}

	@Override
	public String getRunwithClassCanonicalName() {
		return "org.powermock.modules.junit4.PowerMockRunner";
	}

	@Override
	public boolean canStubMultipleTypeWithOneStub() {
		return false;
		// return true; - it can with Mockito.mock(Foo.class, withSettings().extraInterfaces(Bar.class));
	}

	@Override
	public List<ExpressionRenderer> getStrictExpectationsRenderers(List<ExpressionRenderer> codeRenderers) {
		final StringBuffer text = new StringBuffer();
		for (int i = 0; i < codeRenderers.size(); i++) {
			text.append("%s"); // void
			text.append(StructuredTextFileWriter.EOL);
		}

		return Collections.singletonList(new StructuredTextRenderer(text.toString(), codeRenderers.toArray(new ExpressionRenderer[codeRenderers.size()])));
	}

	@Override
	protected ObjectDeclarationScope getStubDeclarationScope(ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter) {
		return objectDeclarationScope;
	}

}
