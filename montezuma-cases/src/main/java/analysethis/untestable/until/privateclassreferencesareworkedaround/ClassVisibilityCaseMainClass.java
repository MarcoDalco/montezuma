package analysethis.untestable.until.privateclassreferencesareworkedaround;

import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseExternalInvokedClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleInterface;

public class ClassVisibilityCaseMainClass {
	public VisibleClass getNewSubClassForClass() {
		return new ClassVisibilityCaseExternalInvokedClass().getNewSubClassForClass();
	}

	public VisibleInterface getNewSubClassForInterface() {
		return new ClassVisibilityCaseExternalInvokedClass().getNewSubClassForInterface();
	}
	
	public String getValueFromNewSubClassForClass() {
		return new ClassVisibilityCaseExternalInvokedClass().getNewSubClassForClass().getNameForClassCase();
	}

	public String getValueFromNewSubClassForInterface() {
		return new ClassVisibilityCaseExternalInvokedClass().getNewSubClassForInterface().getNameForInterfaceCase();
	}
}
