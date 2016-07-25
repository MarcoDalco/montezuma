package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

	private Map<String, String> options = new HashMap<>();

	public TrafficToUnitTestsWriter() {}

	public TrafficToUnitTestsWriter(Map<String, String> options) {
		this.options = options;
	}

	public void generateTestsFor(final Class<?> clazz, List<String> dontMockRegexList, File recordingDir, String outputClassPath, String testClassNamePrefix, String classJavadoc) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
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
