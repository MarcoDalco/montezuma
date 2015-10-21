package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficToUnitTestsWriter extends TrafficReader {

	private static Map<String, Class<?>>	primitiveTypes	= new HashMap<>();
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

	public void generateTestsFor(final Class<?> clazz, List<String> dontMockRegexList, String recordingSubDir, String outputClassPath) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Map<Integer, List<InvocationData>> invocationDataLists = loadInvocationDataForClass(clazz, recordingSubDir);
		for (List<InvocationData> invocationDataList : invocationDataLists.values()) {
			printInvocationDataSizes(invocationDataList);
		}

		generateTestClasses(invocationDataLists, clazz, dontMockRegexList, outputClassPath);
	}

	private void generateTestClasses(Map<Integer, List<InvocationData>> invocationDataLists, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final Map<Integer, Map<Integer, List<InvocationData>>> groupedInvocationDataListMapsMap = new HashMap<Integer, Map<Integer, List<InvocationData>>>();
		// Arbitrary grouping policy based on the number of tests (test classes) to write
		// TODO - make this arbitrary grouping policy configurable
		boolean allTogether = (invocationDataLists.size() < 10);
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
			generateTestClasses(invocationDataListEntry.getKey(), invocationDataListEntry.getValue(), clazz, dontMockRegexList, outputClassPath);
	}

	private void generateTestClasses(int testClassNumber, Map<Integer, List<InvocationData>> invocationDataListsMap, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final String className = clazz.getSimpleName();
		final String testClassName = className + testClassNumber + "Test";
		final String packageName = clazz.getPackage().getName();

		TestClassWriter classWriter = new TestClassWriter(packageName, testClassName);
		classWriter.addImport("mockit.integration.junit4.JMockit");
		classWriter.addImport("org.junit.Test");
		classWriter.addImport("org.junit.runner.RunWith");
		for (Map.Entry<Integer, List<InvocationData>> invocationDataListEntry : invocationDataListsMap.entrySet()) {
			TestMethodsWriter methodsWriter =
					new TestMethodsWriter(invocationDataListEntry.getValue(), clazz, invocationDataListEntry.getKey(), classWriter, dontMockRegexList, new ImmutablesChecker(), classWriter.importsContainer);
			List<TestMethod> testMethods = methodsWriter.buildTestMethods();
			classWriter.addTestMethods(testMethods);
		}

		classWriter.write(outputClassPath);
	}
}
