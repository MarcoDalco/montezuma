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
		final String classJavadoc= "This test checks the behaviour of Montezuma when the class on which a method is invoked is returned as the function return value. The test should generate an assertSame for that every time the instance is returned, and test that the two invocations happened on the same class. The latter is probably superfluous.";
		CasesCommon.generateTestsFor(
				ChainedInit.class, TrafficToUnitTestsWriter.getDontMockClasses(), ChainedInitTrafficRecorder.CHAINEDINIT_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
