package org.montezuma.test.traffic.writing.cases;

import analysethis.utils.math.MonitoredClass;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.PassThroughClassTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class PassThroughClassNoDummyThirdPartyMockingTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final List<String> dontMockClasses = new ArrayList<>();
		dontMockClasses.add(".*DummyThirdParty");
		dontMockClasses.addAll(TrafficToUnitTestsWriter.getDontMockClasses());
		new TrafficToUnitTestsWriter().generateTestsFor(MonitoredClass.class, dontMockClasses, PassThroughClassTrafficRecorder.PASSTHROUGH_CLASS_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
