package org.montezuma.test.traffic.writing;

// This enumeration is not actually used, but is here as a reminder of possible configuration options for the test-writing tool
public enum TestWritingStrategy {
	ONE_TEST_CLASS_PER_INSTANCE, // Unsupported yet
	ONE_TEST_PER_INSTANCE_BUT_ONE_PER_CALL_FOR_STATELESS, // Default
	ONE_TEST_PER_INSTANCE_BUT_ONE_PER_CALL_FOR_SHALLOW_STATELESS,
	ONE_TEST_PER_INSTANCE
}
