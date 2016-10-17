package org.montezuma.test.traffic.writing;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficToUnitTestsWriter extends TrafficReader {

	public static final String TESTS_PER_CLASS_LIMIT_OPTION_NAME = "tests_per_class_limit";
	static Map<String, Class<?>>	primitiveTypes	= new HashMap<>();
	static {
		primitiveTypes.put("boolean", boolean.class);
		primitiveTypes.put("byte", byte.class);
		primitiveTypes.put("char", char.class);
		primitiveTypes.put("short", short.class);
		primitiveTypes.put("int", int.class);
		primitiveTypes.put("long", long.class);
		primitiveTypes.put("float", float.class);
		primitiveTypes.put("double", double.class);
	};

	public static List<String> getDontMockClasses() {
		List<String> dontMockRegexList = new ArrayList<String>();
		dontMockRegexList.add("java.lang.*");
		dontMockRegexList.add("java.math.*");
		dontMockRegexList.add("java.text.*");
		dontMockRegexList.add("java.util.*");
		return dontMockRegexList;
	}

	private final Map<String, String> options = new HashMap<>();

	public TrafficToUnitTestsWriter() {}

	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		Option classnameOption = new Option("c", "classname", true, "Name of the class to write the tests of");
		Option recordingspathOption = new Option("r", "recordingspath", true, "Path of the directory with the recordings data");
		Option dontMockRegexListOption = new Option("n", "dontmock", true, "CSV list of classes and packages that must not be mocked");
		Option outputClassPathOption = new Option("d", "destinationclasspath", true, "Path of the base directory where to write the generated test classes");
		Option testClassNamePrefixOption = new Option("p", "testclassnameprefix", true, "Prefix for the generated test class names");
		Option classJavadocOption = new Option(null, "testclassjavadoc", true, "Javadoc for the generated test classes");

		classnameOption.setRequired(true);
		recordingspathOption.setRequired(true);
		dontMockRegexListOption.setRequired(true);
		outputClassPathOption.setRequired(true);

		Options options = new Options();
		options.addOption(classnameOption);
		options.addOption(recordingspathOption);
		options.addOption(dontMockRegexListOption);
		options.addOption(outputClassPathOption);
		options.addOption(testClassNamePrefixOption);
		options.addOption(classJavadocOption);

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);

			final String className = line.getOptionValue(classnameOption.getLongOpt());
			final String recordingsPath = line.getOptionValue(recordingspathOption.getLongOpt());
			final String[] dontMockRegexArray = line.getOptionValues(dontMockRegexListOption.getLongOpt());
			final String outputClassPath = line.getOptionValue(outputClassPathOption.getLongOpt());
			final String testClassNamePrefix = line.getOptionValue(testClassNamePrefixOption.getLongOpt(), "");
			final String classJavadoc = line.getOptionValue(classJavadocOption.getLongOpt());

			Class<?> clazz = Class.forName(className);
			File recordingDir = new File(recordingsPath);
			List<String> dontMockRegexList = Arrays.asList(dontMockRegexArray);
			new TrafficToUnitTestsWriter().generateTestsFor(clazz, recordingDir, dontMockRegexList, outputClassPath, testClassNamePrefix, classJavadoc);
		}
		catch(ParseException pe) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp(TrafficToUnitTestsWriter.class.getSimpleName(), options, true);
		}
	}

	public void generateTestsFor(final Class<?> clazz, File recordingDir, List<String> dontMockRegexList, String outputClassPath, String testClassNamePrefix, String classJavadoc) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Map<Integer, List<InvocationData>> invocationDataLists = loadInvocationDataForClass(clazz, recordingDir);
		for (List<InvocationData> invocationDataList : invocationDataLists.values()) {
			printInvocationDataSizes(invocationDataList);
		}

		generateTestClasses(invocationDataLists, clazz, dontMockRegexList, outputClassPath, testClassNamePrefix, classJavadoc);
	}

	private String getOption(String optionName, String defaultValue) {
		String value = options.get(optionName);
		if (value != null)
			return value;
		
		return defaultValue;
	}

	private void generateTestClasses(Map<Integer, List<InvocationData>> invocationDataLists, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath, String testClassNamePrefix, String classJavadoc) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final Map<Integer, Map<Integer, List<InvocationData>>> groupedInvocationDataListMapsMap = new HashMap<Integer, Map<Integer, List<InvocationData>>>();
		// Arbitrary grouping policy based on the number of tests (test classes) to write
		// TODO - make this arbitrary grouping policy configurable
		String testPerClassLimitString = getOption(TESTS_PER_CLASS_LIMIT_OPTION_NAME, "10");
		int testPerClassLimit = Integer.parseInt(testPerClassLimitString);

		boolean allTogether = (invocationDataLists.size() < testPerClassLimit);
		if (allTogether) {
			final List<InvocationData> staticInvocationsDataList = invocationDataLists.remove("0");
			if (staticInvocationsDataList != null)
				groupedInvocationDataListMapsMap.put(0, Collections.singletonMap(0, staticInvocationsDataList));

			if (invocationDataLists.size() > 0) {
				groupedInvocationDataListMapsMap.put(1, invocationDataLists);
			}
		} else {
			for (Map.Entry<Integer, List<InvocationData>> invocationDataListsEntry : invocationDataLists.entrySet()) {
				final Integer identityHashCode = invocationDataListsEntry.getKey();
				groupedInvocationDataListMapsMap.put(identityHashCode, Collections.singletonMap(identityHashCode, invocationDataListsEntry.getValue()));
			}
		}

		for (Map.Entry<Integer, Map<Integer, List<InvocationData>>> invocationDataListEntry : groupedInvocationDataListMapsMap.entrySet())
			generateTestClasses(invocationDataListEntry.getKey(), invocationDataListEntry.getValue(), clazz, dontMockRegexList, outputClassPath, testClassNamePrefix, classJavadoc);
	}

	private void generateTestClasses(int testClassNumber, Map<Integer, List<InvocationData>> invocationDataListsMap, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath, String testClassNamePrefix, String classJavadoc) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final String className = clazz.getSimpleName();
		final String testClassName = className + testClassNumber + "Test";

		TestClassWriter classWriter = new TestClassWriter(clazz, testClassNamePrefix + testClassName);
		classWriter.addImport("org.junit.Test");
		classWriter.addImport("org.junit.runner.RunWith");
		classWriter.addClassJavadoc(classJavadoc);
		String mockingFrameworkRunwithClassCanonicalName = MockingFrameworkFactory.getMockingFramework().getRunwithClassCanonicalName();
		classWriter.addImport(mockingFrameworkRunwithClassCanonicalName);
		for (Map.Entry<Integer, List<InvocationData>> invocationDataListEntry : invocationDataListsMap.entrySet()) {
			TestMethodsWriter methodsWriter =
					new TestMethodsWriter(invocationDataListEntry.getValue(), clazz, invocationDataListEntry.getKey(), classWriter, dontMockRegexList, new ImmutablesChecker(), classWriter.importsContainer);
			List<TestMethod> testMethods = methodsWriter.buildTestMethods();
			classWriter.addTestMethods(testMethods);
		}

		classWriter.write(outputClassPath);
	}
}
