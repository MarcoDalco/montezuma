package dontanalysethis.captureboundarychecks;

import analysethis.captureboundarychecks.SecondClassToAnalyse;

public class ClassAfterBoundaryExit {

	public String exitBoundaryGoingForward(String string) {
		return string + ", D - outside boundary - going to return inside";
	}

	public String exitBoundaryGoingForwardAndGetCalledBack(SecondClassToAnalyse secondClassToAnalyse, String string) {
		return secondClassToAnalyse.callback(string + ", D - outside boundary - going to call back in") + ", D - returned outside boundary - going to return inside";
	}

}
