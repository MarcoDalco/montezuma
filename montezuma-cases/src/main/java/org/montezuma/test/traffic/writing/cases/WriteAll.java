package org.montezuma.test.traffic.writing.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.writing.JMockitFramework;
import org.montezuma.test.traffic.writing.MockingFramework;
import org.montezuma.test.traffic.writing.MockingFrameworkFactory;
import org.montezuma.test.traffic.writing.MockitoFramework;

public class WriteAll {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/jmockit/mocking/java" };
		generateMockingTestClasses(args, true, new JMockitFramework());

		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/jmockit/nonmocking/java" };
		generateMockingTestClasses(args, false, new JMockitFramework());

		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/mockito/mocking/java" };
		generateMockingTestClasses(args, true, new MockitoFramework());

		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/mockito/nonmocking/java" };
		generateMockingTestClasses(args, false, new MockitoFramework());

		System.out.println();
		System.out.println("*******************************************");
		System.out.println("*** END OF TEST GENERATION (with mocks) ***");
		System.out.println("*******************************************");
	}

	private static void generateMockingTestClasses(String[] args, boolean mocking, MockingFramework mockingFramework) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		MockingFrameworkFactory.setMockingFramework(mockingFramework);

		BigDecimalUtilsTestWriter.main(args);
		BoundaryChecksCallbackTestWriter.main(mergeArrays(args, new String [] { CasesCommon.TEST_CLASS_NAME_PREFIX_KEY, "BoundaryChecksCallBack" }));
		BoundaryChecksTestWriter.main(mergeArrays(args, new String [] { CasesCommon.TEST_CLASS_NAME_PREFIX_KEY, "BoundaryChecks" }));
		BoundaryChecksWithStateChangeInBothCallForthAndCallBackTestWriter.main(mergeArrays(args, new String [] { CasesCommon.TEST_CLASS_NAME_PREFIX_KEY, "BoundaryChecksWithStateChangeInBothCallForthAndCallBack" }));
		ChainedInitTestWriter.main(args);
		if (mocking)
			ClassVisibilityCaseTestWriter.main(args);
		else
			ClassVisibilityCaseNoExternalInvokedClassTestWriter.main(args);
		if (mocking) CollectionsProviderTestWriter.main(args);
		CompiledStatementStoringPreparedStatementCreatorTestWriter.main(args);
		CurrencyUtilsTestWriter.main(args);
		if (mocking)
			PassThroughClassTestWriter.main(args);
		else
			PassThroughClassNoDummyThirdPartyMockingTestWriter.main(args);
		StaticMethodCallTestWriter.main(args);
		SuperClassCallWithStateTestWriter.main(args);
		TimeConsumerTestWriter.main(args);
		UtilsConsumerTestWriter.main(args);
	}

	private static String[] mergeArrays(String[] array1, String[] array2) {
		String[] mergedArray = new String[array1.length + array2.length];

		System.arraycopy(array1, 0, mergedArray, 0, array1.length);
		System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);

		return mergedArray;
	}
}
