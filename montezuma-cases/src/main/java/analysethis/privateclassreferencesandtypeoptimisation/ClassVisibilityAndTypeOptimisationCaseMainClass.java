package analysethis.privateclassreferencesandtypeoptimisation;

import dontanalysethis.privateclassreferencesandtypeoptimisation.ClassA;
import dontanalysethis.privateclassreferencesandtypeoptimisation.ClassC;
import dontanalysethis.privateclassreferencesandtypeoptimisation.ClassVisibilityAndTypeOptimisationCaseExternalInvokedClass;
import dontanalysethis.privateclassreferencesandtypeoptimisation.InterfaceB;
import dontanalysethis.privateclassreferencesandtypeoptimisation.InterfaceD;

public class ClassVisibilityAndTypeOptimisationCaseMainClass {
	private final ClassVisibilityAndTypeOptimisationCaseExternalInvokedClass EXTERNAL_INVOKED_CLASS = new ClassVisibilityAndTypeOptimisationCaseExternalInvokedClass();

	public ClassA getNewSubClassForClassA() {
		return EXTERNAL_INVOKED_CLASS.getSubClassForClass();
	}

	public InterfaceB getNewSubClassForInterfaceB() {
		return EXTERNAL_INVOKED_CLASS.getSubClassForInterfaceB();
	}

	public InterfaceD getNewSubClassForInterfaceD() {
		return EXTERNAL_INVOKED_CLASS.getSubClassForInterfaceD();
	}

	public String getValueFromNewSubClassForClass() {
		return EXTERNAL_INVOKED_CLASS.getSubClassForClass().getNameForClassCase();
	}

	public String getValueFromNewSubClassForInterfaceB() {
		return EXTERNAL_INVOKED_CLASS.getSubClassForInterfaceB().getNameForInterfaceBCase();
	}

	public String getValueFromNewSubClassForInterfaceD() {
			return EXTERNAL_INVOKED_CLASS.getSubClassForInterfaceD().getNameForInterfaceDCase();
	}

	public String getClassNameOf(ClassC classC) {
		return classC.getNameForClassCase();
	}
}
