package org.montezuma.test.traffic.writing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class CodeChunk implements TextRenderer {
	public Set<Import>														requiredImports					= new HashSet<>();
	public Set<String>														requiredMocks						= new HashSet<>();
	public Set<Class<? extends Throwable>>				declaredThrowables			= new HashSet<>();
	public LinkedHashMap<Integer, InitCodeChunk>	requiredInits						= new LinkedHashMap<>();
	public List<CodeChunk>												methodPartsBeforeLines	= new ArrayList<>();
	public List<ExpressionRenderer>								codeRenderers						= new ArrayList<>();
	public List<CodeChunk>												methodPartsAfterLines		= new ArrayList<>();

	public CodeChunk() {}

	public CodeChunk(CodeChunk original) {
		requiredImports.addAll(original.requiredImports);
		requiredMocks.addAll(original.requiredMocks);
		declaredThrowables.addAll(original.declaredThrowables);
		requiredInits.putAll(original.requiredInits);
		methodPartsBeforeLines.addAll(original.methodPartsBeforeLines);
		codeRenderers.addAll(original.codeRenderers); // Got to clone, instead?
		methodPartsAfterLines.addAll(original.methodPartsAfterLines);
	}

	protected TextRenderer getRenderer() {
		return this;
	}

	public void preprocess() {
		for (CodeChunk codeChunk : requiredInits.values()) {
			codeChunk.preprocess();
		}

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			codeChunk.preprocess();
		}

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			codeChunk.preprocess();
		}
	}

	@Override
	public void render(StructuredTextFileWriter structuredTextFileWriter) {
		for (CodeChunk codeChunk : requiredInits.values()) {
			codeChunk.getRenderer().render(structuredTextFileWriter);
		}

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			codeChunk.getRenderer().render(structuredTextFileWriter);
		}

		for (ExpressionRenderer renderer : getExpressionRenderers()) {
			structuredTextFileWriter.appendLine(2, renderer.render());
		}

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			codeChunk.getRenderer().render(structuredTextFileWriter);
		}
	}

	public Set<Class<? extends Throwable>> getAllDeclaredThrowables() {
		Set<Class<? extends Throwable>> allDeclaredExceptions = new HashSet<>(declaredThrowables);

		for (CodeChunk codeChunk : requiredInits.values()) {
			allDeclaredExceptions.addAll(codeChunk.getAllDeclaredThrowables());
		}

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			allDeclaredExceptions.addAll(codeChunk.getAllDeclaredThrowables());
		}

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			allDeclaredExceptions.addAll(codeChunk.getAllDeclaredThrowables());
		}

		return allDeclaredExceptions;
	}

	public Collection<? extends Import> getAllImports() {
		Set<Import> allImports = new HashSet<>(requiredImports);

		for (CodeChunk codeChunk : requiredInits.values()) {
			allImports.addAll(codeChunk.getAllImports());
		}

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			allImports.addAll(codeChunk.getAllImports());
		}

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			allImports.addAll(codeChunk.getAllImports());
		}

		return allImports;
	}

	public List<ExpressionRenderer> getExpressionRenderers() {
		return codeRenderers;
	}

	public boolean canCombineWith(@SuppressWarnings("unused") CodeChunk previous) {
		return false; // By default!!
	}

	public CodeChunk combineWith(CodeChunk codeChunk) {
		throw new UnsupportedOperationException("Combining not supported for these types: " + this.getClass() + " and " + codeChunk.getClass());
	}

	protected void mergeAllFrom(CodeChunk codeChunk) {
		requiredImports.addAll(codeChunk.requiredImports);
		requiredMocks.addAll(codeChunk.requiredMocks);
		declaredThrowables.addAll(codeChunk.declaredThrowables);
		requiredInits.putAll(codeChunk.requiredInits);
		methodPartsBeforeLines.addAll(codeChunk.methodPartsBeforeLines);
		codeRenderers.addAll(codeChunk.codeRenderers);
		methodPartsAfterLines.addAll(codeChunk.methodPartsAfterLines);
	}

	public static List<CodeChunk> tryCombine(List<CodeChunk> codeChunks) {
		List<CodeChunk> combinedCodeChunks = new ArrayList<>();

		CodeChunk previous = null;
		for (CodeChunk codeChunk : codeChunks) {
			if (codeChunk.canCombineWith(previous)) {
				previous = previous.combineWith(codeChunk);
			} else {
				if (previous != null) {
					combinedCodeChunks.add(previous);
				}
				previous = codeChunk;
			}
		}
		if (previous != null) {
			combinedCodeChunks.add(previous);
		}

		return combinedCodeChunks;
	}

	public LinkedHashMap<Integer, InitCodeChunk> collectAllTheRequiredInits() {
		LinkedHashMap<Integer, InitCodeChunk> inits = new LinkedHashMap<>();

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			inits.putAll(codeChunk.collectAllTheRequiredInits());
		}

		inits.putAll(requiredInits);
		requiredInits.clear();

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			inits.putAll(codeChunk.collectAllTheRequiredInits());
		}

		return inits;
	}

	@Override
	public String toString() {
		return "CodeChunk [requiredImports=" + requiredImports + ", requiredMocks=" + requiredMocks + ", declaredThrowables=" + declaredThrowables + ", requiredInits=" + requiredInits
				+ ", methodPartsBeforeLines=" + methodPartsBeforeLines + ", codeRenderers=" + codeRenderers + ", methodPartsAfterLines=" + methodPartsAfterLines + "]";
	}

	public void addExpressionRenderer(ExpressionRenderer expressionRenderer) {
		codeRenderers.add(expressionRenderer);
	}
}
