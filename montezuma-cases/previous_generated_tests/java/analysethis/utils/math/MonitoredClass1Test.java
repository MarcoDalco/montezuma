package analysethis.utils.math;

import static org.junit.Assert.assertEquals;
import dontanalysethis.DummyThirdParty;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class MonitoredClass1Test {

  @Mocked private DummyThirdParty mockedDummyThirdParty20;

  @Test
  public void test1() {
    final String expectedString21 = "PRE";
    final String expectedString22 = "PRE-Tail";
    final MonitoredClass cut = new MonitoredClass(expectedString21);

    new StrictExpectations() {{
  mockedDummyThirdParty20.enhance(expectedString21); times = 1; result = expectedString22;
}};

    assertEquals(expectedString22, cut.getMessageEnhancedBy(mockedDummyThirdParty20));
    assertEquals(expectedString21, cut.getMessage());
  } // Closing test

}
