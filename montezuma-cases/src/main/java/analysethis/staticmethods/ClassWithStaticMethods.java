package analysethis.staticmethods;

import dontanalysethis.staticmethods.ClassWithStaticMethodsOutsideTheBoundary;

public class ClassWithStaticMethods {
	public static String staticMethod() {
		return "staticMethodInside," + ClassWithStaticMethodsOutsideTheBoundary.staticMethodOutsideTheBoundary();
	}

	public String nonStaticMethod() {
		return "nonStaticMethodInside," + new ClassWithStaticMethodsOutsideTheBoundary().nonStaticMethodOutsideTheBoundary();
	}
}
