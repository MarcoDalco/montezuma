package org.montezuma.test.traffic.replaying.cases;

import org.montezuma.test.traffic.recording.cases.TimeConsumerTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.timefunctionsareworkedaround.TimeConsumer;

public class TimeConsumingTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = TimeConsumer.class;
		String recordingSuDdir = TimeConsumerTrafficRecorder.TIME_CONSUMER_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
