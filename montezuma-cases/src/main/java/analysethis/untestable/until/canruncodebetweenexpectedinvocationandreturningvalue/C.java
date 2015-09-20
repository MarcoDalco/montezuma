package analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue;

import dontanalysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.D;

public class C {
	int	c	= 1;

	public String c(String string) {
		D d = new D();

		c = 3;

		final String resultFromExitingBoundary = d.d(this, string + ", C - exiting boundary");
		return resultFromExitingBoundary + ", C - returned within boundary with c value: " + c;
	}

	public String cCallback(String string) {
		final String stringToReturn = string + ", C - called back into boundary with c value: " + c + " - going to return outside";

		c = 5;

		return stringToReturn;
	}
}
