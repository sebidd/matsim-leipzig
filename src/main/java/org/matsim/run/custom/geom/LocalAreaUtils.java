package org.matsim.run.custom.geom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.run.custom.osm.OSMDataset;
import org.matsim.run.custom.osm.OSMRelation;
import org.matsim.run.custom.osm.OSMWay;
import org.matsim.run.custom.osm.OSMXML;
import org.matsim.run.custom.osm.Vec2d;

public final class LocalAreaUtils {

	private static final GeometryFactory geomFactory = new GeometryFactory();
	
	public static Set<Geometry> loadGeometryFromOSM(String path) {
		Set<Geometry> geomSet = new HashSet<>();
		OSMDataset dataset = OSMXML.load(path).get();

		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("GEOGCS[\"WGS 84\",\r\n"
				+ "    DATUM[\"WGS_1984\",\r\n"
				+ "        SPHEROID[\"WGS 84\",6378137,298.257223563,\r\n"
				+ "            AUTHORITY[\"EPSG\",\"7030\"]],\r\n"
				+ "        AUTHORITY[\"EPSG\",\"6326\"]],\r\n"
				+ "    PRIMEM[\"Greenwich\",0,\r\n"
				+ "        AUTHORITY[\"EPSG\",\"8901\"]],\r\n"
				+ "    UNIT[\"degree\",0.0174532925199433,\r\n"
				+ "        AUTHORITY[\"EPSG\",\"9122\"]],\r\n"
				+ "    AUTHORITY[\"EPSG\",\"4326\"]]", "PROJCS[\"ETRS89 / UTM zone 32N\",\r\n"
						+ "    GEOGCS[\"ETRS89\",\r\n"
						+ "        DATUM[\"European_Terrestrial_Reference_System_1989\",\r\n"
						+ "            SPHEROID[\"GRS 1980\",6378137,298.257222101,\r\n"
						+ "                AUTHORITY[\"EPSG\",\"7019\"]],\r\n"
						+ "            TOWGS84[0,0,0,0,0,0,0],\r\n"
						+ "            AUTHORITY[\"EPSG\",\"6258\"]],\r\n"
						+ "        PRIMEM[\"Greenwich\",0,\r\n"
						+ "            AUTHORITY[\"EPSG\",\"8901\"]],\r\n"
						+ "        UNIT[\"degree\",0.0174532925199433,\r\n"
						+ "            AUTHORITY[\"EPSG\",\"9122\"]],\r\n"
						+ "        AUTHORITY[\"EPSG\",\"4258\"]],\r\n"
						+ "    PROJECTION[\"Transverse_Mercator\"],\r\n"
						+ "    PARAMETER[\"latitude_of_origin\",0],\r\n"
						+ "    PARAMETER[\"central_meridian\",9],\r\n"
						+ "    PARAMETER[\"scale_factor\",0.9996],\r\n"
						+ "    PARAMETER[\"false_easting\",500000],\r\n"
						+ "    PARAMETER[\"false_northing\",0],\r\n"
						+ "    UNIT[\"metre\",1,\r\n"
						+ "        AUTHORITY[\"EPSG\",\"9001\"]],\r\n"
						+ "    AXIS[\"Easting\",EAST],\r\n"
						+ "    AXIS[\"Northing\",NORTH],\r\n"
						+ "    AUTHORITY[\"EPSG\",\"25832\"]]");
		for(OSMRelation relation : dataset.getRelations()) {
			if(relation.getTags().getTag("amenity").equals("parking")) {
				for(OSMRelation.Member member : relation) {
					if(member.role == OSMRelation.Role.Outer && member.element instanceof OSMWay) {
						OSMWay way = (OSMWay) member.element;
						Coordinate[] coordList = new Coordinate[way.getNodes().size()];
						for(int i = 0; i < coordList.length; i++) {
							Vec2d coordIn = way.getNodes().get(i).getCoords();
							Coord coordOut = transformation.transform(new Coord(coordIn.x, coordIn.y));
							coordList[i] = new Coordinate(coordOut.getX(), coordOut.getY());
						}
						geomSet.add(geomFactory.createPolygon(coordList));
						break;
					}
				}
			}
		}
		
		return geomSet;
	}
	
	public static Geometry getContainingGeometry(Coord coord, Set<Geometry> geomSet) {
		for(Geometry geom : geomSet) {
			if(contains(geom, coord)) return geom;
		}
		return null;
	}
	
	public static Set<Geometry> getContainingGeometries(Coord coord, Set<Geometry> geomSet){
		Set<Geometry> out = new HashSet<>();
		for(Geometry geom : geomSet) {
			if(contains(geom, coord)) out.add(geom);
		}
		return out;
	}
	
	public static double getContainedLength(Geometry geom, Link link) {
		LineString ls = asLinestring(link);
		Geometry intersection = ls.intersection(geom);
		System.out.println(intersection.getGeometryType());
		return 0;
	}
	
	public static LineString asLinestring(Link link) {
		return geomFactory.createLineString(new Coordinate[] {asPoint(link.getFromNode()).getCoordinate(), asPoint(link.getToNode()).getCoordinate()});
	}
	
	public static LineString asLinestring(Coord... coords) {
		Coordinate[] out = new Coordinate[coords.length];
		for(int i = 0; i < coords.length; i++) {
			out[i] = new Coordinate(coords[i].getX(), coords[i].getY());
		}
		return geomFactory.createLineString(out);
	}
	
	public static Point asPoint(Coord coord) {
		return geomFactory.createPoint(new Coordinate(coord.getX(), coord.getY()));
	}
	
	public static Point asPoint(Node node) {
		return asPoint(node.getCoord());
	}
	
	public static boolean contains(Geometry geom, Coord coord) {
		return geom.contains(asPoint(coord));
	}
	
}
