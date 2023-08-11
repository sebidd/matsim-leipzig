package org.matsim.run.custom.osm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public final class OSMXML {

	private OSMXML() {}

	public static final QName Q_ID = QName.valueOf("id");
	public static final QName Q_ACTION = QName.valueOf("action");
	public static final QName Q_LAT = QName.valueOf("lat");
	public static final QName Q_LON = QName.valueOf("lon");
	public static final QName Q_REF = QName.valueOf("ref");
	public static final QName Q_ROLE = QName.valueOf("role");
	public static final QName Q_TYPE = QName.valueOf("type");
	public static final QName Q_KEY = QName.valueOf("k");
	public static final QName Q_VALUE = QName.valueOf("v");
	public static final QName Q_VERSION = QName.valueOf("version");

	public static boolean write(OSMDataset set, String path) {
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();

		try {
			XMLStreamWriter writer = xmlof.createXMLStreamWriter(new FileOutputStream(path));

			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement("carto");
			writer.writeAttribute("version", set.version != null ? "1" : "");

			for(OSMNode node : set.node_set) {
				writer.writeStartElement("node");
				writer.writeAttribute("id", String.valueOf(node.getID()));
				writer.writeAttribute("lat", String.valueOf(node.getCoords().x));
				writer.writeAttribute("lon", String.valueOf(node.getCoords().y));
				for(var entry : node.getTags()) {
					writer.writeStartElement("tag");
					writer.writeAttribute("k", entry.getKey());
					writer.writeAttribute("v", entry.getValue());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			for(OSMWay way : set.way_set) {
				writer.writeStartElement("way");
				writer.writeAttribute("id", String.valueOf(way.getID()));

				for(OSMNode ref : way.ref_list) {
					writer.writeStartElement("nd");
					writer.writeAttribute("ref", String.valueOf(ref.getID()));
					writer.writeEndElement();
				}
				for(var entry : way.getTags()) {
					writer.writeStartElement("tag");
					writer.writeAttribute("k", entry.getKey());
					writer.writeAttribute("v", entry.getValue());
					writer.writeEndElement();
				}

				writer.writeEndElement();
			}

			for(OSMRelation relation : set.relation_set) {
				writer.writeStartElement("relation");
				writer.writeAttribute("id", String.valueOf(relation.getID()));

				for(OSMRelation.Member member : relation.member_list) {
					writer.writeStartElement("member");
					writer.writeAttribute("type", member.getType());
					writer.writeAttribute("ref", String.valueOf(member.element.getID()));
					writer.writeAttribute("role", member.role.identifier);
					writer.writeEndElement();
				}
				for(var entry : relation.getTags()) {
					writer.writeStartElement("tag");
					writer.writeAttribute("k", entry.getKey());
					writer.writeAttribute("v", entry.getValue());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			writer.writeEndElement();

			writer.writeEndDocument();

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	/**
	 * A method for creating an {@link OSMDataset} from a file of the OSM-XML format.
	 * @param path The path within the file system to the file to be read.
	 * @return
	 *
	 * @throws FileNotFoundException If the given path param is non existant.
	 * @throws XMLStreamException If the file is not in XML format or the XML within the file is malformed.
	 */
	public static Optional<OSMDataset> load(String path) {

		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = xmlif.createXMLEventReader(new FileInputStream(path));

			Map<Long, OSMNode> node_map = new HashMap<>();
			Map<Long, OSMWay> way_map = new HashMap<>();
			Map<Long, OSMRelation> relation_map = new HashMap<>();

			OSMElement last = null;

			String version = null;

			long node_lid = Long.MAX_VALUE;
			long way_lid = Long.MAX_VALUE;
			long relation_lid = Long.MAX_VALUE;

			while(reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if(event.isStartElement()) {
					StartElement start = (StartElement) event;
					switch(start.getName().getLocalPart()) {

					case "osm": {
						var v = start.getAttributeByName(Q_VERSION);
						if(v != null) version = v.getValue();
						else version = OSMDataset.OSM_VERSION;
						break;
					}

					case "node": {
						last = null;
						long id = Long.valueOf(start.getAttributeByName(Q_ID).getValue());
						float lat = Float.valueOf(start.getAttributeByName(Q_LAT).getValue());
						float lon = Float.valueOf(start.getAttributeByName(Q_LON).getValue());
						if(id < node_lid) node_lid = id;

						OSMNode n = new OSMNode(id, null);
						n.setCoords(new Vec2d(lon, lat));
						last = n;
						break;
					}
					case "way": {
						last = null;
						long id = Long.valueOf(start.getAttributeByName(Q_ID).getValue());
						if(id < way_lid) way_lid = id;

						OSMWay w = new OSMWay(id, null);

						last = w;
						break;
					}
					case "relation": {
						last = null;
						long id = Long.valueOf(start.getAttributeByName(Q_ID).getValue());
						if(id < relation_lid) relation_lid = id;

						OSMRelation r = new OSMRelation(id, null);

						last = r;
						break;
					}
					case "nd": {
						OSMWay w = (OSMWay) last;

						long ref = Long.valueOf(start.getAttributeByName(Q_REF).getValue());
						w.ref_list.add(node_map.get(ref));

						break;
					}
					case "tag": {

						String k = start.getAttributeByName(Q_KEY).getValue();
						String v = start.getAttributeByName(Q_VALUE).getValue();

						last.attribs.setTag(k, v);

						break;
					}
					case "member": {

						String type = start.getAttributeByName(Q_TYPE).getValue();
						long ref = Long.valueOf(start.getAttributeByName(Q_REF).getValue());
						String role = start.getAttributeByName(Q_ROLE).getValue();

						OSMElement ref_o = null;
						switch(type) {
						case "way": {
							ref_o = way_map.get(ref);
							break;
						}
						case "node": {
							ref_o = node_map.get(ref);
							break;
						}
						case "relation": {
							ref_o = relation_map.get(ref);
							break;
						}
						default: break;
						}

						OSMRelation.Member m = new OSMRelation.Member(ref_o, role.matches("inner") ? OSMRelation.Role.Inner : OSMRelation.Role.Outer);

						OSMRelation last_rel = (OSMRelation) last;

						last_rel.member_list.add(m);
						break;
					}
					default: System.err.println(start.getName().getLocalPart());;
					}
				} else if(event.isEndElement()) {
					EndElement end = (EndElement) event;
					switch(end.getName().getLocalPart()) {

					case "node": {
						node_map.put(last.getID(), (OSMNode) last);
						break;
					}
					case "way": {
						way_map.put(last.getID(), (OSMWay) last);
						break;
					}
					case "relation": {
						relation_map.put(last.getID(), (OSMRelation) last);
						break;
					}
					case "nd": {
						break;
					}
					case "tag": {
						break;
					}
					default: break;
					}

				}




			}

			reader.close();
			return Optional.of(new OSMDataset(version, new HashSet<>(node_map.values()), new HashSet<>(way_map.values()), new HashSet<>(relation_map.values()), node_lid, way_lid, relation_lid));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

}
