package org.matsim.run.custom.osm;

import java.util.Set;

public class OSMPolygon {

	public Set<OSMPolygon> hole_set;

	public OSMPolygon() {

	}

	public float getArea() {
		return 0;
	}

	public boolean isInside(OSMPolygon other) {
		return false;
	}

	public float getCircumference() {
		return 0;
	}

}
