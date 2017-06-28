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
package de.jena.uni.mojo.error;

import de.jena.uni.mojo.analysis.CoreAnalysis;

/**
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class StartWithPredecessorAnnotation extends Annotation {

	/**
	 * The failure description.
	 */
	public static final String DESCRIPTION = "The start event has at "
			+ "least one incoming edge. Since that is not possible in "
			+ "processes, it is handled as a task.";

	/**
	 * The constructor defines an start with predecessor annotation and hides
	 * the information about the description and the alarm category.
	 * 
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	public StartWithPredecessorAnnotation(CoreAnalysis analysis) {
		super(EAlarmCategory.WARNING, DESCRIPTION, analysis);
	}

}
