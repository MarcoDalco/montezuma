package org.montezuma.test.traffic.writing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestMethodOpening implements TextRenderer, ObjectDeclarationScope {
	public final Set<String>											annotations					= new HashSet<>();
	public final Set<String>											modifiers						= new HashSet<>();
	public final String														returnType;
	public final String														methodName;
	private final Map<Integer, VariableDeclarationRenderer>	stubParameters	= new HashMap<>();
	private final Map<Integer, VariableDeclarationRenderer>	declaredVariables	= new HashMap<>();
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
		this.declaredThrowables.addAll(opening.declaredThrowables);
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

		for (VariableDeclarationRenderer parameterRenderer : stubParameters.values()) {
			signatureLine.append(parameterRenderer.render(","));
			signatureLine.append(",");
		}
		if (stubParameters.size() > 0) {
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

	public void addParameter(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer) {
		this.stubParameters.put(identityHashCode, variableDeclarationRenderer);
		this.addDeclaredObject(identityHashCode, variableDeclarationRenderer);
	}

	@Override
	public String toString() {
	return "TestMethodOpening [annotations=" + annotations + ", modifiers=" + modifiers + ", returnType=" + returnType + ", methodName=" + methodName + ", stubParameters=" + stubParameters
		+ ", declaredVariables=" + declaredVariables + ", declaredThrowables=" + declaredThrowables + "]";
	}

	@Override
	public void addDeclaredObject(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer) {
		declaredVariables.put(identityHashCode, variableDeclarationRenderer);
	}

	@Override
	public boolean declaresIdentityHashCode(int identityHashCode, Class<?> requiredClass) {
		{
			VariableDeclarationRenderer variableDeclarationRenderer = declaredVariables.get(identityHashCode);
			if ((variableDeclarationRenderer != null) && (variableDeclarationRenderer.declaresClass(requiredClass)))
				return true;
		}

		return false;
	}

	@Override
	public boolean declaresOrCanSeeIdentityHashCode(int identityHashCode, Class<?> requiredClass) {
		throw new IllegalStateException("Unimplemented");
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode, Class<?> requiredClass) {
		VariableDeclarationRenderer renderer;
		if (null != (renderer = declaredVariables.get(identityHashCode)))
			return renderer;

		return null;
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode, Class<?> requiredClass) {
		throw new IllegalStateException("Unimplemented");
	}
}
