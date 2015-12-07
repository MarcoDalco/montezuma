package analysethis.untestable.until.privateclassreferencesareworkedaround;

import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseExternalInvokedClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleInterface;

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
