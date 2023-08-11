package org.matsim.run.custom.osm;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;

public class ChronologicList<S, T extends Comparable<T>> {

	public static class SpacetimeList extends ChronologicList<Coord, Double> {}

	public class Bundle {

		private S s;
		private T t;

		public Bundle(S s, T t) {
			this.s = s;
			this.t = t;
		}

		public T getTime() {
			return this.t;
		}

		public S getValue() {
			return this.s;
		}

		public void setTime(T t) {
			this.t = t;
			sort();
		}

		public void setValue(S s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return String.format("[t:\'%s\', value:\'%s\'", t, s);
		}

	}

	private List<Bundle> list;

	public ChronologicList() {
		this.list = new ArrayList<>();
	}

	private void sort() {
		list.sort((e, f) -> {
			return e.getTime().compareTo(f.getTime());
		});
		System.out.println(list);
	}

	public void add(S s, T t) {
		list.add(this.new Bundle(s, t));
		sort();
	}


	public S getNextBefore(T time) {
		for(int i = list.size() - 1; i >= 0; i--) {
			if(time.compareTo(list.get(i).getTime()) > -1) return list.get(i).getValue();
		}
		return null;
	}

	public S getNextAfter(T time) {
		for(int i = 0; i < list.size(); i++) {
			if(time.compareTo(list.get(i).getTime()) < 1) return list.get(i).getValue();
		}
		return null;
	}

	public static void main(String[] args) {
		SpacetimeList list = new SpacetimeList();
		list.add(new Coord(10, 10), 40d);
		list.add(new Coord(100, 1), 60d);
		list.add(new Coord(100, 2), 10d);

		for(double i = 0; i < 100; i++) {
			System.err.println(i + " " + list.getNextBefore(i) + " " + list.getNextAfter(i));
		}
	}



}
