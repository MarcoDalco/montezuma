package org.montezuma.test.traffic.writing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportsContainer {
	private Map<String, Set<Import>>	importSets	= new HashMap<>();
	private Map<String, Import>				resolvedImportsAndShortNames	= null;
	private Map<String, Import>				resolvedImportsAndLongNames		= null;

	public void addImport(Import imporz) {
		Set<Import> setOfImports;
		String id = imporz.getMethodName();
		if ("*".equals(id)) {
			id = imporz.getShortName() + "." + id;
		}
		if (id == null) {
			id = imporz.getShortName();
		}

		setOfImports = importSets.get(id);
		if (setOfImports == null) {
			setOfImports = new HashSet<>();
			importSets.put(id, setOfImports);
		}

		setOfImports.add(imporz);
	}

	public void addAllImports(Collection<? extends Import> allImports) {
		for (Import imporz : allImports) {
			addImport(imporz);
		}
	}

	public Set<Import> getResolvedImports() {
		return new HashSet<>(getResolvedImportsAndShortNames().values());
	}

	protected Map<String, Import> getResolvedImportsAndShortNames() {
		if (resolvedImportsAndShortNames == null) {
			resolveImportsAndNames();
		}

		return resolvedImportsAndShortNames;
	}

	protected Map<String, Import> getResolvedImportsAndLongNames() {
		if (resolvedImportsAndShortNames == null) {
			resolveImportsAndNames();
		}

		return resolvedImportsAndLongNames;
	}

	protected void resolveImportsAndNames() {
		resolvedImportsAndShortNames = new HashMap<>();
		resolvedImportsAndLongNames = new HashMap<>();

		for (Map.Entry<String, Set<Import>> importsSetEntry : importSets.entrySet()) {
			final Import imporz = importsSetEntry.getValue().iterator().next();
			resolvedImportsAndShortNames.put(importsSetEntry.getKey(), imporz);
			resolvedImportsAndLongNames.put(imporz.className, imporz);
		}
	}

	public void add(ImportsContainer allImports) {
		for (Set<Import> importSet : allImports.importSets.values())
			for (Import imporz : importSet)
				addImport(imporz);
	}

	public boolean imports(String classCanonicalName) {
		return (classCanonicalName.startsWith("java.lang") || getResolvedImportsAndLongNames().containsKey(classCanonicalName));
	}
}
