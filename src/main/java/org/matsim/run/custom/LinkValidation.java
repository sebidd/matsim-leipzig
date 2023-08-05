package org.matsim.run.custom;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.matsim.run.custom.geom.LocalChecker;

public final class LinkValidation {
		
		private final LinkNotification start;
		private final LinkNotification end;
		
		public LinkValidation(LinkNotification start, LinkNotification end) {
			if(start.link != end.link) throw new RuntimeException("Anfangs- und Endlinks für die Linkvalidation stimmen nicht überein!");
			this.start = start;
			this.end = end;
		}
		
		public LinkNotification getStartNotification() {
			return this.start;
		}
		
		public LinkNotification getEndNotification() {
			return this.end;
		}
		
		public LineString asLine() {
			return LocalChecker.asLinestring(start.getPosition(), end.getPosition());
		}
		
		public double getDeltaTime() {
			return end.getTime() - start.getTime();
		}
		
		public Map<Geometry, Double> getPolygonScoring(LAModule module) {
			Map<Geometry, Double> out = new HashMap<>();
			
			
//			for(Polygon poly : module.getPolygons()) {
//				Set<Vec2d> intersections = poly.getOuter().getIntersections(line);
//				if(intersections.isEmpty()) {
//					final boolean in_poly = poly.contains(line.getA());
//					if(in_poly) {
//						out.put(poly, getDeltaTime());
//					} else {
//						continue;
//					}
//				} else {
//					List<Vec2d> intersection_list = new ArrayList<>(intersections);
//					intersection_list.sort((e, f) -> {
//						double pa = line.getProgress(e);
//						double pb = line.getProgress(f);
//						return Double.compare(pa, pb);
//					});
//					
//				}
//			}
			
			return out;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LinkValidation[" + "s-time=" + start.getTime() + ", s-progress=" + start.getProgress() + ", e-time=" + end.getTime() + ", e-progress=" + end.getProgress() + "]");
			return builder.toString();
		}
		
	}