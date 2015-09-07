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
    Kryo Kryo22 = new Kryo();
    Kryo22.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object23;
try (final ByteArrayInputStream ByteArrayInputStream24 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,117,116,105,108,46,68,97,116,-27,1,-19,-98,-72,-89,-6,41});
     final Input Input25 = new Input(ByteArrayInputStream24)) {
  Object23 = Kryo22.readClassAndObject(Input25);
}
    final Date givenDate29 = (Date) Object23;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenDate29, cut.getDate());
  } // Closing test

  @Test
  public void test2() {
    final Long expectedLong30 = 1441581109103L;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(expectedLong30, cut.getTimeMillis());
  } // Closing test

  @Test
  public void test3() throws ClassNotFoundException,IOException {
    Kryo Kryo22 = new Kryo();
    Kryo22.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object23;
try (final ByteArrayInputStream ByteArrayInputStream24 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,117,116,105,108,46,71,114,101,103,111,114,105,97,110,67,97,108,101,110,100,97,-14,1,69,117,114,111,112,101,47,76,111,110,100,111,-18,-12,-98,-72,-89,-6,41,1,2,4,-1,-49,-38,-23,-96,-57,5});
     final Input Input25 = new Input(ByteArrayInputStream24)) {
  Object23 = Kryo22.readClassAndObject(Input25);
}
    final GregorianCalendar givenGregorianCalendar31 = (GregorianCalendar) Object23;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenGregorianCalendar31, cut.getCalendar());
  } // Closing test

  @Test
  public void test4() {
    final Long expectedLong32 = 139962646964253L;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(expectedLong32, cut.getNanos());
  } // Closing test

  @Test
  public void test5() throws ClassNotFoundException,IOException {
    Kryo Kryo22 = new Kryo();
    Kryo22.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object23;
try (final ByteArrayInputStream ByteArrayInputStream24 = new ByteArrayInputStream(new byte [] {1,0,106,97,118,97,46,115,113,108,46,68,97,116,-27,1,-5,-98,-72,-89,-6,41});
     final Input Input25 = new Input(ByteArrayInputStream24)) {
  Object23 = Kryo22.readClassAndObject(Input25);
}
    final Date givenDate33 = (Date) Object23;
    final TimeConsumer cut = new TimeConsumer();

    assertEquals(givenDate33, cut.getSQLDate());
  } // Closing test

}
