package analysethis.captureboundarychecks;

import dontanalysethis.captureboundarychecks.ClassAfterBoundaryExit;

public class SecondClassToAnalyse {

	public String continueWithinBoundaryForFullTraversal(String string) {
		ClassAfterBoundaryExit classAfterBoundary = new ClassAfterBoundaryExit();

		return classAfterBoundary.exitBoundaryGoingForward(string + ", C - exiting boundary") + ", C - returned within boundary";
	}

	public String continueWithinBoundaryForTraversalAndCallback(String string) {
		ClassAfterBoundaryExit classAfterBoundary = new ClassAfterBoundaryExit();

		return classAfterBoundary.exitBoundaryGoingForwardAndGetCalledBack(this, string + ", C - exiting boundary") + ", C - returned within boundary";
	}

	public String callback(String string) {
		return string + ", C - called back into boundary - going to return outside";
	}
}
