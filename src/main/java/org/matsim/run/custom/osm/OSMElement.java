package org.matsim.run.custom.osm;

/**
 * The base class of all OSM elements.
 * Each OSMElement has a respective {@link OSMTags} instance.
 *
 * </br>
 * Subclasses in respect to the OSM configuration are:
 *
 * <ul>
 * 	<li>{@link OSMNode}</li>
 * 	<li>{@link OSMWay}</li>
 * 	<li>{@link OSMRelation}</li>
 * </ul>
 *
 * @author Sebastian
 *
 */
public abstract class OSMElement {

	private long id;
	public OSMTags attribs;

	public OSMElement(long id, OSMTags tags) {
		this.id = id;
		if(tags == null) this.attribs = new OSMTags();
		else this.attribs = tags;
	}

	public long getID() {
		return this.id;
	}

	public void setID(long id) {
		if(this.id != 0) return;
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OSMElement) {
			OSMElement o = (OSMElement) other;
			return o.getClass().getName().matches(this.getClass().getName()) && o.id == this.id;
		}
		return false;
	}

	public OSMTags getTags() {
		return this.attribs;
	}

	@Override
	public int hashCode() {
		return (int) (id % Integer.MAX_VALUE);
	}

	@Override
	public String toString() {
		return "[OSMObject id:'" + id + "', attribs: {" + attribs + "}]";
	}

	public BoundingBox2d getBounds() {
		return getBounds(OSMCoords.Degrees);
	}

	public abstract BoundingBox2d getBounds(OSMCoords system);

}
