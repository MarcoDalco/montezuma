package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksWithStateChangeinBothCallForthAndCallBackTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.B;

public class BoundaryChecksWithStateChangeInBothCallForthAndCallBackTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc = "This test checks that a callback into the 'boundaries' (class C) is performed, rather than mocked. That is in addition to the normal BoundaryChecks test, (where this test checks that Montezuma can define a set of classes (B and C) for testing, and that method invocations across the boundaries (A to B and C to D) get captured and tested (A to B) or mocked/stubbed (C to D), but internal invocations (B to C) are ignored).";
		CasesCommon.generateTestsFor(
				B.class, TrafficToUnitTestsWriter.getDontMockClasses(), BoundaryChecksWithStateChangeinBothCallForthAndCallBackTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
