package org.montezuma.test.traffic;

import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CasesCommon {
	private static final String	TEST_CLASS_PATH_DEFAULT	= "src/generatedtests/java";
	public static final String TEST_CLASS_PATH_KEY = "-dest";

	public static Map<String, String> parseArguments(String [] args) {
		Map<String, String> parsedArgs = new HashMap<>();

		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "-dest" : parsedArgs.put(TEST_CLASS_PATH_KEY, args[++i]);
					break;
			}
		}

		return parsedArgs;
	}

	public static String getClassPath(Map<String, String> parsedArguments) {
		final String testClassPath = parsedArguments.get(TEST_CLASS_PATH_KEY);

		return (testClassPath != null ? testClassPath : TEST_CLASS_PATH_DEFAULT);
	}

	public static void generateTestsFor(Class<?> clazz, List<String> dontMockClasses, String recordingsSubDir, Map<String, String> args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		new TrafficToUnitTestsWriter().generateTestsFor(
		clazz, dontMockClasses, getRecordingsDir(recordingsSubDir), CasesCommon.getClassPath(args));
	}

	public static File getRecordingsDir(String recordingsSubDir) {
		return new File(Common.BASE_RECORDING_PATH, recordingsSubDir);
	}
}
