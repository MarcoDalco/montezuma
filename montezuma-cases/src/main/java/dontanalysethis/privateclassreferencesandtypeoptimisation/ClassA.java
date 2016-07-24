package dontanalysethis.privateclassreferencesandtypeoptimisation;

public class ClassA {
	public String getNameForClassCase() {
		return ClassA.class.getName();
	}
}
