package org.montezuma.test.traffic.writing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestClassWriter {

	private String														packageName;
	private String														testClassName;
	private Set<Import>												imports					= new HashSet<Import>();
	private Map<Integer, ExpressionRenderer>	fieldRenderers	= new HashMap<>();
	private List<TestMethod>									testMethods			= new ArrayList<>();
	private final StructuredTextFileWriter		structuredFileWriter;
	static final String												FILE_SEPARATOR	= System.getProperty("file.separator");

	public TestClassWriter(String packageName, String testClassName) {
		this.packageName = packageName;
		this.testClassName = testClassName;
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
		imports.add(new Import(importExpression));
	}

	private void appendImports() {
		Set<Import> allImports = new HashSet<Import>(imports);
		for (TestMethod testMethod : testMethods) {
			allImports.addAll(testMethod.getAllImports());
		}
		List<Import> sortedImports = new ArrayList<>(allImports);
		Collections.sort(sortedImports);
		for (Import importPath : sortedImports) {
			structuredFileWriter.appendLine(0, "import " + importPath.getText() + ";");
		}
	}

	private void appendClassDeclaration() {
		structuredFileWriter.appendLine(0, "@RunWith(JMockit.class)");
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
}
