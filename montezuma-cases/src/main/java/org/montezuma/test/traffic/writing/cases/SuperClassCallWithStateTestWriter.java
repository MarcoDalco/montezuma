package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.SuperClassCallWithStateTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.superclasscall.withstate.SomeClass;

public class SuperClassCallWithStateTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "TODO - I can't remember exactly why I created this simple test, but certainly there could be different cases to test, especially with the superclass being outside the capture boundaries, where a partial mock of the CUT would be required.";
		CasesCommon.generateTestsFor(
				SomeClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), SuperClassCallWithStateTrafficRecorder.SUPERCLASS_CALL_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
