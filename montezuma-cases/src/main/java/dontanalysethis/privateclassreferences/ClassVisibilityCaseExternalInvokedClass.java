package dontanalysethis.privateclassreferences;

public class ClassVisibilityCaseExternalInvokedClass {
	private final InvisibleClass INVISIBLE_CLASS = new InvisibleClass();

	private class InvisibleClass extends VisibleClass implements VisibleInterface {

		@Override
		public String getNameForClassCase() {
			return InvisibleClass.class.getName();
		}

		@Override
		public String getNameForInterfaceCase() {
			return InvisibleClass.class.getName() + "#" + "InterfaceMethodCall";
		}

	}

	public VisibleClass getSubClassForClass() {
		return INVISIBLE_CLASS;
	}

	public VisibleInterface getSubClassForInterface() {
		return INVISIBLE_CLASS;
	}
}
