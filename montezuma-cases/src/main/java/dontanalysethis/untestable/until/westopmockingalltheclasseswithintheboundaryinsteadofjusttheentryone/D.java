package dontanalysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.C;

public class D {
	public String d(C c, String string) {
		return c.cCallback(string + ", D - outside boundary - going to call back in") + ", D - returned outside boundary - going to return inside";
	}

}
