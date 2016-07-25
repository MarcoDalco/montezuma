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
import java.util.Collections;
import java.util.List;

public class JMockitFramework extends AbstractMockingFramework implements MockingFramework {

	@Override
	public CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException {
		testClassWriter.addImport("mockit.StrictExpectations");
		return super.getStrictExpectationPart(callData, objectDeclarationScope, testClassWriter, testMethod, renderersStrategy, importsContainer, mockingStrategy, deserialiser);
	}

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
		return false;
	}

	@Override
	public void addStub(boolean isStaticStub, boolean isConstructorInvocation, int identityHashCode, Class<?> declaredClass, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, TestMethod testMethod, CodeChunk codeChunk) {
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
		if (testMethod.declaresIdentityHashCode(identityHashCode, declaredClass))
			return;
		VariableDeclarationRenderer variableDeclarationRenderer = new VariableDeclarationRenderer(annotation + ""
			+ " %s %s", identityHashCode, "mocked", declaredClass, importsContainer, ComputableClassNameRendererPlaceholder.instance, VariableDeclarationRenderer.NewVariableNameRendererPlaceholder.instance, null);
		testMethod.addParameter(identityHashCode, variableDeclarationRenderer);
	}

	protected void writeExpectation(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, TestMethod testMethod, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser, final StrictExpectationsCodeChunk codeChunk, String methodName, final Class<?> declaringType, final boolean isConstructorInvocation, final Executable declaredMethod, final boolean isStaticMethod, final StructuredTextRenderer invocationParameters, final byte[] serialisedReturnValue) throws ClassNotFoundException, IOException {
		final ExpressionRenderer invocationExpressionRenderer =
				isConstructorInvocation ?
						new StructuredTextRenderer("new %s(%s)",
								new ClassNameRenderer(declaringType, importsContainer),
								invocationParameters)
						: new StructuredTextRenderer("%s.%s(%s)",
								isStaticMethod ?
										new ClassNameRenderer(declaringType, importsContainer)
										: new ExistingVariableNameRenderer(callData.id, /* TO CHECK - it might need to be the targetClass or its most visible superclass. Perhaps declaredClass */ declaringType, importsContainer, testMethod),
								ExpressionRenderer.stringRenderer(methodName),
								invocationParameters);
		final ExpressionRenderer timesExpressionRenderer = ExpressionRenderer.stringRenderer(" times = 1");
		// TODO - implement behaviour of when a Throwable is thrown rather than a result returned: serialisedReturnValue is
		// null, but returnValueID and serialisedThrowable aren't. Return the throwable.
		final Class<?> returnType = (isConstructorInvocation ? declaringType : ((Method) declaredMethod).getReturnType());
		final ExpressionRenderer resultExpressionRenderer =
				(serialisedReturnValue == null ? ExpressionRenderer.nullRenderer() : new StructuredTextRenderer(" result = %s", buildExpectedReturnValue(
						codeChunk, serialisedReturnValue, returnType, callData.returnValueID, objectDeclarationScope, deserialiser, testClassWriter, testMethod, renderersStrategy, importsContainer, mockingStrategy)));
		codeChunk.addExpressionRenderer(new StructuredTextRenderer("%s;%s;%s;", invocationExpressionRenderer, timesExpressionRenderer, resultExpressionRenderer));
	}

	@Override
	public List<ExpressionRenderer> getStrictExpectationsRenderers(List<ExpressionRenderer> codeRenderers) {
		final StringBuffer text = new StringBuffer("new StrictExpectations() {{");
		text.append(StructuredTextFileWriter.EOL);
		for (int i = 0; i < codeRenderers.size(); i++) {
			text.append(StructuredTextFileWriter.INDENTATION_UNIT + "%s");
			text.append(StructuredTextFileWriter.EOL);
		}
		text.append("}};");
		text.append(StructuredTextFileWriter.EOL);

		return Collections.singletonList(new StructuredTextRenderer(text.toString(), codeRenderers.toArray(new ExpressionRenderer[codeRenderers.size()])));
	}

	@Override
	protected ObjectDeclarationScope getStubDeclarationScope(ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter) {
		return testClassWriter;
	}
}
