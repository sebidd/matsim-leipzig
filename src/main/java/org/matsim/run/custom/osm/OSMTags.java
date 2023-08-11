package org.matsim.run.custom.osm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * A map containing all tags for an {@link OSMElement}.
 * @author Sebastian
 *
 */
public final class OSMTags implements Iterable<Entry<String, String>>{

	public final Map<String, String> pair_map;

	public OSMTags() {
		this.pair_map = new HashMap<>();
	}

	public OSMTags(OSMTags other) {
		this.pair_map = new HashMap<>(other.pair_map);
	}

	public OSMTags(Map<String, String> pair_map) {
		this.pair_map = pair_map;
	}

	public OSMTags copy() {
		return new OSMTags(this);
	}

	public String getTag(String key){
		String opt = pair_map.get(key);
		return opt != null ? opt : "";
	}

	public boolean hasTag(String key) {
		return pair_map.get(key) != null;
	}

	public void setTag(String key, String value) {
		if(key == null || key.isBlank()) pair_map.remove(key);
		else pair_map.put(key, value);
	}

	public void removeTag(String key) {
		pair_map.remove(key);
	}

	@Override
	public String toString() {
		return pair_map.toString();
	}

	public boolean containsTag(String key) {
		return pair_map.containsKey(key);
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return pair_map.entrySet().iterator();
	}
}
