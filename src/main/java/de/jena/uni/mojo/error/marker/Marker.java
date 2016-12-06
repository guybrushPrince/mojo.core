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
import java.util.Collection;
import java.util.List;

/**
 * Class for marking warnings and errors raised by verification and
 * transformations. The markers are displayed in the graph and the problems view
 * of Eclipse.
 * 
 * @author Norbert Spiess
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Marker {

	/**
	 * The message of the marker.
	 */
	private final String message;

	/**
	 * The process annotations.
	 */
	private final Collection<List<Object>> processAnnotations = new ArrayList<List<Object>>();

	/**
	 * Create a new marker.
	 * 
	 * @param message
	 *            The message of the marker.
	 */
	public Marker(final String message) {
		this.message = message;
	}

	/**
	 * Get the message of the marker.
	 * 
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Add a process annotation in form of a marker information.
	 * 
	 * @param markerInformation
	 *            The marker information to add.
	 */
	public void addProcessAnnotation(MarkerInformation markerInformation) {
		this.processAnnotations.add(markerInformation);
	}

	/**
	 * Add a process annotation with a given name, given nodes and a symbol.
	 * 
	 * @param name
	 *            The name of the annotation marker.
	 * @param nodes
	 *            The involved nodes.
	 * @param symbol
	 *            The symbol how to decorate.
	 */
	public void addProcessAnnotation(String name, String nodes, Object symbol) {
		this.processAnnotations.add(new MarkerInformation(name, nodes, symbol));
	}

	/**
	 * Add a collection of marker information.
	 * 
	 * @param markerInformations
	 *            A collection of marker information.
	 */
	public void addProcessAnnotations(
			Collection<MarkerInformation> markerInformations) {
		this.processAnnotations.addAll(markerInformations);
	}

	/**
	 * Get the marker information as lists.
	 * 
	 * @return The marker information.
	 */
	public Collection<List<Object>> getProcessAnnotations() {
		return this.processAnnotations;
	}

}
