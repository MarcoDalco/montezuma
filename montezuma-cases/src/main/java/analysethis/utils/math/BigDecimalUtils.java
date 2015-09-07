package analysethis.utils.math;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalUtils {
	private static final MathContext	DEFAULT_BIGDECIMAL_MATH_CONTEXT	= new MathContext(128, MathUtils.DEFAULT_ROUNDING_MODE);
	public static final BigDecimal		BIGDECIMAL_100									= new BigDecimal(100, BigDecimalUtils.DEFAULT_BIGDECIMAL_MATH_CONTEXT);

	/**
	 * Return a BigDecimal for the given name. If the name does not exist, return 0.0.
	 * 
	 * @param name A String with the name.
	 * @return A BigDecimal.
	 */
	public BigDecimal toBigDecimal(String value) {
		return toBigDecimal(value, BigDecimal.ZERO);
	}

	/**
	 * Return a BigDecimal for the given name. If the name does not exist, return defaultValue.
	 * 
	 * @param name A String with the name.
	 * @param defaultValue The default value.
	 * @return A BigDecimal.
	 */
	public BigDecimal toBigDecimal(String value, BigDecimal defaultValue) {
		if ((value != null) && (!value.isEmpty())) {
			try {
				return new BigDecimal(value, BigDecimalUtils.DEFAULT_BIGDECIMAL_MATH_CONTEXT);
			}
			catch (NumberFormatException e) {}
		}
		return defaultValue;
	}

	/**
	 * Creates a new BigDecimal object with the given value and the default MathContext
	 * {@code DEFAULT_BIGDECIMAL_MATH_CONTEXT}
	 * 
	 * @param value the original double value
	 * @return the new BigDecimal object
	 */
	public BigDecimal toBigDecimal(double value) {
		return new BigDecimal(value, DEFAULT_BIGDECIMAL_MATH_CONTEXT);
	}
}
