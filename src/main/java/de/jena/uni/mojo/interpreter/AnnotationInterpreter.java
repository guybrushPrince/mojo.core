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


import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.error.marker.Marker;
import de.jena.uni.mojo.util.store.ErrorAndWarningStore;

/**
 * Interprets each process annotation.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class AnnotationInterpreter {

	/**
	 * Interprets each annotation and creates a marker for each of them.
	 * 
	 * @param annotations
	 *            A collection of annotations.
	 * @param errorAndWarningStore
	 *            An error and warning store.
	 * @param interpreter
	 *            An id interpreter.
	 * @return A list of markers.
	 */
	public List<Marker> interpret(Collection<Annotation> annotations,
			ErrorAndWarningStore errorAndWarningStore, IdInterpreter interpreter) {

		// Iterate over each annotation and interpret it
		for (Annotation annotation : annotations) {

			errorAndWarningStore.addMarker(annotation.getAlarmCategory(),
					annotation.interpret(interpreter));

		}

		return errorAndWarningStore.getAllMarkers();
	}
}
