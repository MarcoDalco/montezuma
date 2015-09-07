package analysethis.matrix.lookups.db;

import static org.junit.Assert.assertEquals;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import uhuru.matrix.lookups.db.LookupDB;

@RunWith(JMockit.class)
public class DBContext0Test {

  @Mocked private LookupDB mockedLookupDB0;

  @Test
  public void test1() {
    final Boolean expectedBoolean1 = true;
    final String givenString2 = "AHA";
    final String expectedString3 = "";
    new StrictExpectations() {{
  LookupDB.isLogHistory(); times = 1; result = expectedBoolean1;
  LookupDB.getConnection(); times = 1;
}};

    assertEquals(expectedString3, DBContext.simpleFetch(givenString2));
  } // Closing test

}
