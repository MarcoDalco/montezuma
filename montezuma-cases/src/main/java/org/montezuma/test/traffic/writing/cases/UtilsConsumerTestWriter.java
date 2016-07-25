package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.UtilsConsumerTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.utils.consumer.UtilsConsumer;

public class UtilsConsumerTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This case is almost a duplicate of the BigDecimalUtils one, except it tests returning a null value, which should be asserted with 'assertNull'.";
		CasesCommon.generateTestsFor(
				UtilsConsumer.class, TrafficToUnitTestsWriter.getDontMockClasses(), UtilsConsumerTrafficRecorder.UTILS_CONSUMER_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
