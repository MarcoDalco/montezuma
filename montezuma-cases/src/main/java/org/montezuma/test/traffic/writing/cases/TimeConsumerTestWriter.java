package org.montezuma.test.traffic.writing.cases;

import analysethis.utils.time.TimeConsumer;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.TimeConsumerTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class TimeConsumerTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				TimeConsumer.class, TrafficToUnitTestsWriter.getDontMockClasses(), TimeConsumerTrafficRecorder.TIME_CONSUMER_RECORDING_SUBDIR, CasesCommon.getClassPath(CasesCommon.parseArguments(args)));
	}

}
