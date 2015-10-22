package org.montezuma.test.traffic.writing;

import org.montezuma.test.traffic.MustMock;

import java.util.List;

public class MockingStrategy {
	private final List<String>	dontMockRegexList;

	public MockingStrategy(List<String> dontMockRegexList) {
		this.dontMockRegexList = dontMockRegexList;
	}

	boolean shouldStub(final Class<?> targetClazz) {
		boolean shouldMock = true;
		for (String dontMockPattern : dontMockRegexList) {
			if (targetClazz.getCanonicalName().matches(dontMockPattern)) {
				shouldMock = false;
				break;
			}
		}
		return shouldMock;
	}

	boolean mustStub(final Object arg) {
		return (arg instanceof MustMock);
	}

}
