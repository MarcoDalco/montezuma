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
		CasesCommon.generateTestsFor(
				ClassWithStaticMethods.class, TrafficToUnitTestsWriter.getDontMockClasses(), StaticMethodCallTrafficRecorder.STATIC_METHOD_CALL_RECORDING_SUBDIR, CasesCommon.parseArguments(args));
	}

}
