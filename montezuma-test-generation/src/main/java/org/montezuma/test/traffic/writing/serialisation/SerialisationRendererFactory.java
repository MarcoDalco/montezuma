package org.montezuma.test.traffic.writing.serialisation;

import org.montezuma.test.traffic.serialisers.SerialisationFactory;
import org.montezuma.test.traffic.serialisers.Serialiser;

public class SerialisationRendererFactory {
	public static SerialisationRenderer getSerialisationRenderer() {
		return new KryoSerialisationRenderer();
		// TODO: serialisation/deserialisation renderer as a plug-in or specified with Java System properties
		// return new StandardJavaSerialisationRenderer();
	}

	public static Serialiser getSerialiser() {
		return SerialisationFactory.getSerialiser();
	}
}
