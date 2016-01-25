package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.Common;

import java.lang.reflect.Modifier;

public class ReflectionUtils {

	protected static Class<?>[] buildParameterTypes(final String[] argTypes) throws ClassNotFoundException {
		Class<?>[] parameterTypes = new Class<?>[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			final String argTypeString = argTypes[i];
			Class<?> argClass = Common.primitiveClasses.get(argTypeString);
			if (argClass == null) {
				argClass = Class.forName(argTypeString);
			}
			parameterTypes[i] = argClass;
		}
		return parameterTypes;
	}

	public static Class<?> getVisibleSuperClass(Class<?> classToAccess, Class<?> fromClass) {
		Class<?> declaredClass = classToAccess;
		while (!ReflectionUtils.isVisibleFrom(declaredClass, fromClass))
			 declaredClass = declaredClass.getSuperclass();
		
		return declaredClass;
	}

	private static boolean isVisibleFrom(Class<?> classToAccess, Class<?> fromClass) {
		int testedClassModifiers = classToAccess.getModifiers();
		if ((testedClassModifiers & Modifier.PUBLIC) != 0)
			return true;

		if ((testedClassModifiers & Modifier.PRIVATE) != 0)
			return false;

		if (fromClass.getPackage().equals(classToAccess.getPackage()))
			return true;

		if ((testedClassModifiers & Modifier.PROTECTED) != 0)
			return classToAccess.isAssignableFrom(fromClass);

		return false;
	}

}
