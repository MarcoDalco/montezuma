package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.TimeConsumerTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.timefunctionsareworkedaround.TimeConsumer;

public class TimeConsumerTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This case is a catch-all for all time-dependent mocking/testing, which we currently don't support";
		CasesCommon.generateTestsFor(
				TimeConsumer.class, TrafficToUnitTestsWriter.getDontMockClasses(), TimeConsumerTrafficRecorder.TIME_CONSUMER_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
