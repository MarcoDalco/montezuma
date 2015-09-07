package analysethis.utils.math;

import dontanalysethis.DummyThirdParty;

public class MonitoredClass {
	private final String	message;

	public MonitoredClass(String aMessage) {
		super();
		this.message = aMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getMessageEnhancedBy(DummyThirdParty dummy) {
		return dummy.enhance(message);
	}

	public void printClass(Class<?> clazz) {
		System.out.println(clazz);
	}
}
