package org.montezuma.test.traffic.writing;

public interface ObjectDeclarationScope {
	void addDeclaredIdentityHashCode(int identityHashCode);

	boolean declaresIdentityHashCode(int identityHashCode);
}
