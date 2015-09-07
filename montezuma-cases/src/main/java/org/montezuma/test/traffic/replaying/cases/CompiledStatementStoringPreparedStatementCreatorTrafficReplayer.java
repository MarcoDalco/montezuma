package org.montezuma.test.traffic.replaying.cases;

import analysethis.com.somecompany.dao.CompiledStatementStoringPreparedStatementCreator;

import org.montezuma.test.traffic.recording.cases.CompiledStatementStoringPreparedStatementCreatorTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CompiledStatementStoringPreparedStatementCreatorTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = CompiledStatementStoringPreparedStatementCreator.class;
		String recordingSuDdir = CompiledStatementStoringPreparedStatementCreatorTrafficRecorder.COMPILED_STATEMENT_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
