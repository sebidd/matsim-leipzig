package org.matsim.run.custom;

import org.matsim.api.core.v01.Coord;
import org.matsim.run.custom.LAScoringFunction.LinkValidation;

import de.sebidd.base.io.osm.OSMParser;
import de.sebidd.base.math.geom.Vec2d;
import de.sebidd.rail.base.component.Polygon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LAModule {

	private final Set<Polygon> polygon_set;
	
	public LAModule(String path){
		this.polygon_set = new HashSet<>();
		OSMParser parser = new OSMParser();
	}

	public Polygon getContainingPolygon(Vec2d pos) {
		for(Polygon p : polygon_set) {
			if(p.contains(pos)) return p;
		}
		return null;
	}

	public Set<Polygon> getPolygons() {
		return this.polygon_set;
	}
	
	public boolean contains(Coord coord){
		for(Polygon poly : polygon_set) {
			if(poly.contains(new Vec2d(coord.getX(), coord.getY()))) {
				return true;
			}
		}
		return false;
	}
}
