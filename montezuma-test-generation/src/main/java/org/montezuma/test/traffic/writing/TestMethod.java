package org.montezuma.test.traffic.writing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TestMethod implements TextRenderer {
	public TestMethodOpening	opening;
	public CodeChunk					instantiationMethodPart;
	public List<CodeChunk>		codeChunks	= new ArrayList<>();
	public CodeChunk					closure;

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		final Collection<Class<? extends Throwable>> allDeclaredThrowables = getAllDeclaredThrowables();
		opening.declaredThrowables.addAll(allDeclaredThrowables);

		opening.getRenderer().render(structuredTextFileWriter);
		LinkedHashMap<Integer, InitCodeChunk> requiredInits = collectAllTheRequiredInits();
		for (CodeChunk codeChunk : requiredInits.values()) {
			structuredTextFileWriter.appendChunk(2, codeChunk);
		}
		instantiationMethodPart.getRenderer().render(structuredTextFileWriter);

		List<CodeChunk> combinedCodeChunks = CodeChunk.tryCombine(codeChunks);
		for (CodeChunk codeChunk : combinedCodeChunks) {
			structuredTextFileWriter.appendChunk(2, codeChunk);
		}

		structuredTextFileWriter.appendChunk(1, closure);
	}

	private LinkedHashMap<Integer, InitCodeChunk> collectAllTheRequiredInits() {
		LinkedHashMap<Integer, InitCodeChunk> inits = new LinkedHashMap<Integer, InitCodeChunk>();

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

		allDeclaredExceptions.addAll(instantiationMethodPart.getAllDeclaredThrowables());
		for (CodeChunk codeChunk : codeChunks) {
			allDeclaredExceptions.addAll(codeChunk.getAllDeclaredThrowables());
		}
		allDeclaredExceptions.addAll(closure.getAllDeclaredThrowables());

		return allDeclaredExceptions;
	}

	public Collection<? extends Import> getAllImports() {
		Set<Import> allImports = new HashSet<>();

		for (Class<? extends Throwable> throwableClass : getAllDeclaredThrowables()) {
			allImports.add(new Import(throwableClass.getName()));
		}

		allImports.addAll(instantiationMethodPart.getAllImports());
		for (CodeChunk codeChunk : codeChunks) {
			allImports.addAll(codeChunk.getAllImports());
		}
		allImports.addAll(closure.getAllImports());

		return allImports;
	}

	public TestMethod cloneOpening(String methodName) {
		TestMethod newMethod = new TestMethod();

		newMethod.opening = new TestMethodOpening(opening, methodName);
		newMethod.instantiationMethodPart = new CodeChunk(instantiationMethodPart);

		return newMethod;
	}

	public void preprocess() {
		opening.preprocess();
		instantiationMethodPart.preprocess();
		for (CodeChunk codeChunk : codeChunks) {
			codeChunk.preprocess();
		}
		closure.preprocess();
	}
}
