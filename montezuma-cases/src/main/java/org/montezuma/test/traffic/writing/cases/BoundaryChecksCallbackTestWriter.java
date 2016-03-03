package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksCallbackTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.B;

public class BoundaryChecksCallbackTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		CasesCommon.generateTestsFor(
				B.class, TrafficToUnitTestsWriter.getDontMockClasses(), BoundaryChecksCallbackTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR, CasesCommon.parseArguments(args));
	}

}
