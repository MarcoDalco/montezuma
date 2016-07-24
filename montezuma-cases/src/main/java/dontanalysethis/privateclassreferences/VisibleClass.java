package dontanalysethis.privateclassreferences;

public class VisibleClass {
	public String getNameForClassCase() {
		return VisibleClass.class.getName();
	}
}
