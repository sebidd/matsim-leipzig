package org.matsim.run.custom;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

public final class LinkNotification {
	
	public static enum Type {
		Enter, Leave;
	}
	
	final Link link;
	private final double time;
	private final double progress;
	private final LinkNotification.Type type;
	
	public LinkNotification(Link link, LinkNotification.Type type, double time, double progress) {
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
	
	public LinkNotification.Type getType() {
		return this.type;
	}
	
	public Coord getPosition() {
		final Coord start = link.getFromNode().getCoord();
		final Coord end = link.getToNode().getCoord();
		return new Coord(start.getX() + getProgress() * (end.getX() - start.getX()), start.getY() + getProgress() * (end.getY() - start.getY()));
	}
	
	@Override
	public String toString() {
		return "LinkNotification[LinkID=" + link.getId() + " time=" + time + ", progress=" + progress + ", type=" + type + "]";
	}
	
}