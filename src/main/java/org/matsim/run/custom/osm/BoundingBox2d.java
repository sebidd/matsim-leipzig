package org.matsim.run.custom.osm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BoundingBox2d {

	public final Vec2d min, max;

	public BoundingBox2d(Vec2d a, Vec2d b) {
		this.min = new Vec2d(Math.min(a.x, b.x), Math.min(a.y, b.y));
		this.max = new Vec2d(Math.max(a.x, b.x), Math.max(a.y, b.y));
	}

	public BoundingBox2d(double x0, double y0, double x1, double y1) {
		this(new Vec2d(x0, y0), new Vec2d(x1, y1));
	}

	public static Vec2d transformPoint(BoundingBox2d from, BoundingBox2d to, Vec2d point) {
		return to.getPointFromProgress(from.getProgressFromPoint(point));
	}

	public Vec2d transformPointTo(BoundingBox2d to, Vec2d point) {
		return BoundingBox2d.transformPoint(this, to, point);
	}

	public Vec2d transformPointFrom(BoundingBox2d from, Vec2d point) {
		return BoundingBox2d.transformPoint(from, this, point);
	}

	public BoundingBox2d scale(double factor) {
		Vec2d center = getCenter();
		return BoundingBox2d.fromCenterAndDims(center.x, center.y, factor * getWidth(), factor * getHeight());
	}

	public static BoundingBox2d fromVecList(List<Vec2d> vec_list) {
		return fromVecit(vec_list.iterator());
	}

	public static BoundingBox2d fromBoundsList(List<BoundingBox2d> bounds_list) {
		return fromBoundsit(bounds_list.iterator());
	}

	public static BoundingBox2d fromBoundsSet(Set<BoundingBox2d> bounds_set) {
		return fromBoundsit(bounds_set.iterator());
	}

	public static BoundingBox2d fromBoundsit(Iterator<BoundingBox2d> it) {
		Set<Vec2d> vec_set = new HashSet<>();
		while(it.hasNext()) {
			BoundingBox2d next = it.next();
			vec_set.addAll(List.of(next.min, next.max));
		}
		return fromVecSet(vec_set);
	}

	public static enum Type {
		Outside, Inside;
	}

	//IS WRONG
	public BoundingBox2d fitRatio(double ratio, Type type) {
		double current_ratio = getRatio();
		if(type == Type.Outside) {
//			if(ratio > current_ratio) {
//				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getWidth() * ratio / current_ratio, getHeight()));
//			} else {
//				//TODO Muss hier nicht auch die current ratio rein? Wurde temporär eingefügt
//				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getWidth(), getHeight() / ratio * current_ratio));
//			}
			if(ratio < current_ratio) {
				//w const
				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getWidth(), getWidth() / ratio));
			} else {
				//h const
				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getHeight() * ratio, getHeight()));
			}
		} else {
			if(ratio < current_ratio) {
				//h const
				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getHeight() * ratio, getHeight()));
			} else {
				return BoundingBox2d.fromCenterAndDims(getCenter(), new Vec2d(getWidth(), getWidth() / ratio));
			}
		}
	}

	public static BoundingBox2d fromVecSet(Set<Vec2d> vec_set) {
		return fromVecit(vec_set.iterator());
	}

	private static BoundingBox2d fromVecit(Iterator<Vec2d> it) {
		Vec2d next = it.next();
		double xmin = next.x, xmax = next.x, ymin = next.y, ymax = next.y;
		while(it.hasNext()) {
			next = it.next();
			if(next.x < xmin) xmin = next.x;
			else if(next.x > xmax) xmax = next.x;
			if(next.y < ymin) ymin = next.y;
			else if(next.y > ymax) ymax = next.y;
		}
		return new BoundingBox2d(xmin, ymin, xmax, ymax);
	}

	public static BoundingBox2d fromBounds(Vec2d a, Vec2d b) {
		return new BoundingBox2d(a, b);
	}

	public static BoundingBox2d fromBounds(double x0, double y0, double x1, double y1) {
		return new BoundingBox2d(x0, y0, x1, y1);
	}

	public static BoundingBox2d fromCenterAndDims(Vec2d center, Vec2d dims) {
		return new BoundingBox2d(center.x - dims.x / 2, center.y - dims.y / 2, center.x + dims.x / 2, center.y + dims.y / 2);
	}

	public static BoundingBox2d fromCenterAndDims(double cx, double cy, double width, double height) {
		return fromCenterAndDims(new Vec2d(cx, cy), new Vec2d(width, height));
	}

	public boolean intersects(BoundingBox2d other) {
		return (min.x < other.max.x && max.x > other.min.x && min.y < other.max.y && max.y > other.min.y);
	}

	public double getRatio() {
		return getWidth() / getHeight();
	}

	public Vec2d getMin() {
		return min;
	}

	public Vec2d getMax() {
		return max;
	}

	public double getWidth() {
		return getMax().x - getMin().x;
	}

	public double getHeight() {
		return getMax().y - getMin().y;
	}

	public Vec2d getSize() {
		return min.deltaTo(max);
	}

//	public Vec2d getProgressFromPoint(Vec2d point) {
//		return new Vec2d((point.x - max.x) / getWidth(), (point.y - max.y) / getHeight());
//	}

	public boolean contains(BoundingBox2d other) {
		return !(other.min.x < min.x || other.min.y < min.y || other.max.x > max.x || other.max.y > max.y);
	}

	public boolean isContained(BoundingBox2d other) {
		return other.contains(this);
	}

	public boolean contains(Vec2d point) {
		return point.x >= min.x && point.y >= min.y && point.x <= max.x && point.y <= max.y;
	}

	public Vec2d getProgressFromPoint(Vec2d point) {
		return new Vec2d((point.x - min.x) / getWidth(), (point.y - min.y) / getHeight());
	}

	public Vec2d getPointFromProgress(Vec2d progress) {
		return new Vec2d(min.x + progress.x * getWidth(), min.y + progress.y * getHeight());
	}

	public Vec2d getPointFromProgress(double px, double py) {
		return getPointFromProgress(new Vec2d(px, py));
	}

	public BoundingBox2d addPadding(double padding) {
		return new BoundingBox2d(min.add(new Vec2d(padding, padding)), max.sub(new Vec2d(padding, padding)));
	}

	public BoundingBox2d addPadding(double px, double py) {
		return new BoundingBox2d(min.add(new Vec2d(px, py).abs()), max.sub(new Vec2d(px, py).abs()));
	}

	public BoundingBox2d addMargin(double margin) {
		return new BoundingBox2d(min.sub(new Vec2d(margin, margin)), max.add(new Vec2d(margin, margin)));
	}

	public BoundingBox2d addPadding(Vec2d padding) {
		return addPadding(padding.x, padding.y);
	}

	public BoundingBox2d addMargin(Vec2d margin) {
		return addMargin(margin.x, margin.y);
	}

	public BoundingBox2d addMargin(double mx, double my) {
		return new BoundingBox2d(min.sub(new Vec2d(mx, my)), max.add(new Vec2d(mx, my)));
	}

	@Override
	public String toString() {
		return "BoundingBox2d min: [" + min + "], max: [" + max + "]";
	}

	public Vec2d getCenter() {
		return new Vec2d(min.x + getWidth() / 2f, min.y + getHeight() / 2f);
	}

}
