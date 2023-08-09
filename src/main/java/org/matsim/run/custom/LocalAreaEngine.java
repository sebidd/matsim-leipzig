package org.matsim.run.custom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.run.custom.geom.LocalAreaUtils;

public class LocalAreaEngine implements IterationStartsListener, IterationEndsListener {

	private static LocalAreaEngine INSTANCE = null;
	
	public static final String DEBUG_PATH = "output/output-debug/local-area/";
	
	public static final Set<String> PROHIBITED_MODE_SET = Set.of("car");
	
	private Map<Id<Person>, Double> scoring_map;
	
	private Set<Geometry> geomSet;
	
	private LocalAreaEngine(String osmPath) {
		this.geomSet = LocalAreaUtils.loadGeometryFromOSM(osmPath);
	}
	
	public static LocalAreaEngine get() {
		if(INSTANCE == null) throw new RuntimeException("No LocalAreaEngine instance is available. Create a new one with LocalAreaEngine.create()");
		else return INSTANCE;
	}
	
	public static LocalAreaEngine create(String osmPath) {
		INSTANCE = new LocalAreaEngine(osmPath);
		return INSTANCE;
	}
	
	public void reset() {
		scoring_map.clear();
	}
	
	public static Set<String> getProhibitedModes() {
		return PROHIBITED_MODE_SET;
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		reset();
	}
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		StringBuilder builder = new StringBuilder();
		for(Entry<Id<Person>, Double> entry : scoring_map.entrySet()) {
			builder.append(entry.getKey() + " = " + entry.getValue() + System.lineSeparator());
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(DEBUG_PATH + event.getIteration() + ".txt")));
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
