package org.matsim.run.custom;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scoring.ScoringFunctionFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class LocalAreaModule extends AbstractModule implements IterationStartsListener, IterationEndsListener {

	public static final String LINK_IN_LOCAL_AREA_ATTRIBUTE = "LINK_IN_LOCAL_AREA";

	public static double PENALTY_SUM = 0;
	
	private StringBuilder builder = null;
	private static LocalAreaModule INSTANCE = null;

	public static LocalAreaModule get(){
		if(INSTANCE == null) INSTANCE = new LocalAreaModule();
		return INSTANCE;
	}

	private Function<Double, Double> scoringFunc = (e) -> {
		return Math.exp(e) - 1;
	};
	private final String functionIdentifier = "exp-a1-b-1";
	
	public final String debugPath = "output/output-debug/local-area/";
	private final Set<String> prohibitedModeSet = Set.of("car");

	private final LocalAreaScoringFactory scoringff;

	private Map<Id<Link>, Geometry> collMap;
	private Set<Geometry> collSet;

	public LocalAreaModule(){
		this.scoringff = new LocalAreaScoringFactory();
		this.collMap = new HashMap<>();
		this.collSet = new HashSet<>();
	}

	public Set<Geometry> getCollSet(){
		return this.collSet;
	}

	public Map<Id<Link>, Geometry> getCollMap(){
		return this.collMap;
	}

	public void setLocalAreas(String path, Network network){
		collMap.clear();
		collSet.clear();
		collSet.addAll(LocalAreaUtils.loadGeometryFromOSM(path));

		for(Link link : network.getLinks().values()){
			Geometry containing = LocalAreaUtils.getContainingGeometryMin1(link, collSet);
			if(containing != null) {
				link.getAttributes().putAttribute(LINK_IN_LOCAL_AREA_ATTRIBUTE, "true");
				collMap.put(link.getId(), containing);
			}
			else {
				link.getAttributes().putAttribute(LINK_IN_LOCAL_AREA_ATTRIBUTE, "false");
				collMap.remove(link.getId());
			}
		}
	}

	public void storeNetworkAsFile(Network network, String path){
		NetworkUtils.writeNetwork(network, path);
	}
	
	public StringBuilder getBuilder() {
		return this.builder;
	}

	public LocalAreaScoringFactory getScoringFactory(){
		return this.scoringff;
	}

	public Function<Double, Double> getScoringFunction(){
		return this.scoringFunc;
	}

	public Set<String> getProhibitedModes(){
		return this.prohibitedModeSet;
	}

	@Override
	public void install() {
		addControlerListenerBinding().toInstance(this);
		bindScoringFunctionFactory().toInstance(scoringff);
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		builder.append("Sum: " + PENALTY_SUM + System.lineSeparator());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(debugPath + functionIdentifier + "-" + event.getIteration() + ".txt")));
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		PENALTY_SUM = 0;
		builder = new StringBuilder();
//		collMap.entrySet().forEach((e) -> {
//			builder.append(e.getKey() + " = " + Arrays.toString(e.getValue().getCoordinates()) + System.lineSeparator());
//		});
	}
}
