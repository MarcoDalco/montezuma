package org.montezuma.test.traffic.serialisers.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

import org.montezuma.test.traffic.serialisers.Serialiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class KryoSerialiser implements Serialiser {
	private Kryo	kryo	= new Kryo();

	@Override
	public byte[] serialise(Object object) throws IOException {
		Exception lastException = null;
		// Whatever the reason for the serialisation to fail, this try-catch-retry mechanism could succeed
		// It might happen for example in case of OutOfMemory errors or perhaps concurrent changes to the serialised objects
		for (int i = 0; i < 10; i++) {
			try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				serialise(byteArrayOutputStream, object);
				final byte[] serialisedObject = byteArrayOutputStream.toByteArray();
				return serialisedObject;
			}
			catch (KryoException cme) {
				lastException = cme;
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
		throw new IllegalArgumentException("!!!!!!!!!!!!!!! Crossed the number of attempts to serialise an object", lastException);
	}

	@Override
	public void serialise(OutputStream outputStream, Object object) throws IOException {
		try (Output output = new Output(outputStream)) {
			kryo.writeClassAndObject(output, object);
		}
	}
}
