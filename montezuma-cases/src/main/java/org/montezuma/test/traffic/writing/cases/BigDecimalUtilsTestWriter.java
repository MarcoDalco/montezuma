package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BigDecimalUtilsTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.utils.math.BigDecimalUtils;

public class BigDecimalUtilsTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc = "This test is for a real-life case, with simple utility classes/methods that do not invoke methods outside this class, so don't require mocking or stubbing of any sort. It's also a case for assertSame, where an object returned by a tested method must be the same as one passed as a parameter. It's also a case for the handling of BigDecimal objects in test generation.";
		CasesCommon.generateTestsFor(BigDecimalUtils.class, TrafficToUnitTestsWriter.getDontMockClasses(), BigDecimalUtilsTrafficRecorder.BIGDECIMAL_UTILS_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
