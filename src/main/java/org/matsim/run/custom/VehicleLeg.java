package org.matsim.run.custom;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;


public final class VehicleLeg {

	private Coord start;
	private Coord end;

	private final Id<Person> person_id;

	List<LinkNotification> notification_list;

	public VehicleLeg(Id<Person> person_id) {
		this.person_id = person_id;
		this.notification_list = new ArrayList<>();
	}

	public Id<Person> getPersonID() {
		return this.person_id;
	}

	public void setStart(Coord start) {
		this.start = start;
	}

	public Coord getEnd() {
		return this.end;
	}

	public Coord getStart() {
		return this.start;
	}

	public void setEnd(Coord end) {
		this.end = end;
	}

	public void addNotification(LinkNotification notification) {
		notification_list.add(notification);
	}

	@Override
	public String toString() {
		return String.format("[VehicleLeg from: \'%s\', to: \'%s\', via: \'%s\']", start, end, notification_list);
	}
}
