package dontanalysethis.captureboundarychecks;

public class ClassAfterBoundaryExit {

	public String exitBoundaryGoingForward(String string) {
		return string + ", D - outside boundary - going to return inside";
	}
}
