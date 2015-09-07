package analysethis.utils.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils implements Serializable {
	private static final long	serialVersionUID	= 3502868223192370152L;

	public CurrencyUtils() {
		System.out.println("Currency Utils  instantiated");
	}

	private static final DecimalFormatSymbols	DEFAULT_LOCALE									= DecimalFormatSymbols.getInstance(Locale.US);
	private static final Currency							DEFAULT_CURRENCY								= DEFAULT_LOCALE.getCurrency();
	private static final NumberFormat					DEFAULT_CURRENCY_NUMBER_FORMAT	= new DecimalFormat("#0.00", DEFAULT_LOCALE);
	{
		DEFAULT_CURRENCY_NUMBER_FORMAT.setCurrency(DEFAULT_CURRENCY);
	}

	public String formatForDefaultCurrency(BigDecimal value) {
		return DEFAULT_CURRENCY_NUMBER_FORMAT.format(value.setScale(DEFAULT_CURRENCY.getDefaultFractionDigits(), MathUtils.DEFAULT_ROUNDING_MODE));
	}
}
