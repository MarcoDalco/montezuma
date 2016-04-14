package analysethis.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionsProvider {

	public List<String> getArrayList() {
		List<String> list = new ArrayList<>();

		list.add("First");
		list.add(null);
		list.add("Third");

		return list;
	}

	public List<String> getLinkedList() {
		List<String> list = new LinkedList<>();

		list.add("First");
		list.add(null);
		list.add("Third");

		return list;
	}

	public Set<String> getHashSet() {
		Set<String> set = new HashSet<>();

		set.add("First");
		set.add(null);
		set.add("Third");

		return set;
	}

	public Set<String> getTreeSet() {
		Set<String> set = new TreeSet<>();

		set.add("First");
		set.add("Second");

		return set;
	}

	public Map<String, String> getHashMap() {
		Map<String, String> map = new HashMap<>();

		map.put("FirstKey", "FirstValue");
		map.put(null, "NullKey-Value");
		map.put("NullValue-Key", null);
		map.put("FourthKey", "FourthValue");

		return map;
	}

	public Map<String, String> getTreeMap() {
		Map<String, String> map = new TreeMap<>();

		map.put("FirstKey", "FirstValue");
		map.put("NullValue-Key", null);
		map.put("ThirdKey", "ThirdValue");

		return map;
	}

}
