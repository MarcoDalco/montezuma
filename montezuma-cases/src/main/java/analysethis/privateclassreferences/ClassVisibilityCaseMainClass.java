package analysethis.privateclassreferences;

import dontanalisethis.privateclassreferences.ClassVisibilityCaseExternalInvokedClass;
import dontanalisethis.privateclassreferences.VisibleClass;
import dontanalisethis.privateclassreferences.VisibleInterface;

public class ClassVisibilityCaseMainClass {
	private final ClassVisibilityCaseExternalInvokedClass CLASS_VISIBILITY_CASE_EXTERNAL_INVOKED_CLASS = new ClassVisibilityCaseExternalInvokedClass();

	public VisibleClass getNewSubClassForClass() {
		return CLASS_VISIBILITY_CASE_EXTERNAL_INVOKED_CLASS.getSubClassForClass();
	}

	public VisibleInterface getNewSubClassForInterface() {
		return CLASS_VISIBILITY_CASE_EXTERNAL_INVOKED_CLASS.getSubClassForInterface();
	}
	
	public String getValueFromNewSubClassForClass() {
		return CLASS_VISIBILITY_CASE_EXTERNAL_INVOKED_CLASS.getSubClassForClass().getNameForClassCase();
	}

	public String getValueFromNewSubClassForInterface() {
		return CLASS_VISIBILITY_CASE_EXTERNAL_INVOKED_CLASS.getSubClassForInterface().getNameForInterfaceCase();
	}
}
