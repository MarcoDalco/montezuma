package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.captureboundarychecks.EntryClassToAnalyse;

public class BoundaryChecksTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc = "This test checks that Montezuma can define a set of classes (EntryClassToAnalyse and SecondClassToAnalyse) for testing, and that method invocations across the boundaries (ClassEnteringBoundary to EntryClassToAnalyse and SecondClassToAnalyse to ClassAfterBoundaryExit) get captured and tested (ClassEnteringBoundary to EntryClassToAnalyse) or mocked/stubbed (SecondClassToAnalyse to ClassAfterBoundaryExit), but internal invocations (EntryClassToAnalyse to SecondClassToAnalyse) are ignored.";
		CasesCommon.generateTestsFor(
				EntryClassToAnalyse.class, TrafficToUnitTestsWriter.getDontMockClasses(), BoundaryChecksTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
