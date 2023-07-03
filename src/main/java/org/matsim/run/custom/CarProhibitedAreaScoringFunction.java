package org.matsim.run.custom;

import cadyts.calibrators.filebased.Agent;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.scoring.SumScoringFunction;

import java.util.Optional;

public class CarProhibitedAreaScoringFunction implements SumScoringFunction.ArbitraryEventScoring {

	private static CarProhibitedAreaModule PROHIBITED_AREA_MODULE;

	public final float a;
	public final float b;

	private double enter_time = 0;
	private double time_sum = 0;

	private Network network;
	private Agent agent;

	private Optional<CarProhibitedAreaModule.Polygon> leg_start_polygon;
	private Optional<CarProhibitedAreaModule.Polygon> leg_end_polygon;

	public CarProhibitedAreaScoringFunction(float a, float b){
		if(PROHIBITED_AREA_MODULE == null) PROHIBITED_AREA_MODULE = new CarProhibitedAreaModule("lorem ipsum");
		this.a = a;
		this.b = b;
	}

	public float getPenalty(float time){
		return a * time + b;
	}

	public boolean linkInside(Link link){
		return PROHIBITED_AREA_MODULE.contains(link.getFromNode().getCoord()) || PROHIBITED_AREA_MODULE.contains(link.getToNode().getCoord());
	}



	@Override
	public void handleEvent(Event event) {

		if(event instanceof LinkEnterEvent) {
			LinkEnterEvent enter_event = (LinkEnterEvent) event;
			//TODO Modus feststellen
			enter_time = enter_event.getTime();
		} else if(event instanceof LinkLeaveEvent){
			LinkLeaveEvent leave_event = (LinkLeaveEvent) event;
			//Modus entspricht jenem des LinkEnterEvents.
			time_sum += (leave_event.getTime() - enter_time);

		} else if(event instanceof VehicleEntersTrafficEvent){
			VehicleEntersTrafficEvent enter_traffic_event = (VehicleEntersTrafficEvent) event;
			Link enter_link = network.getLinks().get(enter_traffic_event.getLinkId());

		} else if(event instanceof VehicleLeavesTrafficEvent){
			VehicleLeavesTrafficEvent leave_traffic_event = (VehicleLeavesTrafficEvent) event;
			Link leave_link = network.getLinks().get(leave_traffic_event.getLinkId());
		} else if(event instanceof ActivityEndEvent){
			//Anfang vom Trip = Ende der Aktivität.
			ActivityEndEvent end_event = (ActivityEndEvent) event;
			this.leg_start_polygon = PROHIBITED_AREA_MODULE.getCoveringPolygon(end_event.getCoord());
		} else if(event instanceof ActivityStartEvent){
			//Ende vom Trip = Anfang von Aktivität
			ActivityStartEvent start_event = (ActivityStartEvent) event;
			this.leg_end_polygon = PROHIBITED_AREA_MODULE.getCoveringPolygon(start_event.getCoord());
		}
	}

	@Override
	public void finish() {

	}

	@Override
	public double getScore() {
		return -a * time_sum - b;
	}
}
