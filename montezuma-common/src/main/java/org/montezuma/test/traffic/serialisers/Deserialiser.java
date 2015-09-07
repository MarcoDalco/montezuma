package org.montezuma.test.traffic.serialisers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Deserialiser {
	Object deserialise(byte[] serialisedArg) throws ClassNotFoundException, IOException;

	List<? extends Object> deserialiseAll(InputStream inputStream) throws ClassNotFoundException;
}
