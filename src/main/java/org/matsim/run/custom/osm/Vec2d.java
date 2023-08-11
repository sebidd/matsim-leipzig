package org.matsim.run.custom.osm;

public class Vec2d {

	public static final double THETA = 0.0001d;
	public static final double PI = Math.PI;

	public final double x, y;

	public Vec2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2d(float x, float y) {
		this((double) x, (double) y);
	}

	public Vec2d(int x, int y) {
		this((double) x, (double) y);
	}

	public Vec2d scaleBy(double factor) {
		return new Vec2d(x * factor, y * factor);
	}

	public static Vec2d fromPolar(double radius, double theta) {
		return fromTheta(theta).scaleTo(radius);
	}

	public static Vec2d fromTheta(double theta) {
		return new Vec2d(Math.cos(theta - PI), Math.sin(theta - PI));
	}

	public Vec2d abs() {
		return new Vec2d(Math.abs(x), Math.abs(y));
	}

	public double area() {
		return Math.abs(x * y);
	}

	public double range() {
		return y - x;
	}

	public double interpolate(double v) {
		return (v - x) / (y - x);
	}

	public Vec2d rotate(double theta) {
		return Vec2d.fromPolar(getLength(), getTheta() + theta);
	}

	/**
	 * See {@link #Vec2d(Vec2d)}.
	 * @return
	 */
	public Vec2d copy() {
		return new Vec2d(x, y);
	}

	/**
	 * Creates a copy of the given vector.
	 * @param other The vector to be copied.
	 */
	public Vec2d(Vec2d other) {
		this.x = other.x;
		this.y = other.y;
	}

	/**
	 * Returns the euclidian distance between the two vectors a and b.
	 * @param a The first vector.
	 * @param b The second vector.
	 * @return The euclidian distance.
	 */
	public static double dist(Vec2d a, Vec2d b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		return (double) Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof Vec2d) {
			Vec2d v_other = (Vec2d) other;
			return Double.compare(x, v_other.x) == 0 && Double.compare(y, v_other.y) == 0;
		}
		return false;
	}

	public boolean matches(Vec2d other) {
		return dist(this, other) < THETA;
	}


	/**
	 * Returns a unified version of the vector, that is a vector with uniform length of 1.
	 * @return A unified vector with length 1.
	 */
	public Vec2d unify() {
		return normalize();
	}

	/**
	 * Checks if two vectors a and b match, that is their distance is smaller than the constant THETA.
	 * @param a The first vector.
	 * @param b The second vector.
	 * @return If the two vectors match.
	 */
	public static boolean matches(Vec2d a, Vec2d b) {
		return a.matches(b);
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash = (int) ((hash * 31 + Double.doubleToLongBits(x)) * 31 + Double.doubleToLongBits(y));
		return hash;
	}

	/**
	 * Returns the length of the vector.
	 * @return The length.
	 */
	public double getLength() {
		return (double) Math.sqrt(x * x + y * y);
	}

	/**
	 * Returns the inverse vector.
	 * @return The inverse as vector.
	 */
	public Vec2d getInverse() {
		return new Vec2d(-x, -y);
	}

	/**
	 * Returns the inverse vector.
	 * @return The inverse as vector.
	 */
	public Vec2d getFlip() {
		return getInverse();
	}

	/**
	 * Returns the left orthogonal vector.
	 * @return The left orthogonal as vector.
	 */
	public Vec2d getOrthoLeft() {
		return new Vec2d(-y, x);
	}

	/**
	 * Returns the right orthogonal vector.
	 * @return The right orthogonal as vector.
	 */
	public Vec2d getOrthoRight() {
		return new Vec2d(y, -x);
	}

	public double dot(Vec2d other) {
		return x * other.x + y * other.y;
	}

	public static double dot(Vec2d a, Vec2d b) {
		return a.dot(b);
	}

	public double getDotNormalized(Vec2d other) {
		return this.normalize().dot(other.normalize());
	}

	public static double dotn(Vec2d a, Vec2d b) {
		return a.getDotNormalized(b);
	}

	public static double det(Vec2d a, Vec2d b) {
		return a.det(b);
	}

	public double det(Vec2d other) {
		return x * other.y - y * other.x;
	}

	/**
	 * Returns the direction of the vector as a normalized version.
	 * @return The normalized direction.
	 */
	public Vec2d getDirectionNormalized() {
		return normalize();
	}

	/**
	 * Returns the direction of a vector as an angle between 0 and 2π.
	 * @return The angular direction of the vector.
	 */
	public double getTheta() {
		//OLD return (double) Math.atan2(y, x);
		return (double) Math.atan2(y, x) + PI;
	}

	public Vec2d sub(Vec2d other) {
		return new Vec2d(x - other.x, y - other.y);
	}

	public Vec2d add(Vec2d other) {
		return new Vec2d(x + other.x, y + other.y);
	}

	public static Vec2d add(Vec2d a, Vec2d b) {
		return a.add(b);
	}

	public Vec2d add(double x, double y) {
		return new Vec2d(this.x + x, this.y + y);
	}

	public static Vec2d add(Vec2d... vecs) {
		Vec2d start = vecs[0];
		for(int i = 1; i < vecs.length; i++) {
			start = start.add(vecs[i]);
		}
		return start;
	}

	public Vec2d add(double scalar) {
		return new Vec2d(x + scalar, y + scalar);
	}

	public Vec2d sub(double scalar) {
		return new Vec2d(x - scalar, y - scalar);
	}

	public static Vec2d add(Vec2d v, double s) {
		return v.add(s);
	}

	public static Vec2d add(double s, Vec2d v) {
		return v.add(s);
	}

	public static Vec2d sub(Vec2d v, double s) {
		return v.sub(s);
	}

	public static Vec2d sub(double s, Vec2d v) {
		return v.sub(s);
	}


	public static Vec2d sub(Vec2d a, Vec2d b) {
		return a.sub(b);
	}

	public Vec2d deltaTo(Vec2d to) {
		return new Vec2d(to.x - x, to.y - y);
	}

	public Vec2d deltaFrom(Vec2d from) {
		return new Vec2d(x - from.x, y - from.y);
	}

	public static Vec2d delta(Vec2d from, Vec2d to) {
		return from.deltaTo(to);
	}

	public Vec2d normalize() {
		double length = getLength();
		if(length == 0) return new Vec2d(x, y);
		return new Vec2d(x / length, y / length);
	}

	public double clamp(double value) {
		return (value - x) / (y - x);
	}

	public Vec2d scaleTo(double length) {
		double fac = length/getLength();
		return new Vec2d(fac * x, fac * y);
	}

	public Vec2d mul(double scalar) {
		return new Vec2d(x * scalar, y * scalar);
	}

	public Vec2d mul(double _x, double _y) {
		return new Vec2d(x * _x, y * _y);
	}

	public Vec2d mul(Vec2d other) {
		return new Vec2d(x * other.x, y * other.y);
	}

	public static Vec2d mul(double scalar, Vec2d vec) {
		return vec.mul(scalar);
	}

	public static Vec2d mul(Vec2d vec, double scalar) {
		return mul(scalar, vec);
	}

	public static Vec2d mul(Vec2d a, Vec2d b) {
		return a.mul(b);
	}

	public double avg() {
		return (x + y) / 2;
	}

	public double max() {
		return Math.max(x, y);
	}

	public double min() {
		return Math.min(x, y);
	}

	public double delta() {
		return Math.abs(x - y);
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}

}
