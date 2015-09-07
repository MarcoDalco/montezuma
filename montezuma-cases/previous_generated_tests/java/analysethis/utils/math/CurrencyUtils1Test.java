package analysethis.utils.math;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class CurrencyUtils1Test {


  @Test
  public void test1() {
    final BigDecimal expectedBigDecimal1 = BigDecimal.TEN;
    final String expectedString19 = "10.00";
    final CurrencyUtils cut = new CurrencyUtils();

    assertEquals(expectedString19, cut.formatForDefaultCurrency(expectedBigDecimal1));
  } // Closing test

}
