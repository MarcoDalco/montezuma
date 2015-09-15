package org.montezuma.test.traffic.writing;

public class Import implements Comparable<Import> {
	private transient final String	shortName;
	public final String	className;
	public final String	methodName;

	public Import(String className, String methodName) {
		super();
		this.shortName = className.substring(1 + className.lastIndexOf('.'));
		this.className = className;
		this.methodName = methodName;
	}

	public Import(String className) {
		this(className, null);
	}

	public String getShortName() {
		return shortName;
	}

	public String getText() {
		final boolean hasMethodName = methodName == null;
		return (hasMethodName ? "" : "static ") + className + (hasMethodName ? "" : "." + methodName);
	}

	@Override
	public String toString() {
		return "Import [className=" + className + ", methodName=" + methodName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Import other = (Import) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Import o) {
		if (this == o)
			return 0;

		Import other = (Import) o;
		int hasMethodName = (methodName == null ? 0 : -1);
		int otherHasMethodName = (other.methodName == null ? 0 : -1);
		int compareWithOtherForMethodName = hasMethodName - otherHasMethodName;
		if (compareWithOtherForMethodName != 0) {
			return compareWithOtherForMethodName;
		}

		return className.compareTo(other.className);
	}
}
