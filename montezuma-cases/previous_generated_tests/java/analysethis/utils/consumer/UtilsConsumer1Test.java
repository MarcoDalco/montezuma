package analysethis.utils.consumer;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class UtilsConsumer1Test {


  @Test
  public void test1() {
    final String givenString33 = "12";
    final BigDecimal expectedBigDecimal34 = new BigDecimal("12");
    final UtilsConsumer cut = new UtilsConsumer();

    assertEquals(expectedBigDecimal34, cut.doSomething(givenString33));
  } // Closing test

}
