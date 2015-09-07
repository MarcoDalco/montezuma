package analysethis.captureboundarychecks;

public class EntryClassToAnalyse {

	public String enterBoundary(String string) {
		SecondClassToAnalyse classExitingBoundary = new SecondClassToAnalyse();

		return classExitingBoundary.continueWithinBoundary(string + ", B - entered boundary") + ", B - returning and exiting boundary";
	}

}
