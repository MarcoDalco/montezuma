package analysethis.captureboundarychecks;

import static org.junit.Assert.assertEquals;
import dontanalysethis.captureboundarychecks.ClassAfterBoundaryExit;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class EntryClassToAnalyse1Test {

  @Mocked private ClassAfterBoundaryExit mockedClassAfterBoundaryExit6;

  @Test
  public void test1() {
    final String givenString7 = "A - From outside, B - entered boundary, C - exiting boundary";
    final String expectedString8 = "A - From outside, B - entered boundary, C - exiting boundary, D - outside boundary - going to return inside";
    final String givenString9 = "A - From outside";
    final String expectedString10 = "A - From outside, B - entered boundary, C - exiting boundary, D - outside boundary - going to return inside, C - returned within boundary, B - returning and exiting boundary";
    final EntryClassToAnalyse cut = new EntryClassToAnalyse();

    new StrictExpectations() {{
  new ClassAfterBoundaryExit(); times = 1; result = mockedClassAfterBoundaryExit6;
  mockedClassAfterBoundaryExit6.exitBoundaryGoingForward(givenString7); times = 1; result = expectedString8;
}};

    assertEquals(expectedString10, cut.enterBoundary(givenString9));
  } // Closing test

}
