package io.chandler.trapentrix;

import static io.chandler.trapentrix.Trapentrix.Piece.*;

import java.util.Arrays;
import java.util.TreeSet;

public class Trapentrix {
	
	public static Orbit grip1_close_low  = new Orbit(F1D, F1g, B1C);
	public static Orbit grip1_close_high = new Orbit(F1C, B1g, B1D);
	public static Orbit grip1_far_low    = new Orbit(F2D, F1m, B_U);
	public static Orbit grip1_far_high   = new Orbit(F_U, B1m, B2D);
	
	public static Orbit grip2_close_low  = new Orbit(F2D, F2g, B2C);
	public static Orbit grip2_close_high = new Orbit(F2C, B2g, B2D);
	public static Orbit grip2_far_low    = new Orbit(F1D, F2m, B_U);
	public static Orbit grip2_far_high   = new Orbit(F_U, B2m, B1D);
	
	public static Orbit orbitF = new Orbit(F1D, F2D, F1m, F2m, F1g, F2g, B_U, B1C, B2C);
	public static Orbit orbitB = new Orbit(B1D, B2D, B1m, B2m, B1g, B2g, F_U, F1C, F2C);

	public static Move grip1Down = new Move(4)
			.add(grip1_close_low, 1)
			.add(grip1_close_high, 1)
			.add(grip1_far_low, 1)
			.add(grip1_far_high, 1);
	public static Move grip1Up = new Move(4)
			.add(grip1_close_low, -1)
			.add(grip1_close_high, -1)
			.add(grip1_far_low, -1)
			.add(grip1_far_high, -1);
	public static Move grip2Down = new Move(4)
			.add(grip2_close_low, 1)
			.add(grip2_close_high, 1)
			.add(grip2_far_low, 1)
			.add(grip2_far_high, 1);
	public static Move grip2Up = new Move(4)
			.add(grip2_close_low, -1)
			.add(grip2_close_high, -1)
			.add(grip2_far_low, -1)
			.add(grip2_far_high, -1);
	
	public String stateAsCycleListAlt() {
		String src = this.stateAsCycleListAlt();
		for (Piece p : reference) src = src.replaceAll(p.name(), p.altName);
		return src;
	}
	
	public String stateAsCycleList() {
		StringBuilder list = new StringBuilder();
		TreeSet<Piece> unchecked = new TreeSet<Piece>();
		unchecked.addAll(Arrays.asList(reference));
		// Remove unchanged pieces
		//for (Piece p : reference) if (in(p) == p) accountedFor.remove(p);
		while (!unchecked.isEmpty()) {
			Piece original = unchecked.first();
			Piece now = in(original);
			unchecked.remove(original);
			if (!original.equals(now)) {
				list.insert(0,"\n");
				while (!now.equals(original)) {
					list.insert(0,now);
					list.insert(0," -> ");
					unchecked.remove(now);
					now = in(now);
				}
				list.insert(0,original);
				list.insert(0,Trapentrix.orbitF.contains(original) ? "F: " : "B: ");
			}
		}
		return list.toString();
	}
	
	
	public Trapentrix move(Move m) {
		for (int oi = 0; oi < m.entered; oi++) {
			Orbit orbit = m.orbits[oi];
			int dir = m.directions[oi];
			int len = orbit.members.length;
			for (int i = 0; i < len; i++) {
				orbit.buffer[i] = in(orbit.members[i]);
			}
			for (int i = 0; i < len; i++) {
				setIn(orbit.members[i], orbit.buffer[(i + dir + len) % len]);
			}
		}
		return this;
	}
	
	public String altString() {
		String src = this.toString();
		for (Piece p : reference) src = src.replaceAll(p.name(), p.altName);
		return src;
	}
	
	public String colorString() {
		String src = this.toString();
		for (Piece p : reference) src = src.replaceAll(p.name(), " " + p.color + " ");
		return src;
	}
	
	public String changedString() {
		String src = this.toString();
		for (Piece p : reference) src = src.replaceAll(p.name(), in(p).equals(p) ? " . " : " * ");
		return src;
	}
	
	public String toString() {
		StringBuilder state = new StringBuilder();
		state.append("   ").append(in(B1D)).append(" ").append(in(B2D)).append("\n");
		state.append(" ").append(in(B1C)).append("     ").append(in(B2C)).append("\n");
		state.append("     ").append(in(B_U)).append("\n");
		state.append("\n");

		state.append("   ").append(in(B1m)).append(" ").append(in(B2m)).append("\n");
		state.append(" ").append(in(B1g)).append("     ").append(in(B2g)).append("\n");
		state.append(" ").append(in(F1g)).append("     ").append(in(F2g)).append("\n");
		state.append("   ").append(in(F1m)).append(" ").append(in(F2m)).append("\n");
		state.append("\n");
		
		state.append("     ").append(in(F_U)).append("\n");
		state.append(" ").append(in(F1C)).append("     ").append(in(F2C)).append("\n");
		state.append("   ").append(in(F1D)).append(" ").append(in(F2D)).append("\n");
		state.append("\n");
		return state.toString();
	}
	
	public Piece in(Piece loc) {
		return state[loc.ordinal()];
	}
	
	public void setIn(Piece loc, Piece newPiece) {
		state[loc.ordinal()] = newPiece;
	}
	
	public Piece[] reference = Piece.values().clone();
	public Piece[] state = Piece.values().clone();
	
	public static class Move {
		public final Orbit[] orbits;
		public final int[] directions;
		private int entered = 0;
		public Move(int num) {
			this.orbits = new Orbit[num];
			this.directions = new int[num];
		}
		public Move add(Orbit o, int dir) {
			orbits[entered] = o;
			directions[entered++] = dir;
			return this;
		}
	}
	
	public static class Orbit {
		public final Piece[] buffer;
		public final Piece[] members;
		public Orbit(Piece... members) {this.members = members;this.buffer = new Piece[members.length];}
		public boolean contains(Piece p) {
			for (Piece ps : members) if (ps == p) return true;
			return false;
		}
	}
	
	public static enum Piece {
		F_U('r', "F1"),
		F1D('r', "C3"),
		F1C('r', "D1"),
		F2D('r', "C2"),
		F2C('r', "E1"),
		F1m('b', "A2"),
		F1g('b', "A3"),
		F2m('g', "B3"),
		F2g('g', "B2"),
		B1m('b', "E3"),
		B1g('b', "E2"),
		B2m('g', "D2"),
		B2g('g', "D3"),
		B_U('o', "C1"),
		B1D('o', "F2"),
		B1C('o', "B1"),
		B2D('o', "F3"),
		B2C('o', "A1"),
		;
		final char color;
		final String altName;
		Piece(char color, String altName) {
			this.color = color;
			this.altName = altName;
		}
	}
}
