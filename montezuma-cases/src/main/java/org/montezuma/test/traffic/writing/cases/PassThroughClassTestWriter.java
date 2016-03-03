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
		CasesCommon.generateTestsFor(
				MonitoredClass.class, TrafficToUnitTestsWriter.getDontMockClasses(), PassThroughClassTrafficRecorder.PASSTHROUGH_CLASS_RECORDING_SUBDIR, CasesCommon.parseArguments(args));
	}

}
