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
package de.jena.uni.mojo.error.marker;

import java.util.ArrayList;

/**
 * The marker information abstracts the nodes from its semantics by using a
 * specific symbol.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class MarkerInformation extends ArrayList<Object> {

	/**
	 * The serial version UID is needed for the array list.
	 */
	private static final long serialVersionUID = 3222704931999106093L;

	/**
	 * The name of the marker information.
	 */
	private final String name;

	/**
	 * A string with all (process) node ids.
	 */
	private final String nodes;

	/**
	 * The symbol.
	 */
	private final Object symbol;

	/**
	 * The constructor.
	 * 
	 * @param name
	 *            The name of the information.
	 * @param nodes
	 *            The node ids string.
	 * @param symbol
	 *            The symbol.
	 */
	public MarkerInformation(String name, String nodes, Object symbol) {
		this.name = name;
		this.nodes = nodes;
		this.symbol = symbol;
		this.add(name);
		this.add(nodes);
		this.add(symbol);
	}

	/**
	 * Get the name of the marker information.
	 * 
	 * @return The name of this marker information.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the node ids string.
	 * 
	 * @return Node ids string.
	 */
	public String getNodes() {
		return nodes;
	}

	/**
	 * Get the symbol.
	 * 
	 * @return The symbol.
	 */
	public Object getSymbol() {
		return symbol;
	}

}
