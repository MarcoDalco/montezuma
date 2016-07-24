package org.montezuma.test.traffic.writing.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ClassVisibilityAndTypeOptimisationCaseTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import analysethis.privateclassreferencesandtypeoptimisation.ClassVisibilityAndTypeOptimisationCaseMainClass;

public class ClassVisibilityAndTypeOptimisationCaseTestWriter {
	/**
	 * This test checks a few things:
	 * - when an instance of a class that is not visible from this test class is returned and must be mocked, declared or cast,
	 *   - only a visible superclass or superinterface is used for the mock/declaration/casting
	 *   - the most specialised of the specifically required superclass or superinterface are used
	 *   - when a mocking framework cannot mock both the class and the superinterfaces required for testing, separate mocks are declared and used
	 *  The non-mocking version only has to check the return types.
	 *
	 *  Example:
	 *  ClassD extends ClassC, which extends ClassB, which extends ClassA. ClassD also implements InterfaceD and ClassB implements InterfaceB. ClassD is a private class, but the rest of the classes and interfaces are visible from the tests (public, protected or package-visible).
	 *  The Class Under Test has three methods returning an instance of ClassD, one declaring the return value as InterfaceD, one as InterfaceB and another one as ClassA. Mocking frameworks that can force mocks to implement specific interfaces should use a single mock object for all cases, declaring the mock of type ClassB and InterfaceD or ClassA, InterfaceB and InterfaceD.
	 *  Mocking frameworks that cannot force mocks to implement multiple interfaces should declare two mocks, one for InterfaceD and one for ClassB, as the latter both extends ClassA and implements InterfaceB.
	 *  In case the returned object is used in a subsequent invocation cast as ClassC, when a mocking framework can use a single mock, the mock must be declared of class ClassC and InterfaceD and when it can't, the two mocks should similarly be of ClassC and InterfaceD.
	 */

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= null;
		CasesCommon.generateTestsFor(
				ClassVisibilityAndTypeOptimisationCaseMainClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), ClassVisibilityAndTypeOptimisationCaseTrafficRecorder.CLASS_VISIBILITY_AND_TYPE_OPTIMISATION_CASE_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
