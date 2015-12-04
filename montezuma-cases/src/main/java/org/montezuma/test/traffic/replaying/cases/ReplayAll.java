package org.montezuma.test.traffic.replaying.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ReplayAll {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
		BigDecimalUtilsTrafficReplayer.main(args);
		BoundaryChecksTrafficReplayer.main(args);
		BoundaryChecksCallbackTrafficReplayer.main(args);
		BoundaryChecksWithStateChangeInBothCallForthAndCallBackTrafficReplayer.main(args);
		ChainedInitTrafficReplayer.main(args);
		ClassVisibilityCaseTrafficReplayer.main(args);
		CompiledStatementStoringPreparedStatementCreatorTrafficReplayer.main(args);
		CurrencyUtilsTrafficReplayer.main(args);
		PassThroughClassTrafficReplayer.main(args);
		SuperClassCallWithStateTrafficReplayer.main(args);
		TimeConsumingTrafficReplayer.main(args);
		UtilsConsumerTrafficReplayer.main(args);

		System.out.println();
		System.out.println("************************************");
		System.out.println("*** END OF EXECUTION (replaying) ***");
		System.out.println("************************************");
	}
}
