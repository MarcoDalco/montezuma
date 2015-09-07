package org.montezuma.test.traffic;

import java.io.Serializable;

public class MustMock implements Serializable {
	private static final long	serialVersionUID	= 3842117585979628581L;
	public final Class<?>			clazz;

	public MustMock(Object arg) {
		this.clazz = arg.getClass();
	}

}
