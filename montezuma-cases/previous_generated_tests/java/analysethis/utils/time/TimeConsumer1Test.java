package analysethis.utils.time;

import static org.junit.Assert.assertEquals;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.sql.Date;
import java.util.Date;
import java.util.GregorianCalendar;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser;

@RunWith(JMockit.class)
public class TimeConsumer1Test {


  @Test
  public void test1() throws ClassNotFoundException,IOException {
    Kryo Kryo24 = new Kryo();
    Kryo24.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object25;
try (final ByteArrayInputStream ByteArrayInputStream26 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,117,116,105,108,46,68,97,116,-27,1,-19,-78,-108,-126,-7,41});
     final Input Input27 = new Input(ByteArrayInputStream26)) {
  Object25 = Kryo24.readClassAndObject(Input27);
}
    final Date givenDate28 = (Date) Object25;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenDate28, cut.getDate());
  } // Closing test

  @Test
  public void test2() {
    final Long expectedLong29 = 1441234491758L;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(expectedLong29, cut.getTimeMillis());
  } // Closing test

  @Test
  public void test3() throws ClassNotFoundException,IOException {
    Kryo Kryo24 = new Kryo();
    Kryo24.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object25;
try (final ByteArrayInputStream ByteArrayInputStream26 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,117,116,105,108,46,71,114,101,103,111,114,105,97,110,67,97,108,101,110,100,97,-14,1,69,117,114,111,112,101,47,76,111,110,100,111,-18,-15,-78,-108,-126,-7,41,1,2,4,-1,-49,-38,-23,-96,-57,5});
     final Input Input27 = new Input(ByteArrayInputStream26)) {
  Object25 = Kryo24.readClassAndObject(Input27);
}
    final GregorianCalendar givenGregorianCalendar30 = (GregorianCalendar) Object25;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenGregorianCalendar30, cut.getCalendar());
  } // Closing test

  @Test
  public void test4() {
    final Long expectedLong31 = 174978120448433L;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(expectedLong31, cut.getNanos());
  } // Closing test

  @Test
  public void test5() throws ClassNotFoundException,IOException {
    Kryo Kryo24 = new Kryo();
    Kryo24.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object25;
try (final ByteArrayInputStream ByteArrayInputStream26 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,115,113,108,46,68,97,116,-27,1,-9,-78,-108,-126,-7,41});
     final Input Input27 = new Input(ByteArrayInputStream26)) {
  Object25 = Kryo24.readClassAndObject(Input27);
}
    final Date givenDate32 = (Date) Object25;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenDate32, cut.getSQLDate());
  } // Closing test

}
