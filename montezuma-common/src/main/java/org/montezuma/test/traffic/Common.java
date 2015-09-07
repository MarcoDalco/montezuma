package org.montezuma.test.traffic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Common {
	public final static String					BASE_RECORDING_PATH						= "recordings";
	public static final String					DEFAULT_RECORDING_SUBDIR			= "default_recording_subdir";
	public static final String					ARGS_SEPARATOR								= ",";
	public static final String					METHOD_NAME_TO_ARGS_SEPARATOR	= "|";

	public static Set<Class<?>>					primitiveClassesSet						= new HashSet<>();
	static {
		Arrays.spliterator(new Class<?>[] { Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class }).forEachRemaining(
				clazz -> primitiveClassesSet.add(clazz));
	}

	public static Map<String, Class<?>>	primitiveClasses							= new HashMap<>();
	static {
		Arrays.spliterator(new Class<?>[] { boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, void.class }).forEachRemaining(
				clazz -> primitiveClasses.put(clazz.toString(), clazz));
	}
}
