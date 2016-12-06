/**
 * Copyright 2016 mojo Friedrich Schiller University Jena
 * 
 * This file is part of mojo.
 * 
 * mojo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * mojo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with mojo. If not, see <http://www.gnu.org/licenses/>.
 */
package de.jena.uni.mojo.analysis.edge;


/**
 * A definition represents the definition of a virtual variable. For reasons of
 * efficiency, most fields are public and final.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Definition {

	/**
	 * The id of the definition.
	 */
	public final int id;

	/**
	 * The edge that defines this definition.
	 */
	private final Edge edge;

	/**
	 * The edge which defines the virtual variable.
	 */
	private final Edge forkEdge;

	/**
	 * Whether this definition is a phi function.
	 */
	public boolean isPhi = false;

	/**
	 * The constructor of the definition.
	 * 
	 * @param id
	 *            The id of the definition.
	 * @param edge
	 *            The edge which defines this definition.
	 * @param forkEdge
	 *            The edge which defines the virtual variable.
	 */
	public Definition(int id, Edge edge, Edge forkEdge) {
		this.id = id;
		this.edge = edge;
		this.forkEdge = forkEdge;
	}

	/**
	 * Get the edge where this definition is defined.
	 * 
	 * @return The edge.
	 */
	public Edge getEdge() {
		return this.edge;
	}

	/**
	 * Get the edge which defines the virtual variable.
	 * 
	 * @return The edge which defines the virtual variable.
	 */
	public Edge getForkEdge() {
		return this.forkEdge;
	}

	@Override
	public String toString() {
		String s = "[" + id + "]" + "v[" + forkEdge.id + "]";
		return s;
	}
}
