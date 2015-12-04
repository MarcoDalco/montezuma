package dontanalisethis.untestable.until.privateclassreferencesareworkedaround;

public class VisibleClass {
	public String getNameForClassCase() {
		return VisibleClass.class.getName();
	}
}
