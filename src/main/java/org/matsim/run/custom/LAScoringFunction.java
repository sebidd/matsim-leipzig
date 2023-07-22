package org.matsim.run.custom;

import de.sebidd.base.math.geom.Vec2d;
import de.sebidd.rail.base.component.Line;
import de.sebidd.rail.base.component.Polygon;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.scoring.SumScoringFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LAScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
	
	public static final class LinkNotification {
		
		public static enum Type {
			Enter, Leave;
		}
		
		private final Link link;
		private final double time;
		private final double progress;
		private final Type type;
		
		public LinkNotification(Link link, Type type, double time, double progress) {
			this.link = link;
			this.type = type;
			this.time = time;
			this.progress = progress;
		}
		
		public Link getLink() {
			return this.link;
		}
		
		public double getTime() {
			return this.time;
		}
		
		public double getProgress() {
			return this.progress;
		}
		
		public Type getType() {
			return this.type;
		}
		
		public Vec2d getPosition() {
			final Coord start = link.getFromNode().getCoord();
			final Coord end = link.getToNode().getCoord();
			return new Vec2d(start.getX() + getProgress() * (end.getX() - start.getX()), start.getY() + getProgress() * (end.getY() - start.getY()));
		}
		
	}
	
	public static final class LinkValidation {
		
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
		
		public Line asLine() {
			return new Line(start.getPosition(), end.getPosition());
		}
		
		public double getDeltaTime() {
			return end.getTime() - start.getTime();
		}
		
		public Map<Polygon, Double> getPolygonScoring(LAModule module) {
			Map<Polygon, Double> out = new HashMap<>();
			Line line = asLine();
			
			for(Polygon poly : module.getPolygons()) {
				Set<Vec2d> intersections = poly.getOuter().getIntersections(line);
				if(intersections.isEmpty()) {
					final boolean in_poly = poly.contains(line.getA());
					if(in_poly) {
						out.put(poly, getDeltaTime());
					} else {
						continue;
					}
				} else {
					List<Vec2d> intersection_list = new ArrayList<>(intersections);
					intersection_list.sort((e, f) -> {
						double pa = line.getProgress(e);
						double pb = line.getProgress(f);
						return Double.compare(pa, pb);
					});
					
				}
			}
			
			return out;
		}
		
	}
	
	private LAModule module;

	public final Function<Double, Double> scoring_validator;

	private Network network;

	private Vec2d start_pos;
	private Vec2d end_pos;
	
	private List<LinkNotification> link_notification_list;
	
	private boolean inVehicle = false;
	
	public LAScoringFunction(Function<Double, Double> scoring_validator){
		this.module = new LAModule("lorem ipsum");
		this.scoring_validator = scoring_validator != null ? scoring_validator : (e) -> {return 0d;};
	}

	public boolean linkInside(Link link){
		return module.contains(link.getFromNode().getCoord()) || module.contains(link.getToNode().getCoord());
	}

	@Override
	public void handleEvent(Event event) {
		
		if(event instanceof LinkEnterEvent) {
			handleLinkEnter((LinkEnterEvent) event);
		} else if(event instanceof LinkLeaveEvent){
			handleLinkLeave((LinkLeaveEvent) event);
		} else if(event instanceof VehicleEntersTrafficEvent){
			handleVehicleEnter((VehicleEntersTrafficEvent) event);
		} else if(event instanceof VehicleLeavesTrafficEvent){
			handleVehicleLeave((VehicleLeavesTrafficEvent) event);
		} else if(event instanceof ActivityEndEvent){
			handleLegStart((ActivityEndEvent) event);
		} else if(event instanceof ActivityStartEvent){
			handleLegEnd((ActivityStartEvent) event);
		}
	}
	
	private void handleLinkEnter(LinkEnterEvent event) {
		if(!inVehicle) return;
		Link current_link = network.getLinks().get(event.getLinkId());
		link_notification_list.add(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), 0));
	}
	
	private void handleLinkLeave(LinkLeaveEvent event) {
		if(!inVehicle) return;
		Link current_link = network.getLinks().get(event.getLinkId());
		link_notification_list.add(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), 1));
	}
	
	private void handleVehicleEnter(VehicleEntersTrafficEvent event) {
		Link current_link = network.getLinks().get(event.getLinkId());
		link_notification_list.add(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), event.getRelativePositionOnLink()));
		this.inVehicle = true;
	}
	
	private void handleVehicleLeave(VehicleLeavesTrafficEvent event) {
		Link current_link = network.getLinks().get(event.getLinkId());
		link_notification_list.add(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), event.getRelativePositionOnLink()));
		this.inVehicle = false;
	}
	
	private void handleLegStart(ActivityEndEvent event) {
		this.start_pos = new Vec2d(event.getCoord().getX(), event.getCoord().getY());
	}
	
	private void handleLegEnd(ActivityStartEvent event) {
		this.end_pos = new Vec2d(event.getCoord().getX(), event.getCoord().getY());
	}

	private double getPenaltyTime() {
		double score = 0;
		
		Polygon start_poly = module.getContainingPolygon(start_pos);
		Polygon end_poly = module.getContainingPolygon(end_pos);
		
		List<LinkValidation> validation_list = new ArrayList<>(link_notification_list.size() / 2);
		
		//Fasst die einzelnen Events bezüglich Betreten und Verlassen von Links in zeitlich, räumliche Abschnitte zusammen.
		for(int i = 0; i < link_notification_list.size(); i+=2) {
			validation_list.add(new LinkValidation(link_notification_list.get(i), link_notification_list.get(i + 1)));
		}
		
		for(LinkValidation validation : validation_list) {
			Map<Polygon, Double> poly_scoring = validation.getPolygonScoring(module);
			for(Entry<Polygon, Double> entry : poly_scoring.entrySet()) {
				if(entry.getKey() == start_poly || entry.getKey() == end_poly) continue;
				score += entry.getValue();
			}
		}
		
		return score;
	}
	
	@Override
	public void finish() {
		
	}

	@Override
	public double getScore() {
		return scoring_validator.apply(getPenaltyTime());
	}
}
