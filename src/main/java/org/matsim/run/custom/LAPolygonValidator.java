package org.matsim.run.custom;

import de.sebidd.rail.base.component.Polygon;

public class LAPolygonValidator {

	public static enum PolygonEventType {
		Enter, Leave;
	}
	
	private final Polygon polygon;
	
	private final double start_time;
	private final double end_time;
	
	public LAPolygonValidator(Polygon polygon, double start_time, double end_time) {
		this.polygon = polygon;
		this.start_time = start_time;
		this.end_time = end_time;
	}
	
	public Polygon getPolygon() {
		return this.polygon;
	}
	
	public double getStartTime() {
		return this.start_time;
	}
	
	public double getEndTime() {
		return this.end_time;
	}
	
	public double getDeltaTime() {
		return getEndTime() - getStartTime();
	}
	
}
