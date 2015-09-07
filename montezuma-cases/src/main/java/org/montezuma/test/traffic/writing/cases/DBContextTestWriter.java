package org.montezuma.test.traffic.writing.cases;

import analysethis.matrix.lookups.db.DBContext;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.DBContextTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DBContextTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				DBContext.class, TrafficToUnitTestsWriter.getDontMockClasses(), DBContextTrafficRecorder.DBCONTEXT_RECORDING_SUBDIR, CasesCommon.TEST_CLASS_PATH);
	}

}
