/**
 * Copyright 2013 mojo Friedrich Schiller University Jena
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Store for variables defined by analyses and transformations.
 * 
 * @author Norbert Spiess
 * 
 */
public class VariableStore {

	/**
	 * A map of variables and their values.
	 */
	private final Map<String, Object> variables = new HashMap<>();

	/**
	 * Get a variable value out of the store for the given identifier.
	 * 
	 * @param identifier
	 *            The identifier of the variables.
	 * @return an Object holding the latest variable value, null if there is
	 *         none set yet.
	 */
	public Object getVariableValue(final String identifier) {
		return variables.get(identifier);
	}

	/**
	 * Set a variable value in the store for the given identifier. Overwrites an
	 * existing mapping.
	 * 
	 * @param identifier
	 *            The identifier of the variable.
	 * @param value
	 *            The value of the variable.
	 */
	public void setVariableValue(final String identifier, final Object value) {
		variables.put(identifier, value);
	}

	/**
	 * Returns an iterator of the variables.
	 * 
	 * @return A variable iterator.
	 */
	public Set<Entry<String, Object>> getVariableIterator() {
		return variables.entrySet();
	}
}
