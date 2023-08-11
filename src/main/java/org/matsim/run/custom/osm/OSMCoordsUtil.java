package org.matsim.run.custom.osm;

public class OSMCoordsUtil {

	public static final float GLOBAL_RADIUS = 6378137.0f;

	public static Vec2d toDegrees(Vec2d pos) {
		double lat = Math.toDegrees(Math.atan(Math.exp(pos.y / GLOBAL_RADIUS)) * 2 - Math.PI / 2);
		double lon = Math.toDegrees(pos.x / GLOBAL_RADIUS);
		return new Vec2d(lon, lat);
	}

	public static Vec2d toPosition(OSMNode node) {
		return toPosition(node.getCoords().x, node.getCoords().y);
	}

	public static BoundingBox2d toPosition(BoundingBox2d degrees) {
		return new BoundingBox2d(toPosition(degrees.min), toPosition(degrees.max));
	}

	public static Vec2d toPosition(double lon, double lat) {
		double y =  GLOBAL_RADIUS * Math.log((Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)));
		double x =  Math.toRadians(lon) * GLOBAL_RADIUS;
		return new Vec2d(x, y);
	}

	public static Vec2d toPosition(Vec2d degrees) {
		return toPosition(degrees.x, degrees.y);
	}

}
