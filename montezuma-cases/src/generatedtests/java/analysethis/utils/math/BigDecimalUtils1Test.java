package analysethis.utils.math;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class BigDecimalUtils1Test {


  @Test
  public void test1() {
    final Double givenDouble0 = 5.0D;
    final BigDecimal expectedBigDecimal1 = new BigDecimal("5");
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal1, cut.toBigDecimal(givenDouble0));
  } // Closing test

  @Test
  public void test2() {
    final String givenString2 = "8.01";
    final BigDecimal expectedBigDecimal3 = new BigDecimal("8.01");
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal3, cut.toBigDecimal(givenString2));
  } // Closing test

  @Test
  public void test3() {
    final String givenString4 = "";
    final BigDecimal expectedBigDecimal5 = BigDecimal.TEN;
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal5, cut.toBigDecimal(givenString4, expectedBigDecimal5));
  } // Closing test

}
