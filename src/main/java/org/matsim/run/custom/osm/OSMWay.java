package org.matsim.run.custom.osm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class representing a way by means of OSM.
 * @author Sebastian
 *
 */
public class OSMWay extends OSMElement implements Iterable<OSMNode> {

	public List<OSMNode> ref_list;

	public OSMWay(long id, OSMTags tags) {
		super(id, tags);
		this.ref_list = new ArrayList<>();
	}

	@Override
	public Iterator<OSMNode> iterator() {
		return ref_list.iterator();
	}

	@Override
	public String toString() {
		return "[OSMWay of " + super.toString() + " refs: '" + ref_list + "']";
	}

	public OSMNode getNodeAtIndex(int index) {
		return ref_list.get(index);
	}

	@Override
	public BoundingBox2d getBounds(OSMCoords system) {
		List<Vec2d> coord_list = new ArrayList<>(ref_list.size());
		for(OSMNode node : ref_list) {
			coord_list.add(node.getCoords(system));
		}
		return BoundingBox2d.fromVecList(coord_list);
	}

	public List<OSMNode> getNodes(){
		return this.ref_list;
	}

	public List<Vec2d> asCoords() {
		return asCoords(OSMCoords.Degrees);
	}

	public List<Vec2d> asCoords(OSMCoords system) {
		List<Vec2d> points = new ArrayList<>(ref_list.size());
		for(OSMNode node : ref_list) {
			points.add(node.getCoords(system));
		}
		return points;
	}

	/*
	 * TODO The close check only checks if first and last node are the same.
	 * Cases, where the same node is contained multiple times within a way without being first or last order are not checked, although they can describe a closed way.
	 */
	/**
	 *
	 * @return
	 */
	public boolean isClosed() {
		return ref_list.get(0) == ref_list.get(ref_list.size() - 1);
	}

	//TODO
	public boolean isMultipolygon() {
		return isClosed();
	}

	public OSMNode getLastNode() {
		return ref_list.get(ref_list.size() - 1);
	}

	public OSMNode getFirstNode() {
		return ref_list.get(0);
	}

	public boolean joinsLastWithFirst(OSMWay next) {
		return next.getFirstNode() == getLastNode();
	}

	public boolean joinsLastWithLast(OSMWay next) {
		return next.getLastNode() == getLastNode();
	}

	public boolean joinsLastWithAny(OSMWay next) {
		return next.ref_list.contains(getLastNode());
	}

	public boolean joinsFirstWithAny(OSMWay next) {
		return next.ref_list.contains(getFirstNode());
	}

	public float getLength() {
		return getLengthBetweenIndices(0, ref_list.size() - 1);
	}

	public Set<OSMWay> getOtherWaysOfNodeIndexed(int index) {
		OSMNode last = getNodeAtIndex(index);
		Set<OSMWay> out = new HashSet<>();
		Set<OSMElement> candidate_set = OSMDataset.getCurrent().getContainers(last);
		for(OSMElement e : candidate_set) {
			if(e != this && e instanceof OSMWay) {
				out.add((OSMWay) e);
			}
		}
		return out;
	}

	public Set<OSMWay> splitAtIndex(int index) {
		if(index < 1 || index > ref_list.size() - 2) Set.of(this);
		return null;
	}

	public Set<OSMWay> getOtherWaysOfLastNode(){
		return getOtherWaysOfNodeIndexed(ref_list.size() - 1);
	}

	public Set<OSMWay> getOtherWaysOfFirstNode(){
		return getOtherWaysOfNodeIndexed(0);
	}

	public float getLengthBetweenIndices(int i0, int i1) {
		float sum = 0;
		Vec2d n_curr = OSMCoordsUtil.toPosition(ref_list.get(Math.min(i0, i1)));
		for(int i = Math.min(i0, i1); i < Math.max(i0, i1) + 1 - 1; i++) {
			Vec2d next = OSMCoordsUtil.toPosition(ref_list.get(i + 1));
			sum += Vec2d.sub(next, n_curr).getLength();
			n_curr = next;
		}
		return sum;
	}

}
