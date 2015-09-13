package dontanalysethis.captureboundarychecks;

import analysethis.captureboundarychecks.EntryClassToAnalyse;

public class ClassEnteringBoundary {
	public String enterBoundaryForFullTraversal() {
		EntryClassToAnalyse boundaryEntryClass = new EntryClassToAnalyse();
		return boundaryEntryClass.enterBoundaryForFullTraversal("A - From outside") + "- A - returning";
	}

	public String enterBoundaryForTraversalAndCallback() {
		EntryClassToAnalyse boundaryEntryClass = new EntryClassToAnalyse();
		return boundaryEntryClass.enterBoundaryForTraversalAndCallback("A - From outside") + "- A - returning";
	}
}
