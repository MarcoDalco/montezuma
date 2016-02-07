package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.CallInvocationData;
import org.montezuma.test.traffic.serialisers.Deserialiser;

import java.io.IOException;

public interface MockingFramework {

	VariableDeclarationRenderer addStub(boolean isStaticStub, int identityHashCode, Class<?> argClass, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, TestClassWriter testClassWriter);

	CodeChunk getStrictExpectationPart(CallInvocationData callData, ObjectDeclarationScope objectDeclarationScope, TestClassWriter testClassWriter, RenderersStrategy renderersStrategy, ImportsContainer importsContainer, MockingStrategy mockingStrategy, Deserialiser deserialiser) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException;

	String getRunwithClassName();

	String getRunwithClassCanonicalName();

	boolean canStubMultipleTypeWithOneStub();

}
