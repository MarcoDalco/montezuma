package org.montezuma.test.traffic.writing;

public class IdentityHashCodeGenerator {

	private int	fakeIdentityHashCode	= 1000000;

	// TODO - check all the calling methods, to see where instance detection can be improved (or rather "introduced"!!)
	public int generateIdentityHashCode() {
		return fakeIdentityHashCode++;
	}

	int generateIdentityHashCodeForStaticClass(Class<?> clazz) {
		return System.identityHashCode(clazz);
	}

}
