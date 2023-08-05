package org.matsim.run.custom.geom;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;

public class PersonCommute {

	public ChronologicList.SpacetimeList activityList;
	
	public PersonCommute() {
		this.activityList = new ArrayList<>();
	}
	
	public void addSpacetime(Spacetime spacetime) {
		this.spacetimeList.add(spacetime);
	}
	
	public List<Spacetime> getSpacetimeList() {
		return this.spacetimeList;
	}
	
	public Coord getEnd() {
		return this.end;
	}
	
	public Coord getStart() {
		return this.start;
	}
	
	public void setStart(Coord start) {
		this.start = start;
	}
	
	public void setEnd(Coord end) {
		this.end = end;
	}
	
}
