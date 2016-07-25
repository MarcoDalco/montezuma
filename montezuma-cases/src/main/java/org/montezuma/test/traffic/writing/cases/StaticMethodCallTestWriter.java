package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.StaticMethodCallTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.staticmethods.ClassWithStaticMethods;

public class StaticMethodCallTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This case tests the invocation of static and non-static methods both inwards and outwards (in respect to the capture boundaries), and that they can be mocked from within the same test class.";
		CasesCommon.generateTestsFor(
				ClassWithStaticMethods.class, TrafficToUnitTestsWriter.getDontMockClasses(), StaticMethodCallTrafficRecorder.STATIC_METHOD_CALL_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
