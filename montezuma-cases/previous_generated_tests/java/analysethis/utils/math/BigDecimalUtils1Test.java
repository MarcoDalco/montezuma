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
    final String givenString0 = "";
    final BigDecimal expectedBigDecimal1 = BigDecimal.TEN;
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal1, cut.toBigDecimal(givenString0, expectedBigDecimal1));
  } // Closing test

  @Test
  public void test2() {
    final Double givenDouble2 = 5.0D;
    final BigDecimal expectedBigDecimal3 = new BigDecimal("5");
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal3, cut.toBigDecimal(givenDouble2));
  } // Closing test

  @Test
  public void test3() {
    final String givenString4 = "8.01";
    final BigDecimal expectedBigDecimal5 = new BigDecimal("8.01");
    final BigDecimalUtils cut = new BigDecimalUtils();

    assertEquals(expectedBigDecimal5, cut.toBigDecimal(givenString4));
  } // Closing test

}
