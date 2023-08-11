package org.matsim.run.custom.osm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;


/**
 * A set of {@link OSMElement OSMElements} composing a closed data set. The dataset also keeps track of the next available IDs, as new OSMElements can be loaded into a dataset.
 * As the set contains information of all its OSMElements, all utility functions which manipulate elements are located here.
 * @author Sebastian
 *
 */
public class OSMDataset {

	private static OSMDataset CURRENT;

	public static OSMDataset getCurrent() {
		return CURRENT;
	}

	public static void setCurrent(OSMDataset current) {
		if(current != null) CURRENT = current;
	}

	public static final String OSM_VERSION = "0.6";

	public String version;

	public final Set<OSMRelation> relation_set;
	public final Set<OSMWay> way_set;
	public final Set<OSMNode> node_set;

	public long node_id_gen;
	public long way_id_gen;
	public long relation_id_gen;

	public OSMDataset(String version, Set<OSMNode> node_set, Set<OSMWay> way_set, Set<OSMRelation> relation_set, long node_id_gen, long way_id_gen, long relation_id_gen) {
		this.node_set = node_set;
		this.way_set = way_set;
		this.relation_set = relation_set;
		this.node_id_gen = node_id_gen;
		this.way_id_gen = way_id_gen;
		this.relation_id_gen = relation_id_gen;
		this.version = version;
		if(CURRENT == null) setCurrent(this);
	}

	public static OSMDataset fromElements(String version, Set<OSMElement> element_set) {
		Set<OSMNode> node_set = new HashSet<>();
		Set<OSMWay> way_set = new HashSet<>();
		Set<OSMRelation> relation_set = new HashSet<>();

		long node_id_gen = 0;
		long way_id_gen = 0;
		long rel_id_gen = 0;

		for (OSMElement element : element_set) {
			if (element instanceof OSMNode) {
				OSMNode n = (OSMNode) element;
				node_set.add(n);
				if (n.getID() < node_id_gen) node_id_gen = n.getID();
			} else if (element instanceof OSMWay) {
				OSMWay w = (OSMWay) element;
				way_set.add(w);
				if (w.getID() < way_id_gen) way_id_gen = w.getID();
			} else if (element instanceof OSMRelation) {
				OSMRelation r = (OSMRelation) element;
				relation_set.add(r);
				if (r.getID() < rel_id_gen) rel_id_gen = r.getID();
			}
		}

		return new OSMDataset(version, node_set, way_set, relation_set, node_id_gen, way_id_gen, rel_id_gen);
	}

	public static OSMDataset fromEmpty(String version) {
		return new OSMDataset(version, new HashSet<>(), new HashSet<>(), new HashSet<>(), 0, 0, 0);
	}

	public static OSMDataset fromFilteredDataset(OSMDataset dataset, Function<OSMElement, Boolean> filter_func) {
		return OSMDataset.fromElements(dataset.version, dataset.filterAll(filter_func));
	}

	public Set<OSMElement> filterAll(Function<OSMElement, Boolean> query_func) {
		Set<OSMElement> result = new HashSet<>();
		for(OSMNode node : node_set) {
			if(query_func.apply(node)) result.add(node);
		}
		for(OSMWay way : way_set) {
			if(query_func.apply(way)) result.add(way);
		}
		for(OSMRelation relation : relation_set) {
			if(query_func.apply(relation)) result.add(relation);
		}
		return result;
	}

	public Set<OSMWay> getWays() {
		return this.way_set;
	}

	public Set<OSMNode> getNodes(){
		return this.node_set;
	}

	public OSMNode getNodeByID(long id) {
		for(OSMNode node : node_set) {
			if(node.getID() == id) return node;
		}
		return null;
	}

	public OSMWay getWayByID(long id) {
		for(OSMWay way : way_set) {
			if(way.getID() == id) return way;
		}
		return null;
	}

	public OSMRelation getRelationByID(long id) {
		for(OSMRelation relation : relation_set) {
			if(relation.getID() == id) return relation;
		}
		return null;
	}

