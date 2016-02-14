package analysethis.utils.consumer;

import analysethis.utils.math.BigDecimalUtils;

import java.math.BigDecimal;

public class UtilsConsumer {

	public BigDecimal doSomething(String toParse) {
		return new BigDecimalUtils().toBigDecimal(toParse);
	}

	public BigDecimal doSomethingReturningNull() {
		return new BigDecimalUtils().toBigDecimal(null, null);
	}

}
