package org.montezuma.test.traffic.writing.cases;

import analysethis.staticmethods.ClassWithStaticMethods;
import analysethis.superclasscall.withstate.SomeClass;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.StaticMethodCallTrafficRecorder;
import org.montezuma.test.traffic.recording.cases.SuperClassCallWithStateTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class StaticMethodCallTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				ClassWithStaticMethods.class, TrafficToUnitTestsWriter.getDontMockClasses(), StaticMethodCallTrafficRecorder.STATIC_METHOD_CALL_RECORDING_SUBDIR, CasesCommon.getClassPath(CasesCommon.parseArguments(args)));
	}

}
