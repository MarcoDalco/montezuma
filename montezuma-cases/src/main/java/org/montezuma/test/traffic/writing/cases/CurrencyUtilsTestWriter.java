package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CurrencyUtilsTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.utils.math.CurrencyUtils;

public class CurrencyUtilsTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This test is a very basic real-life scenario. It just tests the invocation of a method and the equivalence of its returned value.";
		CasesCommon.generateTestsFor(
				CurrencyUtils.class, TrafficToUnitTestsWriter.getDontMockClasses(), CurrencyUtilsTrafficRecorder.CURRENCY_UTILS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
