package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analysethis.collections.CollectionsProvider;

public class CollectionsProviderTrafficRecorder {
	public static final String	COLLECTIONS_PROVIDER_RECORDING_SUBDIR	= "collectionsprovider";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new CollectionsProvider());
				RecordingAspect.turnOn();

				CollectionsProvider cut = new CollectionsProvider();

				List<String> arrayList = cut.getArrayList();
				List<String> linkedList = cut.getLinkedList();
				Set<String> hashSet = cut.getHashSet();
				Set<String> treeSet = cut.getTreeSet();
				Map<String, String> hashMap = cut.getHashMap();
				Map<String, String> treeMap = cut.getTreeMap();

				RecordingAspect.turnOff();
				System.out.println(arrayList);
				System.out.println(linkedList);
				System.out.println(hashSet);
				System.out.println(treeSet);
				System.out.println(hashMap);
				System.out.println(treeMap);
			}
		}, COLLECTIONS_PROVIDER_RECORDING_SUBDIR);
	}
}
