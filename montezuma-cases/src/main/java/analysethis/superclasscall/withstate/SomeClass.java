package analysethis.superclasscall.withstate;

public class SomeClass extends SuperClass {
	public int getState() {
		super.changeState();
		return state;
	}
}
