package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ClassVisibilityCaseTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseMainClass;

public class ClassVisibilityCaseTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				ClassVisibilityCaseMainClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), ClassVisibilityCaseTrafficRecorder.CLASS_VISIBILITY_CASE_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
