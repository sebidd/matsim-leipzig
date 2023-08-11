package org.matsim.run.custom.osm;

import java.util.function.Function;

public enum OSMCoords {

	Degrees((e) -> {return e;}), ProjectedMercator84((e) -> {
		return OSMCoordsUtil.toPosition(e);
	});

	public final Function<Vec2d, Vec2d> transformation_func;

	private OSMCoords(Function<Vec2d, Vec2d> transformation_func) {
		this.transformation_func = transformation_func;
	}

	public Vec2d transform(Vec2d in) {
		return transformation_func.apply(in);
	}

}
