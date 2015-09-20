package analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone;

public class B {
	public String b(String string) {
		C c = new C();

		return c.c(string + ", B - entered boundary") + ", B - returning and exiting boundary";
	}

}
