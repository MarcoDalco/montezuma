package dontanalisethis.untestable.until.privateclassreferencesareworkedaround;

public class ClassVisibilityCaseExternalInvokedClass {
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

	public VisibleClass getNewSubClassForClass() {
		return new InvisibleClass();
	}

	public VisibleInterface getNewSubClassForInterface() {
		return new InvisibleClass();
	}
}
