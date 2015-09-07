package org.montezuma.test.traffic.replaying.cases;

import analysethis.utils.math.MonitoredClass;

import org.montezuma.test.traffic.recording.cases.PassThroughClassTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class PassThroughClassTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = MonitoredClass.class;
		String recordingSuDdir = PassThroughClassTrafficRecorder.PASSTHROUGH_CLASS_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
