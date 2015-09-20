package dontanalysethis.captureboundarychecks;

import analysethis.captureboundarychecks.SecondClassToAnalyse;

public class ClassAfterBoundaryExit {

	public String exitBoundaryGoingForward(String string) {
		return string + ", D - outside boundary - going to return inside";
	}
}
