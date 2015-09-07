package org.montezuma.test.traffic.writing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImmutablesChecker {

	private static Map<Class<?>, Boolean>	checkedImmutables = new HashMap<Class<?>, Boolean>();
	{
		checkedImmutables.put(BigInteger.class, Boolean.TRUE);
		checkedImmutables.put(BigDecimal.class, Boolean.TRUE);
	};

	boolean isImmutable(Class<?> testClass) {
		return isImmutable(testClass, new HashSet<Class<?>>());
	}

	private boolean isImmutable(Class<?> testClass, Set<Class<?>> checking) {
		final Boolean isVerifiedImmutable = checkedImmutables.get(testClass);
		if (isVerifiedImmutable != null) {
			return isVerifiedImmutable;
		}
		if (testClass == Object.class) {
			return true;
		}
		if (testClass.isPrimitive() || testClass.isEnum()) {
			return true;
		}
		if (checking.contains(testClass)) {
			return true;
		}
		if (testClass.isArray()) {
			return false;
		}
		checking.add(testClass);
	
		final Field[] fields = testClass.getDeclaredFields();
	
		for (Field field : fields) {
			final int modifiers = field.getModifiers();
			if (Modifier.isFinal(modifiers)) {
				// TODO - check that String is considered immutable
				if (isImmutable(field.getType(), checking)) {
					continue;
				}
			}
			// WARNING: This is arbitrary, as a transient field is modifiable, so the object can change
			// state, but usually transient is used to mark a field to not be serialised, as it's used
			// only for optimisation purposes, so the object's externally visible behaviour should not
			// depend on it, and that is why I'm considering it a condition of "immutability".
			if (Modifier.isTransient(modifiers)) {
				continue;
			}
			checkedImmutables.put(testClass, Boolean.FALSE);
			return false;
		}
	
		final boolean isImmutable = isImmutable(testClass.getSuperclass(), checking);
		if (isImmutable) {
			checkedImmutables.put(testClass, Boolean.TRUE);
		}
		return isImmutable;
	}

}
