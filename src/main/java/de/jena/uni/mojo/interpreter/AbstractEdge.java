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
package de.jena.uni.mojo.interpreter;

/**
 * An abstract edge contains of an (abstract) source and target object.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class AbstractEdge {

	/**
	 * The source of the edge.
	 */
	public final Object source;

	/**
	 * The target of the edge.
	 */
	public final Object target;

	/**
	 * Creates a new abstract edge.
	 * 
	 * @param source
	 *            The source node of the edge.
	 * @param target
	 *            The target node of the edge.
	 */
	public AbstractEdge(Object source, Object target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return "(" + this.source + " -> " + this.target + ")";
	}

}
