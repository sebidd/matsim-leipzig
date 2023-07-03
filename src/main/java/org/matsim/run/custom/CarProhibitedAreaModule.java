package org.matsim.run.custom;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CarProhibitedAreaModule {

	private final Set<Polygon> polygon_set;
	public CarProhibitedAreaModule(String path){
		this.polygon_set = new HashSet<>();
	}

	public static final class Polygon {

	}

	public Optional<Polygon> getCoveringPolygon(Coord coord){
		return Optional.empty();
	}

	public boolean contains(Coord coord){
		return false;
	}
}
