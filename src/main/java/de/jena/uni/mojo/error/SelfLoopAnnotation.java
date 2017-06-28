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
public class SelfLoopAnnotation extends Annotation {

	/**
	 * The failure description.
	 */
	public static final String DESCRIPTION = "A task, gateway, or event cannot "
			+ "be connected to itself.";

	/**
	 * The constructor defines a self loop annotation and hides the
	 * information about the description and the alarm category.
	 * 
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	public SelfLoopAnnotation(CoreAnalysis analysis) {
		super(EAlarmCategory.WARNING, DESCRIPTION, analysis);
	}

}
