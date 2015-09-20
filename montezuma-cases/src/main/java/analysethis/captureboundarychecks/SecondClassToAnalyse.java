package analysethis.captureboundarychecks;

import dontanalysethis.captureboundarychecks.ClassAfterBoundaryExit;

public class SecondClassToAnalyse {

	public String continueWithinBoundaryForFullTraversal(String string) {
		ClassAfterBoundaryExit classAfterBoundary = new ClassAfterBoundaryExit();

		return classAfterBoundary.exitBoundaryGoingForward(string + ", C - exiting boundary") + ", C - returned within boundary";
	}
}
