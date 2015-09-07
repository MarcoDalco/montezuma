package org.montezuma.test.traffic.serialisers.standard;

import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.serialisers.Deserialiser;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class StandardJavaDeserialiser implements Deserialiser {
	private final static boolean	log	= true;

	@Override
	public Object deserialise(byte[] serialisedArg) throws ClassNotFoundException {
		try {
			return deserialise(new ByteArrayInputStream(serialisedArg));
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("This should not happen!!!", e);
		}
	}

	private Object deserialise(InputStream inputStream) throws ClassNotFoundException, IOException {
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		final Object readObject = ois.readObject();
		if (log) {
			System.out.println("Read object class: " + readObject.getClass());
			System.out.println("Read object: " + readObject);
		}
		return readObject;
	}

	@Override
	public List<Object> deserialiseAll(InputStream inputStream) throws ClassNotFoundException {
		List<Object> objects = new ArrayList<>();
		try {
			while (true) {
				final Object readObject = this.deserialise(inputStream);
				if (log) {
					System.out.println("READOBJECT: " + (readObject == null ? null : "Something"));
					System.out.println(((InvocationData) readObject).signature);
				}
				InvocationData.printSingleInvocationDataSize((InvocationData) readObject);
				objects.add(readObject);
			}
		}
		catch (EOFException eof) {
			// Fine, it's just the End of Stream. There is no better way to check it.
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("This should not happen!!!", e);
		}
		return objects;
	}
}
