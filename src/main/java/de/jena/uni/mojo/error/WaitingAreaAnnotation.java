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

import java.util.BitSet;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.error.marker.Marker;
import de.jena.uni.mojo.interpreter.AbstractEdge;
import de.jena.uni.mojo.interpreter.IdInterpreter;

/**
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class WaitingAreaAnnotation extends Annotation {

	/**
	 * The failure description.
	 */
	public static final String DESCRIPTION = "The select OR-join has "
			+ "to wait for all the other selected nodes if both are active.";
	
	/**
	 * The name of the waiting area attribute.
	 */
	public static final String WAITING_AREA = "WaitingArea";

	/**
	 * The waiting area.
	 */
	private BitSet waitingArea = new BitSet();

	/**
	 * The constructor defines an abundance annotation and hides the information
	 * about the description and the alarm category.
	 * 
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	public WaitingAreaAnnotation(Analysis analysis) {
		super(EAlarmCategory.INFO, DESCRIPTION, analysis);
	}

	/**
	 * The constructor defines an abundance annotation and hides the information
	 * about the alarm category.
	 * 
	 * @param description
	 *            The description of the failure annotation.
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	protected WaitingAreaAnnotation(String description, Analysis analysis) {
		super(EAlarmCategory.ERROR, DESCRIPTION, analysis);
	}

	/**
	 * Get the waiting area.
	 * 
	 * @return the waiting area
	 */
	public BitSet getWaitingArea() {
		return waitingArea;
	}

	/**
	 * Set the waiting area.
	 * 
	 * @param waitingArea
	 *            the waiting area.
	 */
	public void setWaitingArea(BitSet waitingArea) {
		this.waitingArea = waitingArea;
	}

	@Override
	public void printInformation(IdInterpreter interpreter) {
		super.printInformation(interpreter);

		// Extract the workflow graph edges
		List<Edge> wfgEdges = this.extractEdgePath(waitingArea);

		System.out.printf("\t\t%-35s: %s%n", "Waiting area (WFG)",
				wfgEdges.toString());

		// Print the process nodes
		System.out.printf("\t\t%-35s: %s%n", "Waiting area (Process)",
				this.extractAbstractPath(wfgEdges).toString());

	}

	@Override
	public Marker interpret(IdInterpreter interpreter) {
		Marker marker = super.interpret(interpreter);

		// Add additional information

		// Extract the workflow graph edges
		List<Edge> wfgEdges = this.extractEdgePath(waitingArea);
		// Extract the origin objects
		List<AbstractEdge> processEdges = this.extractAbstractPath(wfgEdges);

		marker.addProcessAnnotation(WAITING_AREA,
				interpreter.extractPath(processEdges), 4);

		return marker;
	}

}
