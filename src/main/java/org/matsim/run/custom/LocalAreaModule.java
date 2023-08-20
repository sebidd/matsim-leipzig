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

/**
 * The local area module class implements a singleton, retreived by the static method get(), which contains basic constants and state informations required for the implementation of local area penalty scoring.
 * Both interfaces IterationStartsListener and IterationEndsListener were implemented for debug purposes, as a BufferedWriter was used to store debug info into a separate file on the local drive.
 * @author Sebastian
 *
 */
public class LocalAreaModule extends AbstractModule implements IterationStartsListener, IterationEndsListener {

	/**
	 * Custom attribute key string to definne all links that are in local areas.
	 */
	public static final String LINK_IN_LOCAL_AREA_ATTRIBUTE = "LINK_IN_LOCAL_AREA";

	/**
	 * Debug only sum of all penalty scores during one iteration.
	 */
	public static double PENALTY_SUM = 0;
	
	/**
	 * String builder for debug purposes.
	 */
	private StringBuilder builder = null;
	
	/**
	 * Singleton instance holder of the local area module.
	 */
	private static LocalAreaModule INSTANCE = null;

	/**
	 * Getter function of the singleton instance.
	 * @return The singleton instance of LocalAreaModule.
	 */
	public static LocalAreaModule get(){
		if(INSTANCE == null) INSTANCE = new LocalAreaModule();
		return INSTANCE;
	}

	/**
	 * The transformation function, used for appliance on the accumulated local area time.
	 */
	private Function<Double, Double> scoringFunc = (e) -> {
		return Math.exp(e) - 1;
	};
	
	/**
	 * A prefix for storage of the output debug files.
	 */
	private final String functionIdentifier = "exp-a1-b-1";
	
	/**
	 * Default path prefix for debug purposes.
	 */
	public final String debugPath = "output/output-debug/local-area/";
	
	/**
	 * A set containing all modes with restricted through traffic access in respect to the local areas implemented.
	 * By default, it is only containing the mode 'car'. Other modes are possible to be added in the future.
	 */
	private final Set<String> prohibitedModeSet = Set.of("car");

	/**
	 * The custom local area scoring factory that is used to override the default scoring for agents.
	 */
	private final LocalAreaScoringFactory scoringff;

	/**
	 * A map containing all links within the network that are part of a local area, with their corresponding local areas as polygons.
	 */
	private Map<Id<Link>, Geometry> collMap;
	
	/**
	 * A set containing all local area polygons loaded by the OSMParser.
	 */
	private Set<Geometry> collSet;

	public LocalAreaModule(){
		this.scoringff = new LocalAreaScoringFactory();
		this.collMap = new HashMap<>();
		this.collSet = new HashSet<>();
	}

	/**
	 * A getter for all local area polygons
	 * @return All local area polygons as a set.
	 */
	public Set<Geometry> getCollSet(){
		return this.collSet;
	}

	public Map<Id<Link>, Geometry> getCollMap(){
		return this.collMap;
	}

	/**
	 * Sets the local areas of the module by parsing of an .osm file at given path.
	 * @param path The path of the .osm file to be parsed.
	 * @param network The corresponding MATSim network to be evaluated.
	 */
	public void setLocalAreas(String path, Network network){
		collMap.clear();
		collSet.clear();
		//Call to the helper class LocalAreaUtils.
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

	/**
	 * Stores the network file with calculated attributes for local areas. Made for debug purposes.
	 * @param network The network to be written into the local file system.
	 * @param path The output path of the network file.
	 */
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
