package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.PassThroughClassTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.utils.math.MonitoredClass;

public class PassThroughClassTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc = "This test is a sort of a duplicate of the BoundaryChecks tests, with the difference that the instance of the class invoked when exiting the boundaries is passed as a mocked parameter rather than directly instantiated (and mocked) from inside the boundaries.";
		CasesCommon.generateTestsFor(
				MonitoredClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), PassThroughClassTrafficRecorder.PASSTHROUGH_CLASS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
