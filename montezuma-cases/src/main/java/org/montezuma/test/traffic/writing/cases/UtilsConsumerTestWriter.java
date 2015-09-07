package org.montezuma.test.traffic.writing.cases;

import analysethis.utils.consumer.UtilsConsumer;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.UtilsConsumerTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class UtilsConsumerTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				UtilsConsumer.class, TrafficToUnitTestsWriter.getDontMockClasses(), UtilsConsumerTrafficRecorder.UTILS_CONSUMER_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
