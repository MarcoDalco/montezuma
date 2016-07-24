package dontanalysethis.privateclassreferencesandtypeoptimisation;

import java.io.Serializable;

public class ClassVisibilityAndTypeOptimisationCaseExternalInvokedClass {
	private final InvisibleClassD INVISIBLE_CLASS = new InvisibleClassD();

	private class InvisibleClassD extends ClassC implements InterfaceD, Serializable {

		@Override
		public String getNameForClassCase() {
			return InvisibleClassD.class.getName();
		}

		@Override
		public String getNameForInterfaceBCase() {
			return InvisibleClassD.class.getName() + "#" + "InterfaceBMethodCall";
		}

		@Override
		public String getNameForInterfaceDCase() {
			return InvisibleClassD.class.getName() + "#" + "InterfaceDMethodCall";
		}

	}

	public ClassA getSubClassForClass() {
		return INVISIBLE_CLASS;
	}

	public InterfaceB getSubClassForInterfaceB() {
		return INVISIBLE_CLASS;
	}

	public InterfaceD getSubClassForInterfaceD() {
		return INVISIBLE_CLASS;
	}
}
