package org.montezuma.test.traffic.serialisers;

import java.io.IOException;
import java.io.OutputStream;

public interface Serialiser {
	byte[] serialise(Object object) throws IOException;

	void serialise(OutputStream outputStream, Object object) throws IOException;
}
