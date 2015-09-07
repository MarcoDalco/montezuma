package org.montezuma.test.traffic.writing;

import java.util.HashSet;
import java.util.Set;

public class TestMethodOpening implements TextRenderer {
	public final Set<String>											annotations					= new HashSet<>();
	public final Set<String>											modifiers						= new HashSet<>();
	public final String														returnType;
	public final String														methodName;
	public final Set<String>											parameters					= new HashSet<>();
	public final Set<Class<? extends Throwable>>	declaredThrowables	= new HashSet<>();

	public TestMethodOpening(String returnType, String methodName) {
		super();
		this.returnType = returnType;
		this.methodName = methodName;
	}

	public TestMethodOpening(TestMethodOpening opening, String testMethodName) {
		this(opening.returnType, testMethodName);
		this.annotations.addAll(opening.annotations);
		this.modifiers.addAll(opening.modifiers);
		this.parameters.addAll(opening.parameters);
		this.declaredThrowables.addAll(opening.declaredThrowables);
	}

	@Override
	public String toString() {
		return "TestMethodOpening [annotations=" + annotations + ", modifiers=" + modifiers + ", returnType=" + returnType + ", methodName=" + methodName + ", parameters=" + parameters
				+ ", declaredThrowables=" + declaredThrowables + "]";
	}

	protected TextRenderer getRenderer() {
		return this;
	}

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		for (String annotation : annotations) {
			structuredTextFileWriter.appendLine(1, annotation);
		}

		StringBuffer signatureLine = new StringBuffer();
		for (String modifier : modifiers) {
			signatureLine.append(modifier);
			signatureLine.append(" ");
		}

		signatureLine.append(returnType);
		signatureLine.append(" ");

		signatureLine.append(methodName);
		signatureLine.append("(");

		for (String parameter : parameters) {
			signatureLine.append(parameter);
			signatureLine.append(",");
		}
		if (parameters.size() > 0) {
			signatureLine.setLength(signatureLine.length() - 1);
		}

		signatureLine.append(")");

		if (declaredThrowables.size() > 0) {
			signatureLine.append(" throws ");

			for (Class<? extends Throwable> throwable : declaredThrowables) {
				signatureLine.append(throwable.getSimpleName());
				signatureLine.append(",");
			}

			signatureLine.setLength(signatureLine.length() - 1);
		}

		signatureLine.append(" {");
		structuredTextFileWriter.appendLine(1, signatureLine.toString());
	}

	public void preprocess() {
		// Not yet necessary
	}
}
