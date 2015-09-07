package org.montezuma.test.traffic.writing.cases;

import analysethis.utils.math.BigDecimalUtils;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BigDecimalUtilsTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BigDecimalUtilsTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				BigDecimalUtils.class, TrafficToUnitTestsWriter.getDontMockClasses(), BigDecimalUtilsTrafficRecorder.BIGDECIMAL_UTILS_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
