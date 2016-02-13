package org.montezuma.test.traffic.writing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TestClassWriter implements ObjectDeclarationScope {

	final private String											packageName;
	final private String											testClassName;
	final Class<?>														testClass;
	ImportsContainer													importsContainer					= new ImportsContainer();
	private Map<Integer, ExpressionRenderer>	fieldRenderers		= new HashMap<>();
	private List<TestMethod>									testMethods				= new ArrayList<>();
	private final StructuredTextFileWriter		structuredFileWriter;
	int																				testNumber				= 0;
	private Map<Integer, VariableDeclarationRenderer>											declaredVariables	= new HashMap<>();
	// The IdentityHashCodeGenerator must not generate duplicate IDs within the same test class, can do it in different
	// test classes; that's why it's here.
	public final IdentityHashCodeGenerator		identityHashCodeGenerator	= new IdentityHashCodeGenerator();
	static final String												FILE_SEPARATOR		= System.getProperty("file.separator");

	public TestClassWriter(Class<?> clazz, String testClassName) {
		this.packageName = clazz.getPackage().getName();
		this.testClassName = testClassName;
		this.testClass = clazz;
		this.structuredFileWriter = new StructuredTextFileWriter();
	}

	public void write(final String testClassPath) throws IOException {
		preprocess();

		final String dirPath = packageName.replaceAll("\\.", FILE_SEPARATOR);
		File dirs = new File(testClassPath, dirPath);
		dirs.mkdirs();
		File testClassFile = new File(dirs, testClassName + ".java");
		try (FileWriter fileWriter = new FileWriter(testClassFile)) {
			appendPackage();
			structuredFileWriter.addEmptyLine();
			appendImports();
			structuredFileWriter.addEmptyLine();
			appendClassDeclaration();
			structuredFileWriter.addEmptyLine();
			appendFields();
			structuredFileWriter.addEmptyLine();
			appendTestMethods();
			appendClassEnd();
			System.out.println("Writing to file: " + testClassFile.getAbsolutePath());
			fileWriter.write(structuredFileWriter.toString());
		}
	}

	private void preprocess() {
		for (TestMethod method : testMethods) {
			method.preprocess();
		}
	}

	private void appendPackage() {
		structuredFileWriter.appendLine(0, "package " + packageName + ";");
	}

	public void addImport(String importExpression) {
		importsContainer.addImport(new Import(importExpression));
	}

	private void appendImports() {
		for (TestMethod testMethod : testMethods) {
			importsContainer.add(testMethod.getAllImports());
		}
		Set<Import> sortedImports = new TreeSet<>(importsContainer.getResolvedImports());
		for (Import importPath : sortedImports) {
			structuredFileWriter.appendLine(0, "import " + importPath.getText() + ";");
		}
	}

	private void appendClassDeclaration() {
		String mockingFrameworkRunwithClassName = MockingFrameworkFactory.getMockingFramework().getRunwithClassName();
		structuredFileWriter.appendLine(0, "@RunWith(" + mockingFrameworkRunwithClassName + ".class)");
		structuredFileWriter.appendLine(0, "public class " + testClassName + " {");
	}

	public void addField(int identityHashCode, ExpressionRenderer fieldRenderer) {
		fieldRenderers.put(identityHashCode, fieldRenderer);
	}

	private void appendFields() {
		for (ExpressionRenderer fieldRenderer : fieldRenderers.values()) {
			structuredFileWriter.appendLine(1, fieldRenderer.render());
		}
	}

	public void addTestMethods(List<TestMethod> testMethods) {
		this.testMethods.addAll(testMethods);
	}

	private void appendTestMethods() {
		for (TestMethod method : testMethods) {
			method.render(structuredFileWriter);
			structuredFileWriter.addEmptyLine();
		}
	}

	private void appendClassEnd() {
		structuredFileWriter.appendLine(0, "}");
	}

	@Override
	public void addDeclaredObject(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer) {
		declaredVariables.put(identityHashCode, variableDeclarationRenderer);
	}

	@Override
	public boolean declaresIdentityHashCode(int identityHashCode, Class<?> requiredClass) {
		VariableDeclarationRenderer variableDeclarationRenderer = declaredVariables.get(identityHashCode);
		return ((variableDeclarationRenderer != null) && (variableDeclarationRenderer.declaresClass(requiredClass)));
	}

	@Override
	public boolean declaresOrCanSeeIdentityHashCode(int identityHashCode, Class<?> requiredClass) {
		return declaresIdentityHashCode(identityHashCode, requiredClass);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode, Class<?> requiredClass) {
		return getVisibleDeclarationRenderer(identityHashCode, requiredClass);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode, Class<?> requiredClass) {
		return declaredVariables.get(identityHashCode);
	}
}
