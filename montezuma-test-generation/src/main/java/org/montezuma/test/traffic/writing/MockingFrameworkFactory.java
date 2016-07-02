package org.montezuma.test.traffic.writing;

public class MockingFrameworkFactory {
	private static MockingFramework mockingFramework = new JMockitFramework();

	static MockingFramework getMockingFramework() {
		return mockingFramework;
	}

	public static void setMockingFramework(MockingFramework mockingFramework) {
		MockingFrameworkFactory.mockingFramework = mockingFramework;
	}
}
