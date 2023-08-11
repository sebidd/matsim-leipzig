package org.matsim.run.custom;

import java.util.Set;

import org.locationtech.jts.geom.Geometry;

public class LocalAreaMap {

	private Set<Geometry> geomSet;

	public LocalAreaMap(String osmPath) {
		this.geomSet = LocalAreaUtils.loadGeometryFromOSM(osmPath);
	}

	public Set<Geometry> getGeometries() {
		return this.geomSet;
	}

}
