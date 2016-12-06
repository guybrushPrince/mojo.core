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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.error.marker.Marker;
import de.jena.uni.mojo.interpreter.AbstractEdge;
import de.jena.uni.mojo.interpreter.IdInterpreter;
import de.jena.uni.mojo.model.WGNode;

/**
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class DeadlockAnnotation extends Annotation {

	/**
	 * The failure description.
	 */
	public static final String DESCRIPTION = "There is no fork on a path from "
			+ "the start node (orange) to this join (red) where this fork ever "
			+ "makes it possible that this join (red) will be executed.";
	
	/**
	 * The name of the path to failure attribute.
	 */
	public static final String PATH_TO_FAILURE = "PathToFailure";
	
	/**
	 * The name of the failure nodes attribute.
	 */
	public static final String FAILURE_NODES = "FailureNodes";

	/**
	 * Defines paths from the source of the failure to the blocking join.
	 */
	private final List<BitSet> pathsToFailure = new ArrayList<BitSet>();

	/**
	 * The nodes which may cause the deadlock failure.
	 */
	private final Set<WGNode> failureNodes = new HashSet<WGNode>();

	/**
	 * The constructor defines an deadlock annotation and hides the information
	 * about the description and the alarm category.
	 * 
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	public DeadlockAnnotation(Analysis analysis) {
		this(DESCRIPTION, analysis);
	}

	/**
	 * The constructor defines an deadlock annotation and hides the information
	 * about the alarm category.
	 * 
	 * @param description
	 *            The description of the failure annotation.
	 * @param analysis
	 *            The analysis which defines this annotation.
	 */
	protected DeadlockAnnotation(String description, Analysis analysis) {
		super(EAlarmCategory.ERROR, DESCRIPTION, analysis);
	}

	/**
	 * Get the paths to the failure.
	 * 
	 * @return the pathToFailure
	 */
	public List<BitSet> getPathsToFailure() {
		return pathsToFailure;
	}

	/**
	 * Add the paths to the failure.
	 * 
	 * @param patshToFailure
	 *            the paths to the failure to add
	 */
	public void addPathsToFailure(Collection<BitSet> pathsToFailure) {
		this.pathsToFailure.addAll(pathsToFailure);
	}

	/**
	 * Add a single path to the failure.
	 * 
	 * @param pathToFailure
	 *            the path to failure to add.
	 */
	public void addPathToFailure(BitSet pathToFailure) {
		this.pathsToFailure.add(pathToFailure);
	}

	/**
	 * Get a list of the failure nodes.
	 * 
	 * @return The failure nodes.
	 */
	public List<WGNode> getFailureNodes() {
		return new ArrayList<WGNode>(this.failureNodes);
	}

	/**
	 * Add a collection of failure nodes.
	 * 
	 * @param nodes
	 *            The nodes to add.
	 */
	public void addFailureNodes(Collection<? extends WGNode> nodes) {
		this.failureNodes.addAll(nodes);
		this.addInvolvedNodes(nodes);
	}

	/**
	 * Add a failure node.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addFailureNode(WGNode node) {
		this.failureNodes.add(node);
		this.addInvolvedNode(node);
	}

	@Override
	public void printInformation(IdInterpreter interpreter) {
		super.printInformation(interpreter);

		System.out.printf("\t\t%-35s: %s%n", "Failure nodes (WFG)",
				failureNodes.toString());

		System.out.printf("\t\t%-35s: %s%n", "Failure nodes (Process)",
				getInterpretedFailureNodes().toString());

		System.out.printf("\t\t%-35s: %n", "Paths to the failure (WFG + Process)");

		int pathCounter = 0;
		for (BitSet path : this.pathsToFailure) {
			pathCounter++;
			
			// Extract the workflow graph edges
			List<Edge> wfgEdges = this.extractEdgePath(path);

			System.out.printf("\t\t\t%-20s: %s%n", "Path " + pathCounter + " (WFG)",
					wfgEdges.toString());

			// Print the process nodes
			System.out.printf("\t\t\t%-20s: %s%n",
					"Path " + pathCounter + " (Process)",
					this.extractAbstractPath(wfgEdges).toString());
		}

	}

	@Override
	public Marker interpret(IdInterpreter interpreter) {
		Marker marker = super.interpret(interpreter);

		// Add additional information
		int pathCounter = 0;
		for (BitSet path : pathsToFailure) {
			// Extract the workflow graph edges
			List<Edge> wfgEdges = this.extractEdgePath(path);
			// Extract the origin objects
			List<AbstractEdge> processEdges = this.extractAbstractPath(wfgEdges);

			marker.addProcessAnnotation(PATH_TO_FAILURE,
					interpreter.extractPath(processEdges), 4 + (pathCounter++));
		}

		// Create information about failure nodes
		marker.addProcessAnnotation(FAILURE_NODES,
				getIdString(getInterpretedFailureNodes(), interpreter), 3);

		return marker;
	}

	/**
	 * Returns a list of the failure nodes. However, instead of taking the
	 * workflow graph nodes, it uses the elements which are annotated to the
	 * nodes (e.g. BPMN elements).
	 * 
	 * @return A list of elements of the origin process.
	 */
	public List<Object> getInterpretedFailureNodes() {
		return extractOriginalNodes(failureNodes);
	}

}
