package org.matsim.run.custom;

import de.sebidd.base.math.geom.Vec2d;
import de.sebidd.rail.base.component.Line;
import de.sebidd.rail.base.component.Polygon;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;

import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LAHandler implements ActivityEndEventHandler, ActivityStartEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler, LinkLeaveEventHandler {
	
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
		
		@Override
		public String toString() {
			return "LinkNotification[LinkID=" + link.getId() + " time=" + time + ", progress=" + progress + ", type=" + type + "]";
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
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LinkValidation[" + "s-time=" + start.getTime() + ", s-progress=" + start.getProgress() + ", e-time=" + end.getTime() + ", e-progress=" + end.getProgress() + "]");
			return builder.toString();
		}
		
	}
	
	private LAModule module;

	public static final class VehicleLeg {
		
		private Vec2d start_pos;
		private Vec2d end_pos;
		
		private List<LinkNotification> notification_list;
		
		public VehicleLeg() {
			this.notification_list = new ArrayList<>();
		}
		
		public void setStart(Vec2d start_pos) {
			this.start_pos = start_pos;
		}
		
		public void setEnd(Vec2d end_pos) {
			this.end_pos = end_pos;
		}
		
		public void addNotification(LinkNotification notification) {
			notification_list.add(notification);
		}
	}
	
	public final Function<Double, Double> scoring_validator;
	

	@Inject
	private EventsManager events;
	
	//TODO
	@Inject
	private Scenario scenario;
	
	private StringBuilder builder;
	
	private Map<Id<Vehicle>, Id<Person>> driver_map;
	private Map<Id<Person>, VehicleLeg> leg_map;
	private Map<Id<Person>, String> mode_map;

	private final String TEST_PATH = "C:\\Users\\Sebastian\\Desktop\\matsim-out\\";
	private BufferedWriter writer = null;

	@Inject
	public LAHandler(Scenario scenario){
		this.module = new LAModule("lorem ipsum");
		this.scoring_validator = (e) -> {return -1d;};
		this.scenario = scenario;
		this.leg_map = new HashMap<>();
		this.driver_map = new HashMap<>();
		this.mode_map = new HashMap<>();
	}
	
	public Network getNetwork() {
		return this.scenario.getNetwork();
	}

	public boolean linkInside(Link link){
		return module.contains(link.getFromNode().getCoord()) || module.contains(link.getToNode().getCoord());
	}
	
	@Override
	public void reset(int iteration) {
		this.builder = new StringBuilder();
		this.mode_map.clear();
		this.driver_map.clear();
		this.leg_map.clear();
		try {
			this.writer = new BufferedWriter(new FileWriter(TEST_PATH + iteration + ".txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double getPenaltyTime(Id<Person> person_id) {
		VehicleLeg current_leg = getCurrentLegByPerson(person_id);
		if(current_leg == null) return 0;
		
		double score = 0;
		
		
		Polygon start_poly = module.getContainingPolygon(current_leg.start_pos);
		Polygon end_poly = module.getContainingPolygon(current_leg.end_pos);
		
		List<LinkValidation> validation_list = new ArrayList<>(current_leg.notification_list.size() / 2);
		
		//Fasst die einzelnen Events bezüglich Betreten und Verlassen von Links in zeitlich, räumliche Abschnitte zusammen.

		for(int i = 0; i < current_leg.notification_list.size(); i+=2) {
			validation_list.add(new LinkValidation(current_leg.notification_list.get(i), current_leg.notification_list.get(i + 1)));
		}
		
		StringBuilder builder = new StringBuilder();
		
		for(LinkValidation validation : validation_list) {
			Map<Polygon, Double> poly_scoring = validation.getPolygonScoring(module);
			for(Entry<Polygon, Double> entry : poly_scoring.entrySet()) {
				if(entry.getKey() == start_poly || entry.getKey() == end_poly) continue;
				score += entry.getValue();
			}
			builder.append(validation);
		}

		return score;
	}
//
//	@Override
//	public double getScore() {
//		//-Hoher Wert t=const
//		//-Exponentialfunktion t^2
//		//-Logarithmus ln(t)
//		//-Lineare Funktion a*t+b
//		//-Aufsteigende Sinusfunktion sin(x)+x
//		return scoring_validator.apply(getPenaltyTime());
//	}

	public boolean isProhibitedModeByPerson(Id<Person> person_id) {
		return mode_map.get(person_id) != null && mode_map.get(person_id).equals("car");
	}
	
	public boolean isProhibitedModeByVehicle(Id<Vehicle> vehicle_id) {
		return isProhibitedModeByPerson(driver_map.get(vehicle_id));
	}

	public VehicleLeg getCurrentLegByPerson(Id<Person> person_id) {
		return leg_map.get(person_id);
	}
	
	public VehicleLeg getCurrentLegByVehicle(Id<Vehicle> vehicle_id) {
		return leg_map.get(driver_map.get(vehicle_id));
	}
	
	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if(!isProhibitedModeByVehicle(event.getVehicleId())) return;
		Link current_link = getNetwork().getLinks().get(event.getLinkId());
		getCurrentLegByVehicle(event.getVehicleId()).addNotification(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), 1));
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if(!isProhibitedModeByVehicle(event.getVehicleId())) return;
		Link current_link = getNetwork().getLinks().get(event.getLinkId());
		getCurrentLegByVehicle(event.getVehicleId()).addNotification(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), 0));
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		if(isProhibitedModeByPerson(event.getPersonId())) {
			Link current_link = getNetwork().getLinks().get(event.getLinkId());
			getCurrentLegByPerson(event.getPersonId()).addNotification(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), event.getRelativePositionOnLink()));
		}
		driver_map.remove(event.getVehicleId());
		mode_map.put(event.getPersonId(), event.getNetworkMode());	
		//Auswertung

	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		mode_map.put(event.getPersonId(), event.getNetworkMode());
		if(!isProhibitedModeByPerson(event.getPersonId())) return;
		if(!leg_map.containsKey(event.getPersonId())) {
			leg_map.put(event.getPersonId(), new VehicleLeg());
		}
		Link current_link = getNetwork().getLinks().get(event.getLinkId());
		driver_map.put(event.getVehicleId(), event.getPersonId());
		getCurrentLegByPerson(event.getPersonId()).addNotification(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), event.getRelativePositionOnLink()));
		
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		getCurrentLegByPerson(event.getPersonId()).setEnd(new Vec2d(event.getCoord().getX(), event.getCoord().getY()));
		//Validate score
//		System.err.println(getCurrentLegByPerson(event.getPersonId()).notification_list);
		events.processEvent(new PersonScoreEvent(event.getTime(), event.getPersonId(), -getPenaltyTime(event.getPersonId()), "local-area cost"));
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if(getCurrentLegByPerson(event.getPersonId()) == null) {
			leg_map.put(event.getPersonId(), new VehicleLeg());
		}
		getCurrentLegByPerson(event.getPersonId()).setStart(new Vec2d(event.getCoord().getX(), event.getCoord().getY()));
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
//		mode_map.put(event.getPersonId(), "leg");
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
//		mode_map.put(event.getPersonId(), "car");
	}
}
