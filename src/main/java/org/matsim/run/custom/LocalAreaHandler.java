package org.matsim.run.custom;

import org.locationtech.jts.geom.Geometry;
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
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

public class LocalAreaHandler implements IterationEndsListener, IterationStartsListener, ActivityEndEventHandler, ActivityStartEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler, LinkLeaveEventHandler {

	public final Function<Double, Double> scoring_validator;


	@Inject
	private EventsManager events;

	//TODO

	private StringBuilder builder;

	private Map<Id<Vehicle>, Id<Person>> driver_map;
	private Map<Id<Person>, List<VehicleLeg>> leg_map;
	private Map<Id<Person>, String> mode_map;

	private final String TEST_PATH = "C:\\Users\\Sebastian\\Desktop\\matsim-out\\";

	private LocalAreaModule module;

	private Controler controler;

	private static Set<String> PROHIBITED_MODE_SET = Set.of("car");


	@Inject
	public LocalAreaHandler(Controler controler, LocalAreaModule module){
		this.module = module;
	//	LocalAreaUtils.setOSM("res/Leipzig.osm");
		this.scoring_validator = (e) -> {return -1d;};
		this.controler = controler;
		this.leg_map = new HashMap<>();
		this.driver_map = new HashMap<>();
		this.mode_map = new HashMap<>();
	}

	public Network getNetwork() {
		return this.controler.getScenario().getNetwork();
	}

	public Scenario getScenario() {
		return this.controler.getScenario();
	}

	public boolean linkInside(Link link){
//		return module.contains(link.getFromNode().getCoord()) || module.contains(link.getToNode().getCoord());
		return false;
	}

	@Override
	public void reset(int iteration) {
		this.mode_map.clear();
		this.driver_map.clear();
		this.leg_map.clear();
	}

	public Person getPersonByID(Id<Person> person_id) {
		return getScenario().getPopulation().getPersons().get(person_id);
	}

	private double getPenaltyTime(Id<Person> person_id) {
		VehicleLeg current_leg = getCurrentLegByPerson(person_id);
		if(current_leg == null) return 0;

		double score = 0;

		/*
		Map<Geometry, Double> time_map = validateTime(current_leg);

	//	Geometry startGeom = LocalAreaUtils.getContainingGeometry(current_leg.getStart());
	//	Geometry endGeom = LocalAreaUtils.getContainingGeometry(current_leg.getEnd());

		List<LinkValidation> validation_list = new ArrayList<>(current_leg.notification_list.size() / 2);

		//Fasst die einzelnen Events bezüglich Betreten und Verlassen von Links in zeitlich, räumliche Abschnitte zusammen.

		for(int i = 0; i < current_leg.notification_list.size(); i+=2) {
			validation_list.add(new LinkValidation(current_leg.notification_list.get(i), current_leg.notification_list.get(i + 1)));
		}

		for(LinkValidation validation : validation_list) {
			Map<Geometry, Double> poly_scoring = validation.getPolygonScoring(module);
			for(Entry<Geometry, Double> entry : poly_scoring.entrySet()) {
				if(entry.getKey() == startGeom || entry.getKey() == endGeom) continue;
				score += entry.getValue();
			}
			builder.append(person_id + " " + validation + " " + LocalAreaUtils.getContainedLength(endGeom, null) + System.lineSeparator());
		}
		 */

		return score;
	}

	private Map<Geometry, Double> validateTime(VehicleLeg leg){
		return null;
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
		return mode_map.get(person_id) != null && PROHIBITED_MODE_SET.contains(mode_map.get(person_id));
	}

	public boolean isProhibitedModeByVehicle(Id<Vehicle> vehicle_id) {
		return isProhibitedModeByPerson(driver_map.get(vehicle_id));
	}

	public VehicleLeg getCurrentLegByPerson(Id<Person> person_id) {
		List<VehicleLeg> leg_list = leg_map.get(person_id);
		if(leg_list != null) return leg_list.get(leg_list.size() - 1);
		else return null;
	}

	public VehicleLeg getCurrentLegByVehicle(Id<Vehicle> vehicle_id) {
		return getCurrentLegByPerson(driver_map.get(vehicle_id));
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if(getCurrentLegByPerson(event.getPersonId()) == null) {
			leg_map.put(event.getPersonId(), new ArrayList<>());
		}

		VehicleLeg current = new VehicleLeg(event.getPersonId());
		current.setStart(event.getCoord());
		leg_map.get(event.getPersonId()).add(current);
//		getCurrentLegByPerson(event.getPersonId()).setStart(event.getCoord());
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		mode_map.put(event.getPersonId(), event.getNetworkMode());
		driver_map.put(event.getVehicleId(), event.getPersonId());

		if(!isProhibitedModeByPerson(event.getPersonId())) return;

		Link current_link = getNetwork().getLinks().get(event.getLinkId());

		VehicleLeg current = getCurrentLegByPerson(event.getPersonId());
		if(current != null) current.addNotification(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), event.getRelativePositionOnLink()));
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if(!isProhibitedModeByVehicle(event.getVehicleId())) return;

		Link current_link = getNetwork().getLinks().get(event.getLinkId());


		VehicleLeg current = getCurrentLegByVehicle(event.getVehicleId());
		if(current != null) current.addNotification(new LinkNotification(current_link, LinkNotification.Type.Enter, event.getTime(), 0));
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if(!isProhibitedModeByVehicle(event.getVehicleId())) return;

		Link current_link = getNetwork().getLinks().get(event.getLinkId());

		VehicleLeg current = getCurrentLegByVehicle(event.getVehicleId());
		if(current != null) current.addNotification(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), 1));
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		if(isProhibitedModeByPerson(event.getPersonId())) {
			Link current_link = getNetwork().getLinks().get(event.getLinkId());

			VehicleLeg current = getCurrentLegByPerson(event.getPersonId());
			if(current != null) current.addNotification(new LinkNotification(current_link, LinkNotification.Type.Leave, event.getTime(), event.getRelativePositionOnLink()));
		}

		driver_map.remove(event.getVehicleId());
		mode_map.put(event.getPersonId(), event.getNetworkMode());

	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		VehicleLeg current = getCurrentLegByPerson(event.getPersonId());
		if(current != null) current.setEnd(event.getCoord());

//		events.processEvent(new PersonScoreEvent(event.getTime(), event.getPersonId(), -getPenaltyTime(event.getPersonId()), "local-area cost"));

	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
//		mode_map.put(event.getPersonId(), "leg");
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
//		mode_map.put(event.getPersonId(), "car");
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		debugIteration(event);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(TEST_PATH + event.getIteration() + ".txt"));
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void debugIteration(IterationEndsEvent event) {
		StringBuilder b = new StringBuilder();

		for(Entry<Id<Person>, List<VehicleLeg>> entry : leg_map.entrySet()) {
			Person p = getPersonByID(entry.getKey());
			b.append(p + " " + p.getSelectedPlan() + " " + p.getAttributes() + ":" + System.lineSeparator());
			b.append(p.getSelectedPlan().getPlanElements());
//			for(VehicleLeg leg : entry.getValue()) {
//				b.append(leg);
//			}
			b.append(System.lineSeparator());
		}

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(TEST_PATH + "persona-" + event.getIteration() + ".txt")));
			bw.write(b.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		this.builder = new StringBuilder();

	}
}
