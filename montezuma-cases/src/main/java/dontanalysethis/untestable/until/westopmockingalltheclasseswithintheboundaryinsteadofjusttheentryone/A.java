package dontanalysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.B;

public class A {
	public String a() {
		B b = new B();
		return b.b("A - From outside") + "- A - returning";
	}
}