	public void remove(OSMNode node) {
		for (OSMWay other : way_set) {
			if (other.ref_list.contains(node)) {
				throw new RuntimeException("The node which is scheduled to be removed is contained in another way: " + other.getID() + ".");
			}
		}
		for (OSMRelation other : relation_set) {
			for (OSMRelation.Member member : other.member_list) {
				if (member.element == node) {
					throw new RuntimeException("The way which is scheduled to be removed is contained in another relation: " + other.getID() + ".");
				}
			}
		}
	}

	public void add(OSMNode node) {
		if(node.getID() == 0) node.setID(nextNodeID());
		node_set.add(node);
	}

	public void add(OSMWay way) {
		if(way.getID() == 0) way.setID(nextWayID());
		way_set.add(way);
	}

	public void add(OSMRelation relation) {
		if(relation.getID() == 0) relation.setID(nextRelationID());
		relation_set.add(relation);
	}

	public void remove(OSMWay way) {
		for(OSMRelation other : relation_set) {
			for(OSMRelation.Member member : other.member_list) {
				if(member.element == way){
					throw new RuntimeException("The way which is scheduled to be removed is contained in another relation: " + other.getID() + ".");
				}
			}
		}
	}

	public Set<OSMElement> getContainers(OSMNode node) {
		Set<OSMElement> set = new HashSet<>();
		for(OSMWay other : way_set) {
			if(other.ref_list.contains(node)) {
				set.add(other);
			}
		}
		for(OSMRelation other : relation_set) {
			for(OSMRelation.Member member : other.member_list) {
				if(member.element == node){
					set.add(other);
				}
			}
		}
		return set;
	}

	public Set<OSMElement> getContainers(OSMWay way) {
		Set<OSMElement> set = new HashSet<>();
		for(OSMRelation other : relation_set) {
			for(OSMRelation.Member member : other.member_list) {
				if(member.element == way){
					set.add(other);
				}
			}
		}
		return set;
	}

	public Set<OSMElement> getContainers(OSMRelation relation) {
		Set<OSMElement> set = new HashSet<>();
		for(OSMRelation other : relation_set) {
			if(other == relation) continue;
			for(OSMRelation.Member member : other.member_list) {
				if(member.element == relation){
					set.add(other);
				}
			}
		}
		return set;
	}

	public void remove(OSMRelation relation) {
		for(OSMRelation other : relation_set) {
			if(other == relation) continue;
			for(OSMRelation.Member member : other.member_list) {
				if(member.element == relation){
					throw new RuntimeException("The relation which is scheduled to be removed is contained in another relation: " + other.getID() + ".");
				}
			}
		}
	}

	public Set<OSMRelation> getRelations(){
		return this.relation_set;
	}

	public Optional<Set<OSMWay>> splitWay(OSMWay way, OSMNode node) {
		if(!way.ref_list.contains(node) || way.isClosed()) return Optional.empty();
		int index = way.ref_list.indexOf(node);
		if(index < 0 || index > way.ref_list.size() - 1) return Optional.empty();

		OSMWay w1 = new OSMWay(nextWayID(), way.attribs.copy());
		OSMWay w2 = new OSMWay(nextWayID(), way.attribs.copy());

		add(w1);
		add(w2);
		remove(way);

		return Optional.of(Set.of(w1, w2));
	}


	private long nextWayID() {
		return --way_id_gen;
	}

	private long nextNodeID() {
		return --node_id_gen;
	}

	private long nextRelationID() {
		return --relation_id_gen;
	}

	public BoundingBox2d getBounds() {
		double xmin = Float.MAX_VALUE;
		double xmax = -Float.MAX_VALUE;
		double ymin = Float.MAX_VALUE;
		double ymax = -Float.MAX_VALUE;
		for(OSMNode e : node_set) {
			if(e.getCoords().x < xmin) xmin = e.getCoords().x;
			else if(e.getCoords().x > xmax) xmax = e.getCoords().x;
			if(e.getCoords().y < ymin) ymin = e.getCoords().y;
			else if(e.getCoords().y > ymax) ymax = e.getCoords().y;
		}
		return new BoundingBox2d(xmin, ymin, xmax, ymax);
	}
}
