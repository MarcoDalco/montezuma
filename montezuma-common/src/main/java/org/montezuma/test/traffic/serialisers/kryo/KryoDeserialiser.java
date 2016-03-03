package org.montezuma.test.traffic.serialisers.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.serialisers.Deserialiser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class KryoDeserialiser implements Deserialiser {
	private final static boolean	log		= true;

	private Kryo	kryo	= new Kryo();
	{
		kryo.setDefaultSerializer(KryoRegisteredSerialiser.class);
	}

	@Override
	public Object deserialise(final byte[] serialisedArg) throws ClassNotFoundException, IOException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(serialisedArg);
		Input input = new Input(bais);
		try {
			return deserialise(input);
		}
		finally {
			try {
				input.close();
			} catch (Throwable t) {
				// Empty on purpose
			}
			try {
				bais.close();
			} catch (Throwable t) {
				// Empty on purpose
			}
		}
	}

	private Object deserialise(final Input input) throws ClassNotFoundException {
		final Object readObject = kryo.readClassAndObject(input);
		if (log) {
			System.out.println("Read object class: " + (readObject == null ? null : readObject.getClass()));
			System.out.println("Read object: " + readObject);
		}

		return readObject;
	}

	@Override
	public List<Object> deserialiseAll(InputStream inputStream) throws ClassNotFoundException {
		List<Object> objects = new ArrayList<Object>();
		Input input = new Input(inputStream);
		try {
			while (true) {
				final Object readObject = this.deserialise(input);
				if (log) {
					System.out.println("READOBJECT: " + (readObject == null ? null : "Something"));
					System.out.println(((InvocationData) readObject).signature);
				}
				InvocationData.printSingleInvocationDataSize((InvocationData) readObject);
				objects.add(readObject);
			}
		}
		catch (KryoException eof) {
			if (!eof.getMessage().contains("Buffer underflow")) {
				throw eof;
			}
			// Fine, it's just the End of Stream. There is no better way to check it.
		}
		finally {
			try {
				input.close();
			} catch (Throwable t) {
				// Empty on purpose
			}
		}
		return objects;
	}

}
