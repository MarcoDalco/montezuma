package dontanalysethis;

import java.io.Serializable;

public class DummyThirdParty implements Serializable {
	private static final long	serialVersionUID	= 8206120199498544990L;
	private final String			tail;

	public DummyThirdParty(String tail) {
		super();
		this.tail = tail;
	}

	public String enhance(String toEnhance) {
		return toEnhance + tail;
	}
}
