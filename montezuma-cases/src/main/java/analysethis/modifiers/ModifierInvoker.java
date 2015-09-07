package analysethis.modifiers;

public class ModifierInvoker {
	public int invokeModifiersCase() {
		int sum = 0;
		sum += new ModifiersCase(1).protectedMethod();
		sum += new ModifiersCase(1L).publicMethod();
		sum += new ModifiersCase("1").packageAccessMethod();
		sum += ModifiersCase.invokeThePrivateStuff();
		System.out.println("The sum is: " + sum);
		return sum;
	}
}
