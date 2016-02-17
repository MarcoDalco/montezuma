package dontanalysethis.staticmethods;

public class ClassWithStaticMethodsOutsideTheBoundary {

	public static String staticMethodOutsideTheBoundary() {
		return "staticMethodOutsideTheBoundary";
	}

	public String nonStaticMethodOutsideTheBoundary() {
		return "nonStaticMethodOutsideTheBoundary";
	}

}
