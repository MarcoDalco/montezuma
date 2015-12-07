package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ClassVisibilityCaseTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import analysethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseMainClass;

public class ClassVisibilityCaseNoExternalInvokedClassTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final List<String> dontMockClasses = new ArrayList<>();
		dontMockClasses.add(".*ClassVisibilityCaseExternalInvokedClass");
		dontMockClasses.addAll(TrafficToUnitTestsWriter.getDontMockClasses());
		new TrafficToUnitTestsWriter().generateTestsFor(ClassVisibilityCaseMainClass.class, dontMockClasses, ClassVisibilityCaseTrafficRecorder.CLASS_VISIBILITY_CASE_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
