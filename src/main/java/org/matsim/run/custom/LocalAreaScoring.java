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

	public LocalAreaScoring(Network network) {
		this.time = 0;
		this.network = network;
	}

	@Override
	public void handleTrip(final Trip trip) {
		Geometry startGeom = null;
		if(trip.getOriginActivity() != null && trip.getOriginActivity().getCoord() != null){
			startGeom = LocalAreaUtils.getContainingGeometry(trip.getOriginActivity().getCoord(), LocalAreaModule.get().getCollSet());
		}
		Geometry endGeom = null;
		if(trip.getDestinationActivity() != null && trip.getDestinationActivity().getCoord() != null) {
			endGeom = LocalAreaUtils.getContainingGeometry(trip.getDestinationActivity().getCoord(), LocalAreaModule.get().getCollSet());
		}

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
