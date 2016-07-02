package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.serialisers.Deserialiser;

import java.io.IOException;
import java.util.List;

public interface MockingFramework {

	void addStub(boolean isStaticStub, boolean isConstructorInvocation, int identityHashCode, Class<?> argClass, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, TestClassWriter testClassWriter, CodeChunk codeChunk) throws ClassNotFoundException;

	CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException;

	String getRunwithClassName();

	String getRunwithClassCanonicalName();

	boolean canStubMultipleTypeWithOneStub();

	List<ExpressionRenderer> getStrictExpectationsRenderers(List<ExpressionRenderer> codeRenderers);

}
