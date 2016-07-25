package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CompiledStatementStoringPreparedStatementCreatorTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.com.somecompany.dao.CompiledStatementStoringPreparedStatementCreator;

public class CompiledStatementStoringPreparedStatementCreatorTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This test is a real-life scenario. It checks expected invocations on a mocked object passed as parameter and on a mock returned by the invocation of a method on the previous mock.";
		CasesCommon.generateTestsFor(
				CompiledStatementStoringPreparedStatementCreator.class, TrafficToUnitTestsWriter.getDontMockClasses(),
				CompiledStatementStoringPreparedStatementCreatorTrafficRecorder.COMPILED_STATEMENT_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
