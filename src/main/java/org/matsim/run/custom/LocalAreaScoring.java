package org.matsim.run.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.inject.Inject;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.scoring.SumScoringFunction;

public class LocalAreaScoring implements SumScoringFunction.TripScoring {

	private double time;
	


	@Inject
	private Network network;

	/**
	 * Creates a new local area scoring instance from a MATSim network to process.
	 * @param network The network on which agents travel.
	 */
	public LocalAreaScoring(Network network) {
		this.time = 0;
		this.network = network;
	}

	@Override
	/**
	 * Handles a trip performed by an MATSim agent.
	 */
	public void handleTrip(final Trip trip) {
		//Validates the starting geometry of an agents trip, if there is one. Else the startGeom local variable remains null.
		Geometry startGeom = null;
		if(trip.getOriginActivity() != null && trip.getOriginActivity().getCoord() != null){
			startGeom = LocalAreaUtils.getContainingGeometry(trip.getOriginActivity().getCoord(), LocalAreaModule.get().getCollSet());
		}
		//Validates the end geometry of an agents trip, if there is one. Else the endGeom local variable remains null.
		Geometry endGeom = null;
		if(trip.getDestinationActivity() != null && trip.getDestinationActivity().getCoord() != null) {
			endGeom = LocalAreaUtils.getContainingGeometry(trip.getDestinationActivity().getCoord(), LocalAreaModule.get().getCollSet());
		}

		//Checks each leg within a trip for the restricted modes defined within the LocalAreaModule class. If a leg with restricted mode is found, further validation of passed polygons is started.
		for(Leg leg : trip.getLegsOnly()) {
			if(LocalAreaModule.get().getProhibitedModes().contains(leg.getMode())) {
				final String[] linkStrs = leg.getRoute().getRouteDescription().split(" ");

				for(String linkStr : linkStrs){
					Id<Link> linkID = Id.createLinkId(linkStr);
					Link link = network.getLinks().get(linkID);

					Geometry containing = LocalAreaModule.get().getCollMap().get(link.getId());

					if(containing != startGeom && containing != endGeom) {
						time += (link.getLength() / link.getFreespeed());
					}

				}

			}
		}

	}

	@Override
	public void finish() {

	}

	@Override
	/**
	 * Handles the local area scoring of a MATSim agents trip by applying a transformation function implemented in the LocalAreaModule class singleton to the accumulated time spent in local areas by the current agent.
	 */
	public double getScore() {
		double penalty = -LocalAreaModule.get().getScoringFunction().apply(time);
		LocalAreaModule.PENALTY_SUM += penalty;
		return penalty; //TODO Manipulate time with function
	}


//	private Map<Id<Person>, ChronologicList.SpacetimeList> activityMap;
//	private Map<Id<Vehicle>, Id<Person>> driverMap;
//
//	private Controler controler;
//
//	public LocalAreaScoring(Controler controler) {
//		this.activityMap = new HashMap<>();
//		this.controler = controler;
//	}
//
//	@Override
//	public void handleEvent(ActivityStartEvent event) {
//		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
//		if(!activityMap.containsKey(event.getPersonId())) {
//			activityMap.put(event.getPersonId(), new SpacetimeList());
//		}
//		activityMap.get(event.getPersonId()).add(event.getCoord(), event.getTime());
//	}
//
//	public abstract boolean isPersonValid(Person person);
//	public abstract boolean isModeValid(String mode);
//
//	private Person getPersonByID(Id<Person> person_id) {
//		return controler.getScenario().getPopulation().getPersons().get(person_id);
//	}
//
//	@Override
//	public void handleEvent(PersonEntersVehicleEvent event) {
////		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
//		driverMap.put(event.getVehicleId(), event.getPersonId());
//	}
//
//	@Override
//	public void handleEvent(PersonLeavesVehicleEvent event) {
//		driverMap.remove(event.getVehicleId());
//	}
//
//	@Override
//	public void handleEvent(ActivityEndEvent event) {
//		if(!isPersonValid(getPersonByID(event.getPersonId()))) return;
//		if(!activityMap.containsKey(event.getPersonId())) {
//			activityMap.put(event.getPersonId(), new SpacetimeList());
//		}
//		activityMap.get(event.getPersonId()).add(event.getCoord(), event.getTime());
//	}

}
