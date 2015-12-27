package org.montezuma.test.traffic.writing;

public interface ObjectDeclarationScope {
	void addDeclaredObject(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer);

	boolean declaresIdentityHashCode(int identityHashCode);
	boolean declaresOrCanSeeIdentityHashCode(int identityHashCode);

	VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode);
	VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode);
}
