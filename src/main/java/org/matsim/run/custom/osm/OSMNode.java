package org.matsim.run.custom.osm;

/**
 * Class representing a node by means of OSM.
 * @author Sebastian
 *
 */
public class OSMNode extends OSMElement {

	private Vec2d pos;

	public OSMNode(long id, OSMTags tags) {
		super(id, tags);
	}

	public Vec2d getCoords() {
		return this.pos;
	}

	public void setCoords(Vec2d coords) {
		this.pos = coords;
	}

	public Vec2d getCoords(OSMCoords system) {
		return system.transform(pos);
	}

	public OSMNode copy() {
		return new OSMNode(this.getID(), super.getTags().copy());
	}

	@Override
	public String toString() {
		return "[OSMNode of " + super.toString() + "]";
	}

	@Override
	public BoundingBox2d getBounds(OSMCoords system) {
		Vec2d bounds = getCoords(system);
		return new BoundingBox2d(bounds, bounds);
	}

}
