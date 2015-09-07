package org.montezuma.test.traffic.serialisers;

import org.montezuma.test.traffic.serialisers.kryo.KryoDeserialiser;
import org.montezuma.test.traffic.serialisers.kryo.KryoSerialiser;

public class SerialisationFactory {
	public static Serialiser getSerialiser() {
		return new KryoSerialiser();
		// TODO: serialiser/deserialiser as a plug-in or specified with Java System properties
		// return new StandardJavaSerialiser();
	}

	public static Deserialiser getDeserialiser() {
		return new KryoDeserialiser();
		// TODO: serialiser/deserialiser as a plug-in or specified with Java System properties
		// return new StandardJavaDeserialiser();
	}

}
