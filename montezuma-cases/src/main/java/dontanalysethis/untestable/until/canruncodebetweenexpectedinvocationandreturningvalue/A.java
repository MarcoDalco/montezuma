package dontanalysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue;

import analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.B;

public class A {
	public String a() {
		B b = new B();
		return b.b("A - From outside") + "- A - returning";
	}
}
