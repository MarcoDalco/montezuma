package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksCallbackTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.B;

public class BoundaryChecksCallbackTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc = "This test checks that a callback into the 'boundaries' (class C) is performed, rather than mocked; it does not do it effectively, but only checks the returned value, which is not a good indication, and there is a more specific test doing it more effectively (and currently failing), which checks if code in C's callback is actually run. That is in addition to the normal BoundaryChecks test, (where this test checks that Montezuma can define a set of classes (B and C) for testing, and that method invocations across the boundaries (A to B and C to D) get captured and tested (A to B) or mocked/stubbed (C to D), but internal invocations (B to C) are ignored).";
		CasesCommon.generateTestsFor(
			B.class, TrafficToUnitTestsWriter.getDontMockClasses(), BoundaryChecksCallbackTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
		}

}
