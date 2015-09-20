package analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue;

public class B {
	public String b(String string) {
		C c = new C();

		// Test dependency: to solve this we first have to solve the simple "BoundaryChecksCallback" case, as C is currently
		// mocked.
		return c.c(string + ", B - entered boundary") + ", B - returning and exiting boundary";
	}

}
