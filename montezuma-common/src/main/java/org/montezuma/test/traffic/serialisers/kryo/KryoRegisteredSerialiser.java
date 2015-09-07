package org.montezuma.test.traffic.serialisers.kryo;

import sun.reflect.ReflectionFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class KryoRegisteredSerialiser<T> extends FieldSerializer<T> {

	public KryoRegisteredSerialiser(Kryo kryo, Class<?> type) {
		super(kryo, type);
	}

	public KryoRegisteredSerialiser(Kryo kryo, Class<?> type, Class<?>[] generics) {
		super(kryo, type, generics);
	}

	public void write(Kryo kryo, Output output, T object) {
		super.write(kryo, output, object);
	}

	public T read(Kryo kryo, Input input, Class<T> type) {
		return super.read(kryo, input, type);
	}

	protected T create(Kryo kryo, Input input, Class<T> type) {
		try {
			return super.create(kryo, input, type);
		}
		catch (KryoException e) {
			try {
				final Constructor<?> constructor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(type, Object.class.getDeclaredConstructor(new Class[0]));
				constructor.setAccessible(true);
				@SuppressWarnings("unchecked") T newInstance = (T) constructor.newInstance(new Object[0]);
				return newInstance;
			}
			catch (NoSuchMethodException re) {
				re.printStackTrace();
				System.out.println("********* SHOULD NEVER HAPPEN, as the constructor should have JUST been created");
			}
			catch (IllegalArgumentException iae) {
				iae.printStackTrace();
				System.out.println("********* SHOULD NEVER HAPPEN (Wrong parameters, but accessing the default constructor, or if trying to instantiate an Enum)");
			}
			catch (ExceptionInInitializerError eiie) {
				eiie.printStackTrace();
				System.out
						.println("!!!!!!!!! POSSIBLE: the initialization of the object failed (invocation throws an exception?): it can happen if initialisation code fail - perhaps code outsude constructors.");
				throw eiie;
			}
			catch (InstantiationException ie) {
				ie.printStackTrace();
				System.out.println("********* SHOULD NEVER HAPPEN unless the class being instantiated has been made abstract");
			}
			catch (IllegalAccessException iae) {
				iae.printStackTrace();
				System.out.println("********* SHOULD NEVER HAPPEN because the generated constructor has been made accessible");
			}
			catch (InvocationTargetException ite) {
				ite.printStackTrace();
				System.out.println("!!!!!!!!! POSSIBLE: if the constructor throws an exception");
			}
		}
		throw new IllegalStateException("Unexpected: see previous exceptions");
	}
}
