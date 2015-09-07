package dontanalysethis.captureboundarychecks;

import analysethis.captureboundarychecks.EntryClassToAnalyse;

public class ClassEnteringBoundary {
	public String enterBoundary() {
		EntryClassToAnalyse boundaryEntryClass = new EntryClassToAnalyse();
		return boundaryEntryClass.enterBoundary("A - From outside") + "- A - returning";
	}
}
