package org.montezuma.test.traffic;

import org.montezuma.test.traffic.serialisers.Deserialiser;
import org.montezuma.test.traffic.serialisers.SerialisationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficReader {
	protected static Deserialiser	deserialiser	= SerialisationFactory.getDeserialiser();

	public static Object[] getDeserialisedArgs(final byte[][] serialisedArgs) throws ClassNotFoundException, IOException {
		final int serialisedArgsLength = serialisedArgs.length;
		Object[] methodArgs = new Object[serialisedArgsLength];
		for (int i = 0; i < serialisedArgsLength; i++)
			methodArgs[i] = deserialiser.deserialise(serialisedArgs[i]);
		return methodArgs;
	}

	protected static void printInvocationDataSizes(final List<InvocationData> invocationDataList) {
		for (InvocationData invocationData : invocationDataList) {
			InvocationData.printSingleInvocationDataSize(invocationData);
		}
	}

	protected static Map<String, List<InvocationData>> loadInvocationDataForClass(final Class<?> class1, String recordingSubDir) throws FileNotFoundException, IOException, ClassNotFoundException {
		File baseDir = new File(Common.BASE_RECORDING_PATH, recordingSubDir);
		File[] recordingsForThatClass = baseDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(class1.getTypeName());
			}
		});
		Map<String, List<InvocationData>> invocationLists = new HashMap<>();
		for (File firstRecording : recordingsForThatClass) {
			String fileName = firstRecording.getName();
			String instanceId = fileName.substring(1 + fileName.lastIndexOf('@'));
			List<InvocationData> invocations = new ArrayList<>();
			try (FileInputStream fis = new FileInputStream(firstRecording)) {
				@SuppressWarnings("unchecked") List<InvocationData> deserialisedObjects = (List<InvocationData>) deserialiser.deserialiseAll(fis);
				for (Object o : deserialisedObjects) {
					assert (o instanceof InvocationData);
				}
				invocations = deserialisedObjects;
			}
			invocationLists.put(instanceId, invocations);
		}

		return invocationLists;
	}
}
