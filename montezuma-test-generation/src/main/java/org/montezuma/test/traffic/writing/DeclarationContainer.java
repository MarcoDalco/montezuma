package org.montezuma.test.traffic.writing;

public interface DeclarationContainer {
	void addVariableDeclaration(int id, VariableDeclarationRenderer variableDeclaration);
	VariableDeclarationRenderer getVariableDeclaration(int id);
}
