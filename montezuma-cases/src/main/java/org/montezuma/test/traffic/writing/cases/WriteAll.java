package org.montezuma.test.traffic.writing.cases;

import org.montezuma.test.traffic.CasesCommon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class WriteAll {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/mocking/java" };
		BigDecimalUtilsTestWriter.main(args);
		BoundaryChecksTestWriter.main(args);
		BoundaryChecksCallbackTestWriter.main(args);
		BoundaryChecksWithStateChangeInBothCallForthAndCallBackTestWriter.main(args);
		ChainedInitTestWriter.main(args);
		ClassVisibilityCaseTestWriter.main(args);
		CollectionsProviderTestWriter.main(args);
		CompiledStatementStoringPreparedStatementCreatorTestWriter.main(args);
		CurrencyUtilsTestWriter.main(args);
		PassThroughClassTestWriter.main(args);
		StaticMethodCallTestWriter.main(args);
		SuperClassCallWithStateTestWriter.main(args);
		TimeConsumerTestWriter.main(args);
		UtilsConsumerTestWriter.main(args);

		args = new String [] { CasesCommon.TEST_CLASS_PATH_KEY, "src/generatedtests/nonmocking/java" };
		BigDecimalUtilsTestWriter.main(args);
		BoundaryChecksTestWriter.main(args);
		BoundaryChecksCallbackTestWriter.main(args);
		BoundaryChecksWithStateChangeInBothCallForthAndCallBackTestWriter.main(args);
		ChainedInitTestWriter.main(args);
		ClassVisibilityCaseNoExternalInvokedClassTestWriter.main(args);
		CompiledStatementStoringPreparedStatementCreatorTestWriter.main(args);
		CurrencyUtilsTestWriter.main(args);
		PassThroughClassNoDummyThirdPartyMockingTestWriter.main(args);
		StaticMethodCallTestWriter.main(args);
		SuperClassCallWithStateTestWriter.main(args);
		TimeConsumerTestWriter.main(args);
		UtilsConsumerNoUtilsMockingTestWriter.main(args);

		System.out.println();
		System.out.println("*******************************************");
		System.out.println("*** END OF TEST GENERATION (with mocks) ***");
		System.out.println("*******************************************");
	}
}
