package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CollectionsProviderTrafficRecorder;
import org.montezuma.test.traffic.writing.TrafficToUnitTestsWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.collections.CollectionsProvider;

public class CollectionsProviderTestWriter {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		final String classJavadoc= "This test case checks the code-generation for the creation of the most commonly used collections";
		CasesCommon.generateTestsFor(
				CollectionsProvider.class, TrafficToUnitTestsWriter.getDontMockClasses(), CollectionsProviderTrafficRecorder.COLLECTIONS_PROVIDER_RECORDING_SUBDIR, CasesCommon.parseArguments(args), classJavadoc);
	}

}
