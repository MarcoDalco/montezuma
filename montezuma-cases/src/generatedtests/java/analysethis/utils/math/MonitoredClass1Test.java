package analysethis.utils.math;

import static org.junit.Assert.assertEquals;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import dontanalysethis.DummyThirdParty;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.montezuma.test.traffic.serialisers.kryo.KryoRegisteredSerialiser;

@RunWith(JMockit.class)
public class MonitoredClass1Test {


  @Test
  public void test1() throws ClassNotFoundException,IOException {
    final String expectedString21 = "PRE";
    Kryo Kryo22 = new Kryo();
    Kryo22.setDefaultSerializer(KryoRegisteredSerialiser.class);
    final Object Object23;
try (final ByteArrayInputStream ByteArrayInputStream24 = new ByteArrayInputStream(new byte [] {1,0,100,111,110,116,97,110,97,108,121,115,101,116,104,105,115,46,68,117,109,109,121,84,104,105,114,100,80,97,114,116,-7,1,1,45,84,97,105,-20});
     final Input Input25 = new Input(ByteArrayInputStream24)) {
  Object23 = Kryo22.readClassAndObject(Input25);
}
    final DummyThirdParty givenDummyThirdParty26 = (DummyThirdParty) Object23;
    final String expectedString27 = "PRE-Tail";
    final MonitoredClass cut = new MonitoredClass(expectedString21);

    assertEquals(expectedString27, cut.getMessageEnhancedBy(givenDummyThirdParty26));
    assertEquals(expectedString21, cut.getMessage());
  } // Closing test

}
