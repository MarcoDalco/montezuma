package org.montezuma.test.traffic.recording.cases;

import java.sql.SQLException;

public class RecordAll {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		BigDecimalUtilsTrafficRecorder.main(args);
		BoundaryChecksTrafficRecorder.main(args);
		CompiledStatementStoringPreparedStatementCreatorTrafficRecorder.main(args);
		CurrencyUtilsTrafficRecorder.main(args);
		PassThroughClassTrafficRecorder.main(args);
		SuperClassCallWithStateTrafficRecorder.main(args);
		TimeConsumerTrafficRecorder.main(args);
		UtilsConsumerTrafficRecorder.main(args);

		System.out.println();
		System.out.println("************************************");
		System.out.println("*** END OF EXECUTION (recording) ***");
		System.out.println("************************************");
	}
}
