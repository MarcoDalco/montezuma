package org.montezuma.test.traffic.writing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeChunk implements TextRenderer, ObjectDeclarationScope {
	public ImportsContainer												requiredImports					= new ImportsContainer();
	public Set<String>														requiredMocks						= new HashSet<>();
	public Set<Class<? extends Throwable>>				declaredThrowables			= new HashSet<>();
	public LinkedHashMap<Integer, InitCodeChunk>	requiredInits						= new LinkedHashMap<>();
	public List<CodeChunk>												methodPartsBeforeLines	= new ArrayList<>();
	public List<ExpressionRenderer>								codeRenderers						= new ArrayList<>();
	public List<CodeChunk>												methodPartsAfterLines		= new ArrayList<>();
	public Map<Integer, VariableDeclarationRenderer>	declarations	= new HashMap<>();
	public final ObjectDeclarationScope	parentObjectDeclarationScope;

	public CodeChunk(ObjectDeclarationScope	parentObjectDeclarationScope) {
		this.parentObjectDeclarationScope =parentObjectDeclarationScope;
	}

	public CodeChunk(CodeChunk original) {
		requiredImports.add(original.requiredImports);
		requiredMocks.addAll(original.requiredMocks);
		declaredThrowables.addAll(original.declaredThrowables);
		requiredInits.putAll(original.requiredInits);
		methodPartsBeforeLines.addAll(original.methodPartsBeforeLines);
		codeRenderers.addAll(original.codeRenderers); // Got to clone, instead?
		methodPartsAfterLines.addAll(original.methodPartsAfterLines);
		parentObjectDeclarationScope = original.parentObjectDeclarationScope;
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

	public ImportsContainer getAllImports() {
		ImportsContainer importsContainer = new ImportsContainer();

		importsContainer.add(requiredImports);

		for (CodeChunk codeChunk : requiredInits.values()) {
			importsContainer.add(codeChunk.getAllImports());
		}

		for (CodeChunk codeChunk : methodPartsBeforeLines) {
			importsContainer.add(codeChunk.getAllImports());
		}

		for (CodeChunk codeChunk : methodPartsAfterLines) {
			importsContainer.add(codeChunk.getAllImports());
		}

		return importsContainer;
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
		requiredImports.add(codeChunk.requiredImports);
		requiredMocks.addAll(codeChunk.requiredMocks);
		declaredThrowables.addAll(codeChunk.declaredThrowables);
		// FIXME - At the moment it's overriding inits of the same objects (same identityHashCodes), but some of those Inits need to be preproceesed for other objects to use them (NewVariableNameRenderer), and merging them causes the overridden ones not to be preprocessed.
		mergeRequiredInits(requiredInits, codeChunk);
		methodPartsBeforeLines.addAll(codeChunk.methodPartsBeforeLines);
		codeRenderers.addAll(codeChunk.codeRenderers);
		methodPartsAfterLines.addAll(codeChunk.methodPartsAfterLines);
	}

	static void mergeRequiredInits(LinkedHashMap<Integer, InitCodeChunk> requiredInits, CodeChunk codeChunk) {
		for (CodeChunk codeChunk2 : codeChunk.methodPartsBeforeLines)
			mergeRequiredInits(requiredInits, codeChunk2.requiredInits);

		mergeRequiredInits(requiredInits, codeChunk.requiredInits);

		for (CodeChunk codeChunk2 : codeChunk.methodPartsAfterLines)
			mergeRequiredInits(requiredInits, codeChunk2.requiredInits);
//		codeChunk.requiredInits.clear();
	}

	private static void mergeRequiredInits(LinkedHashMap<Integer, InitCodeChunk> requiredInits, LinkedHashMap<Integer, InitCodeChunk> codeChunkRequiredInits) {
		for (Map.Entry<Integer, InitCodeChunk> chunkInitEntry : codeChunkRequiredInits.entrySet()) {
			Integer key = chunkInitEntry.getKey();
			InitCodeChunk chunkInitValue = chunkInitEntry.getValue();

			InitCodeChunk requiredInitValue = requiredInits.get(key);
			if (requiredInitValue == null) {
				requiredInits.put(key, chunkInitValue);
			} else {
				assert((chunkInitValue instanceof StandardInitCodeChunk) == (requiredInitValue instanceof StandardInitCodeChunk));
				if (chunkInitValue instanceof StandardInitCodeChunk) {
					StandardInitCodeChunk s1 = (StandardInitCodeChunk) chunkInitValue;
					StandardInitCodeChunk s2 = (StandardInitCodeChunk) requiredInitValue;
					assert(s1.argClass == s2.argClass);
				}
				chunkInitValue.chunkOverridingDeclaration = requiredInitValue;
			}
		}

		codeChunkRequiredInits.clear();
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

		// FIXME - At the moment it's overriding inits of the same objects (same identityHashCodes), but some of those Inits need to be preproceesed for other objects to use them (NewVariableNameRenderer), and merging them causes the overridden ones not to be preprocessed.
		mergeRequiredInits(inits, this);
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

	@Override
	public void addDeclaredObject(int identityHashCode, VariableDeclarationRenderer variableDeclarationRenderer) {
		declarations.put(identityHashCode, variableDeclarationRenderer);
	}

	@Override
	public boolean declaresIdentityHashCode(int identityHashCode) {
		if (declarations.containsKey(identityHashCode))
			return true;

		for (CodeChunk codeChunk : requiredInits.values())
			if (codeChunk.declaresIdentityHashCode(identityHashCode))
				return true;
		for (CodeChunk codeChunk : methodPartsBeforeLines)
			if (codeChunk.declaresIdentityHashCode(identityHashCode))
				return true;
		for (CodeChunk codeChunk : methodPartsAfterLines)
			if (codeChunk.declaresIdentityHashCode(identityHashCode))
				return true;

		return false;
	}

	@Override
	public boolean declaresOrCanSeeIdentityHashCode(int identityHashCode) {
		if (declaresIdentityHashCode(identityHashCode))
			return true;

		return parentObjectDeclarationScope.declaresOrCanSeeIdentityHashCode(identityHashCode);
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRendererInScopeOrSubscopes(int identityHashCode) {
		VariableDeclarationRenderer renderer;
		if (null != (renderer = declarations.get(identityHashCode)))
			return renderer;

		for (CodeChunk codeChunk : requiredInits.values())
			if (null != (renderer = codeChunk.getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode)))
				return renderer;
		for (CodeChunk codeChunk : methodPartsBeforeLines)
			if (null != (renderer = codeChunk.getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode)))
				return renderer;
		for (CodeChunk codeChunk : methodPartsAfterLines)
			if (null != (renderer = codeChunk.getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode)))
				return renderer;

		return null;
	}

	@Override
	public VariableDeclarationRenderer getVisibleDeclarationRenderer(int identityHashCode) {
		VariableDeclarationRenderer renderer;
		if (null != (renderer = getVisibleDeclarationRendererInScopeOrSubscopes(identityHashCode)))
			return renderer;

		return parentObjectDeclarationScope.getVisibleDeclarationRenderer(identityHashCode);
	}

	boolean shouldBeRendered() {
		return true;
	}
}
