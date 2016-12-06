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
package de.jena.uni.mojo.util.store;

import java.util.ArrayList;
import java.util.List;


import de.jena.uni.mojo.error.EAlarmCategory;
import de.jena.uni.mojo.error.marker.Marker;
import de.jena.uni.mojo.info.IPluginStrings;

/**
 * Store holding warnings and errors created by the transformation and analyses.
 * 
 * @author Norbert Spiess
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class ErrorAndWarningStore {

	/**
	 * A list to store the warnings.
	 */
	private final List<Marker> warnings = new ArrayList<>();

	/**
	 * A list to store the errors.
	 */
	private final List<Marker> errors = new ArrayList<>();

	/**
	 * A list to store the additional information.
	 */
	private final List<Marker> infos = new ArrayList<>();

	/**
	 * Get the warnings stored in this store.
	 * 
	 * @return A list of warning markers.
	 */
	public List<Marker> getWarnings() {
		return warnings;
	}

	/**
	 * Get the informations stored in this class.
	 * 
	 * @return A list of information markers.
	 */
	public List<Marker> getInformations() {
		return infos;
	}

	/**
	 * Get the errors stored in this store.
	 * 
	 * @return A list of errors markers.
	 */
	public List<Marker> getErrors() {
		return errors;
	}

	/**
	 * Get all errors, warnings and informations.
	 * 
	 * @return A copy of all errors, warnings and informations.
	 */
	public List<Marker> getAllMarkers() {
		List<Marker> markers = new ArrayList<Marker>(this.getErrors());
		markers.addAll(this.getWarnings());
		markers.addAll(this.getInformations());

		return markers;
	}

	/**
	 * Add a new marker to the store.
	 * 
	 * @param category
	 *            The type of annotation marker.
	 * @param marker
	 *            The marker.
	 */
	public void addMarker(final EAlarmCategory category, final Marker marker) {
		switch (category) {
		case ERROR:
			errors.add(marker);
			break;
		case WARNING:
			warnings.add(marker);
			break;
		case INFO:
			infos.add(marker);
			break;
		default:
			throw new IllegalArgumentException(IPluginStrings.UNEXPECTED_TYPE
					+ category);
		}
	}

}
