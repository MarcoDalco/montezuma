package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ChainedInitTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.returningpassedobjects.ChainedInit;

public class ChainedInitTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		CasesCommon.generateTestsFor(
				ChainedInit.class, TrafficToUnitTestsWriter.getDontMockClasses(), ChainedInitTrafficRecorder.CHAINEDINIT_RECORDING_SUBDIR, CasesCommon.parseArguments(args));
	}

}
