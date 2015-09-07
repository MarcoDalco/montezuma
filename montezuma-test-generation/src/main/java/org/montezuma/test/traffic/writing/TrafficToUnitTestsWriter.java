package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.TrafficReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
		dontMockRegexList.add("java.*");
		return dontMockRegexList;
	}

	public void generateTestsFor(final Class<?> clazz, List<String> dontMockRegexList, String recordingSubDir, String outputClassPath) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Map<String, List<InvocationData>> invocationDataLists = loadInvocationDataForClass(clazz, recordingSubDir);
		for (List<InvocationData> invocationDataList : invocationDataLists.values()) {
			printInvocationDataSizes(invocationDataList);
		}

		generateTestClasses(invocationDataLists, clazz, dontMockRegexList, outputClassPath);
	}

	private void generateTestClasses(Map<String, List<InvocationData>> invocationDataLists, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final Map<String, List<InvocationData>> groupedInvocationDataLists;
		// Arbitrary grouping policy based on the number of tests (test classes) to write
		// TODO - make this arbitrary grouping policy configurable
		boolean allTogether = invocationDataLists.size() < 10;
		if (allTogether) {
			groupedInvocationDataLists = new HashMap<String, List<InvocationData>>();
			final List<InvocationData> staticInvocationsDataList = invocationDataLists.remove("0");
			if (staticInvocationsDataList != null)
				groupedInvocationDataLists.put("0", staticInvocationsDataList);

			List<InvocationData> invocationDataList = new ArrayList<>();
			for (List<InvocationData> invocationsPerInstance : invocationDataLists.values()) {
				invocationDataList.addAll(invocationsPerInstance);
			}
			if (invocationDataList.size() > 0)
				groupedInvocationDataLists.put("1", invocationDataList);
		} else
			groupedInvocationDataLists = invocationDataLists;

		for (Map.Entry<String, List<InvocationData>> invocationDataListEntry : groupedInvocationDataLists.entrySet())
			generateTestClasses(Integer.parseInt(invocationDataListEntry.getKey()), invocationDataListEntry.getValue(), clazz, dontMockRegexList, outputClassPath);
	}

	private void generateTestClasses(int instanceId, List<InvocationData> invocationDataList, Class<?> clazz, List<String> dontMockRegexList, String outputClassPath) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		final String className = clazz.getSimpleName();
		final String testClassName = className + instanceId + "Test";
		final String packageName = clazz.getPackage().getName();

		TestClassWriter classWriter = new TestClassWriter(packageName, testClassName);
		classWriter.addImport("mockit.integration.junit4.JMockit");
		classWriter.addImport("org.junit.Test");
		classWriter.addImport("org.junit.runner.RunWith");
		TestMethodsWriter methodsWriter = new TestMethodsWriter(invocationDataList, clazz, instanceId, classWriter, dontMockRegexList, new ImmutablesChecker());
		List<TestMethod> testMethods = methodsWriter.buildTestMethods();
		classWriter.addTestMethods(testMethods);

		classWriter.write(outputClassPath);
	}
}
