package org.matsim.run.custom.geom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.Controler;
import org.matsim.run.custom.geom.ChronologicList.SpacetimeList;
import org.matsim.vehicles.Vehicle;

public abstract class PersonCommuteHandler implements ActivityEndEventHandler, ActivityStartEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {

	private Map<Id<Person>, ChronologicList.SpacetimeList> activityMap;
	private Map<Id<Vehicle>, Id<Person>> driverMap;
	
	private Controler controler;
	
	public PersonCommuteHandler(Controler controler) {
		this.activityMap = new HashMap<>();
		this.controler = controler;
	}
	
	@Override
	public void handleEvent(ActivityStartEvent event) {
		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
		if(!activityMap.containsKey(event.getPersonId())) {
			activityMap.put(event.getPersonId(), new SpacetimeList());
		}
		activityMap.get(event.getPersonId()).add(event.getCoord(), event.getTime());
	}
	
	public abstract boolean isPersonValid(Person person);
	public abstract boolean isModeValid(String mode);
	
	private Person getPersonByID(Id<Person> person_id) {
		return controler.getScenario().getPopulation().getPersons().get(person_id);
	}
	
	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
//		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
		driverMap.put(event.getVehicleId(), event.getPersonId());
	}
	
	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		driverMap.remove(event.getVehicleId());
	}
	
	@Override
	public void handleEvent(ActivityEndEvent event) {
		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
		if(!activityMap.containsKey(event.getPersonId())) {
			activityMap.put(event.getPersonId(), new SpacetimeList());
		}
		activityMap.get(event.getPersonId()).add(event.getCoord(), event.getTime());
	}

}
