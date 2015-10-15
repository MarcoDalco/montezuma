package org.montezuma.test.traffic.writing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TestMethod implements TextRenderer, ObjectDeclarationScope {
	public TestMethodOpening	opening;
	public CodeChunk					instantiationMethodPart;
	public List<CodeChunk>		codeChunks	= new ArrayList<>();
	public CodeChunk					closure;
	private Set<Integer>			declaredIdentityHashCodes	= new HashSet<>();

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		final Collection<Class<? extends Throwable>> allDeclaredThrowables = getAllDeclaredThrowables();
		opening.declaredThrowables.addAll(allDeclaredThrowables);

		opening.getRenderer().render(structuredTextFileWriter);
		LinkedHashMap<Integer, InitCodeChunk> requiredInits = collectAllTheRequiredInits();
		for (CodeChunk codeChunk : requiredInits.values()) {
			structuredTextFileWriter.appendChunk(2, codeChunk);
		}
		if (instantiationMethodPart != null)
			instantiationMethodPart.getRenderer().render(structuredTextFileWriter);

		List<CodeChunk> combinedCodeChunks = CodeChunk.tryCombine(codeChunks);
		for (CodeChunk codeChunk : combinedCodeChunks) {
			structuredTextFileWriter.appendChunk(2, codeChunk);
		}

		structuredTextFileWriter.appendChunk(1, closure);
	}

	private LinkedHashMap<Integer, InitCodeChunk> collectAllTheRequiredInits() {
		LinkedHashMap<Integer, InitCodeChunk> inits = new LinkedHashMap<Integer, InitCodeChunk>();

		if (instantiationMethodPart != null)
			inits.putAll(instantiationMethodPart.collectAllTheRequiredInits());
		for (CodeChunk chunk : codeChunks) {
			inits.putAll(chunk.collectAllTheRequiredInits());
		}

		return inits;
	}

	@Override
	public String toString() {
		return "TestMethod [opening=" + opening + ", instantiationMethodPart=" + instantiationMethodPart + ", codeChunks=" + codeChunks + ", closure=" + closure + "]";
	}

	private Collection<Class<? extends Throwable>> getAllDeclaredThrowables() {
		Set<Class<? extends Throwable>> allDeclaredExceptions = new HashSet<>();

		if (instantiationMethodPart != null)
			allDeclaredExceptions.addAll(instantiationMethodPart.getAllDeclaredThrowables());
		for (CodeChunk codeChunk : codeChunks) {
			allDeclaredExceptions.addAll(codeChunk.getAllDeclaredThrowables());
		}
		allDeclaredExceptions.addAll(closure.getAllDeclaredThrowables());

		return allDeclaredExceptions;
	}

	public ImportsContainer getAllImports() {
		ImportsContainer importsContainer = new ImportsContainer();

		for (Class<? extends Throwable> throwableClass : getAllDeclaredThrowables()) {
			importsContainer.addImport(new Import(throwableClass.getName()));
		}

		if (instantiationMethodPart != null)
			importsContainer.add(instantiationMethodPart.getAllImports());
		for (CodeChunk codeChunk : codeChunks) {
			importsContainer.add(codeChunk.getAllImports());
		}
		importsContainer.add(closure.getAllImports());

		return importsContainer;
	}

	public TestMethod cloneOpening(String methodName) {
		TestMethod newMethod = new TestMethod();

		newMethod.opening = new TestMethodOpening(opening, methodName);
		if (instantiationMethodPart != null) {
			newMethod.instantiationMethodPart = new CodeChunk(instantiationMethodPart);
			declaredIdentityHashCodes = new HashSet<>(instantiationMethodPart.declaredIdentityHashCodes);
		}

		return newMethod;
	}

	public void preprocess() {
		opening.preprocess();
		if (instantiationMethodPart != null)
			instantiationMethodPart.preprocess();
		for (CodeChunk codeChunk : codeChunks) {
			codeChunk.preprocess();
		}
		closure.preprocess();
	}

	@Override
	public void addDeclaredIdentityHashCode(int identityHashCode) {
		declaredIdentityHashCodes.add(identityHashCode);
	}

	@Override
	public boolean declaresIdentityHashCode(int identityHashCode) {
		if (declaredIdentityHashCodes.contains(identityHashCode))
			return true;

		if ((instantiationMethodPart != null) && (instantiationMethodPart.declaresIdentityHashCode(identityHashCode)))
				return true;

		for (CodeChunk codeChunk : codeChunks)
			if (codeChunk.declaresIdentityHashCode(identityHashCode))
				return true;

		if ((closure != null) && (closure.declaresIdentityHashCode(identityHashCode)))
				return true;

			return false;
	}
}
