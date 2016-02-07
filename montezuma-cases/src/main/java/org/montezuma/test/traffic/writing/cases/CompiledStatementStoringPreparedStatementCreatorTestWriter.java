package org.montezuma.test.traffic.writing.cases;

import analysethis.com.somecompany.dao.CompiledStatementStoringPreparedStatementCreator;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CompiledStatementStoringPreparedStatementCreatorTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CompiledStatementStoringPreparedStatementCreatorTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
				CompiledStatementStoringPreparedStatementCreator.class, TrafficToUnitTestsWriter.getDontMockClasses(),
				CompiledStatementStoringPreparedStatementCreatorTrafficRecorder.COMPILED_STATEMENT_RECORDING_SUBDIR, CasesCommon.getClassPath(CasesCommon.parseArguments(args)));
	}

}
