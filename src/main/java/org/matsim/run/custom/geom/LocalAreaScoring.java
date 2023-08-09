package org.matsim.run.custom.geom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.contrib.analysis.vsp.traveltimedistance.NetworkRouteValidator;
import org.matsim.core.population.routes.LinkNetworkRouteFactory;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.scoring.SumScoringFunction.TripScoring;
import org.matsim.run.custom.LocalAreaEngine;
import org.matsim.run.custom.LocalAreaMap;

public class LocalAreaScoring implements TripScoring {
	
	private LocalAreaMap map;
	
	private double time = 0;
	
	private Geometry startGeom = null;
	private Geometry endGeom = null;
	private Set<Geometry> compGeom = null;
	
	public LocalAreaScoring(LocalAreaMap map) {
		this.map = Objects.requireNonNull(map);
	}
	
	@Override
	public void handleTrip(final Trip trip) {
		this.startGeom = LocalAreaUtils.getContainingGeometry(trip.getOriginActivity().getCoord(), map.getGeometries());
		this.endGeom = LocalAreaUtils.getContainingGeometry(trip.getDestinationActivity().getCoord(), map.getGeometries());
		this.compGeom = new HashSet<>(map.getGeometries());
		compGeom.remove(startGeom);
		compGeom.remove(endGeom);
		
		for(Leg leg : trip.getLegsOnly()) {
			if(LocalAreaEngine.getProhibitedModes().contains(leg.getMode())) {
				final List<Link> linkList = null;
				
				
				for(Link link : linkList) {
					validateLink(link);
				}
			}
		}
		
	}
	
	private void validateLink(Link link) {
		Coord start = link.getFromNode().getCoord();
		Coord end = link.getToNode().getCoord();
		Coord mid = new Coord(start.getX() + (0.5 * (end.getX() - start.getX())), start.getY() + (0.5 * (end.getY() - start.getY())));
		Geometry containing = LocalAreaUtils.getContainingGeometry(mid, compGeom);
		if(containing != null) {
		
		}
	}

	@Override
	public void finish() {
		
	}

	@Override
	public double getScore() {
		return time; //TODO Manipulate time with function
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
