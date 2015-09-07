package analysethis.com.somecompany.dao;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.montezuma.test.traffic.recording.cases.MyConnection;
import org.montezuma.test.traffic.recording.cases.MyPreparedStatement;

@RunWith(JMockit.class)
public class CompiledStatementStoringPreparedStatementCreator1Test {

  @Mocked private MyConnection mockedMyConnection0;
  @Mocked private MyPreparedStatement mockedMyPreparedStatement1;

  @Test
  public void test1() throws SQLException {
    final String givenString2 = "insert into delete_log (resource_id, params) values( ? )";
    final Long givenLong3 = 1234L;
    final Object[] givenObjectArray4 = new Object[] {givenLong3};
    final Integer givenInteger5 = 1;
    final Long givenLong6 = 1234L;
    final String expectedString7 = "This is the compiled statement";
    final CompiledStatementStoringPreparedStatementCreator cut = new CompiledStatementStoringPreparedStatementCreator(givenString2, givenObjectArray4);

    new StrictExpectations() {{
  mockedMyConnection0.prepareStatement(givenString2); times = 1; result = mockedMyPreparedStatement1;
  mockedMyPreparedStatement1.setObject(givenInteger5, givenLong6); times = 1;
}};

    assertEquals(mockedMyPreparedStatement1, cut.createPreparedStatement(mockedMyConnection0));
    assertEquals(expectedString7, cut.getCompiledSQL());
  } // Closing test

}
