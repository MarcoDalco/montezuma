package analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone;

import dontanalysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.D;

public class C {
	public String c(String string) {
		D d = new D();

		return d.d(this, string + ", C - exiting boundary") + ", C - returned within boundary";
	}

	public String cCallback(String string) {
		return string + ", C - called back into boundary - going to return outside";
	}
}
