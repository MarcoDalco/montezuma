package analysethis.modifiers;

public class ModifiersCase {
	private ModifiersCase() {
		System.out.println("Private constructor");
	}

	protected ModifiersCase(int i) {
		System.out.println("Protected constructor: " + i);
	}

	public ModifiersCase(long l) {
		System.out.println("Public constructor: " + l);
	}

	ModifiersCase(String s) {
		System.out.println("Package-access constructor: " + s);
	}

	private int privateMethod() {
		System.out.println("Private Method");
		return 1;
	}

	protected int protectedMethod() {
		System.out.println("Private Method");
		return 2;
	}

	public int publicMethod() {
		System.out.println("Public Method");
		return 3;
	}

	int packageAccessMethod() {
		System.out.println("Package-access Method");
		return 4;
	}

	public static int invokeThePrivateStuff() {
		return new ModifiersCase().privateMethod();
	}
}
