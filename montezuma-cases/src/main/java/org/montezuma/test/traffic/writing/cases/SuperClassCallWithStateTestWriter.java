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
		CasesCommon.generateTestsFor(
				SomeClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), SuperClassCallWithStateTrafficRecorder.SUPERCLASS_CALL_RECORDING_SUBDIR, CasesCommon.parseArguments(args));
	}

}
