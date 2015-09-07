package analysethis.superclasscall.withstate;

import static org.junit.Assert.assertEquals;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class SomeClass1Test {


  @Test
  public void test1() {
    final Integer expectedInteger23 = 2;
    final SomeClass cut = new SomeClass();

    assertEquals(expectedInteger23, cut.getState());
  } // Closing test

}
