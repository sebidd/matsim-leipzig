package org.matsim.run.custom.osm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Class representing a relation by means of OSM.
 * @author Sebastian
 *
 */
public class OSMRelation extends OSMElement implements Iterable<OSMRelation.Member> {

	/**
	 * The role of a member element, as either {@literal Outer} or {@value Inner}.
	 * @author Sebastian
	 *
	 */
	public static enum Role {
		Inner("inner"), Outer("outer");

		public final String identifier;

		private Role(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public String toString() {
			return identifier;
		}
	}

	/**
	 * A class resembling members of an {@link OSMRelation} by combining the underlying {@link OSMElement} and its role.
	 * @author Sebastian
	 *
	 */
	public static final class Member {

		public OSMElement element;
		public Role role;

		public Member(OSMElement object, Role role) {
			if(object == null) return;
			if(!(object instanceof OSMElement)) return;
			this.element = object;
			this.role = role;
		}

		public Member(OSMElement object) {
			this(object, Role.Outer);
		}

		public String getType() {
			if(element instanceof OSMNode) return "node";
			if(element instanceof OSMWay) return "way";
			if(element instanceof OSMRelation) return "relation";
			else throw new RuntimeException("The type of the member element is undefined.");
		}

		@Override
		public String toString() {
			String type = null;
			if(element instanceof OSMNode) type = "node";
			else if(element instanceof OSMWay) type = "way";
			else if(element instanceof OSMRelation) type = "relation";
			return "[OSMMember type='" + type + "' id='" + element.getID() + "', role='" + role + "']";
		}

	}

	/**
	 * The list of members of the relation in order of their appearance.
	 */
	public List<Member> member_list;

	public OSMRelation(long id, OSMTags tags) {
		super(id, tags);
		this.member_list = new ArrayList<>();
	}

	/**
	 * Checks if the relation is a multipolygon, that is, if it contains a tag with the following configuration: "type"="multipolygon".
	 * @return A boolean indicating if the relation is a multipolygon by means of OSM.
	 */
	public boolean isMultipolygon() {
		String tag = getTags().getTag("type");
		if(tag.matches("multipolygon")) return true;
		return false;
	}

	//TODO A relation can be composed of multiple closed polygons.
	@Deprecated
	public boolean isClosed() {
		for(Member member : member_list) {
			if(member.element instanceof OSMWay) {


				return false;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Returns all member elements of this relation as a list of {@link OSMElement OSMElements}, that is without their role within the relation.
	 * @return A list of {@link OSMElement OSMElements}.
	 */
	public List<OSMElement> getMemberElements() {
		List<OSMElement> elements = new ArrayList<>(member_list.size());
		for(Member member : member_list) {
			elements.add(member.element);
		}
		return elements;
	}

//	public List<OSMPolygon> getPolygons() {
//		List<OSMWay> current_list = new ArrayList<>();
//		for(Member member : member_list) {
//			if(member.element instanceof OSMWay) {
//				OSMWay w = (OSMWay) member.element;
//
//				if(!current_list.isEmpty()) {
//
//				} else {
//					current_list.add(w);
//				}
//
//				OSMNode a1 = current_list.get(0).getFirstNode();
//				OSMNode a2 = current_list.get(0).getLastNode();
//				OSMNode b1 = current_list.get(current_list.size() - 1).getFirstNode();
//				OSMNode b2 = current_list.get(current_list.size() - 1).getLastNode();
//
//				if(a1 == b1 || a1 == b2 || a2 == b1 || a2 == b2) {
//					System.out.println(current_list);
//					current_list.clear();
//				}
//			}
//		}
//	}

	@Override
	public Iterator<OSMRelation.Member> iterator() {
		return member_list.iterator();
	}

	@Override
	public BoundingBox2d getBounds(OSMCoords system) {
		Set<BoundingBox2d> bounds = new HashSet<>();
		for(OSMElement element : getMemberElements()) {
			bounds.add(element.getBounds());
		}
		return BoundingBox2d.fromBoundsSet(bounds);
	}

	@Override
	public String toString() {
		return "[OSMRelation of " + super.toString() + " members: '" + member_list + "']";
	}

}
