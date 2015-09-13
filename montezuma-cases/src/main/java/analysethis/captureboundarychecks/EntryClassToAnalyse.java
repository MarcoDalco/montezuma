package analysethis.captureboundarychecks;

public class EntryClassToAnalyse {

	public String enterBoundaryForFullTraversal(String string) {
		SecondClassToAnalyse classExitingBoundary = new SecondClassToAnalyse();

		return classExitingBoundary.continueWithinBoundaryForFullTraversal(string + ", B - entered boundary") + ", B - returning and exiting boundary";
	}

	public String enterBoundaryForTraversalAndCallback(String string) {
		SecondClassToAnalyse classExitingBoundary = new SecondClassToAnalyse();

		return classExitingBoundary.continueWithinBoundaryForTraversalAndCallback(string + ", B - entered boundary") + ", B - returning and exiting boundary";
	}

}
