package org.montezuma.test.traffic.serialisers.standard;

import org.montezuma.test.traffic.serialisers.Serialiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class StandardJavaSerialiser implements Serialiser {
	@Override
	public byte[] serialise(Object object) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		serialise(byteArrayOutputStream, object);
		final byte[] serialisedObject = byteArrayOutputStream.toByteArray();
		return serialisedObject;
	}

	@Override
	public void serialise(OutputStream outputStream, Object object) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(object);
	}
}
