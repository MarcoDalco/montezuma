package org.montezuma.test.traffic.writing.cases;

import analysethis.captureboundarychecks.EntryClassToAnalyse;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BoundaryChecksTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				EntryClassToAnalyse.class, TrafficToUnitTestsWriter.getDontMockClasses(), BoundaryChecksTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR, CasesCommon.getClassPath(CasesCommon.parseArguments(args)));
	}

}
