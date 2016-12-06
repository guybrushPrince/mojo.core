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

import java.util.Collection;
import java.util.List;

/**
 * An IdInterpreter is an abstract class that allows a more general id
 * extraction of nodes or edges. It makes it possible to extract ids without the
 * knowledge of the real model.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class IdInterpreter {

	/**
	 * Defines how the id of the given object is extracted.
	 * 
	 * @param obj
	 *            The object which has an id.
	 * @return The id of the object as a string.
	 */
	public abstract String extractId(Object obj);

	/**
	 * Defines how to extract edge ids of a set of nodes which should define a
	 * path.
	 * 
	 * @param nodes
	 *            The collection of nodes.
	 * @return A comma separated string of ids.
	 */
	public abstract String extractPath(Collection<Object> nodes);

	/**
	 * Defines how to extract original edge ids of a set of abstract edges which
	 * should define a path.
	 * 
	 * @param edges
	 *            The list of edges.
	 * @return A comma separated string of ids.
	 */
	public abstract String extractPath(List<AbstractEdge> edges);

}
