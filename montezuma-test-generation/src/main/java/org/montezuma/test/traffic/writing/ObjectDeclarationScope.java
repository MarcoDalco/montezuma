package org.montezuma.test.traffic.writing;

public interface ObjectDeclarationScope {
	void addDeclaredObject(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer);

	boolean declaresIdentityHashCode(int identityHashCode, Class<?> requiredClass);
	boolean declaresOrCanSeeIdentityHashCode(int identityHashCode, Class<?> requiredClass);

	VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode, Class<?> requiredClass);
	VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode, Class<?> requiredClass);
}
