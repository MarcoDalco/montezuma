package org.montezuma.test.traffic.writing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TestClassWriter {

	private String														packageName;
	private String														testClassName;
	protected ImportsContainer								importsContainer	= new ImportsContainer();
	private Map<Integer, ExpressionRenderer>	fieldRenderers		= new HashMap<>();
	private List<TestMethod>									testMethods				= new ArrayList<>();
	private final StructuredTextFileWriter		structuredFileWriter;
	int																				testNumber				= 0;
	static final String												FILE_SEPARATOR		= System.getProperty("file.separator");

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
