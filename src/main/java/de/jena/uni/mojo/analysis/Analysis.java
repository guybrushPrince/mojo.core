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
package de.jena.uni.mojo.analysis;

import java.util.List;


import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * The analysis class is the core of Mojo since each analysis inherits this
 * abstract class. The abstract class itself is a RecursiveTask, i.e., it can be
 * run in parallel by a fork-join-pool. Furthermore, the analysis class measures
 * the time of each computation of the implementing class.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class Analysis extends CoreAnalysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2145403605821913673L;

	/**
	 * The workflow graph which should be analyzed.
	 */
	protected final WorkflowGraph graph;

	/**
	 * The workflow graph node array map.
	 */
	protected WGNode[] map;

	/**
	 * The constructor defines an analysis consisting of a workflow graph, a map
	 * which contains each node and an analysis information reporter that
	 * measures for example the time.
	 * 
	 * @param graph
	 *            The workflow graph to analyze.
	 * @param map
	 *            A node array map.
	 * @param reporter
	 *            An analysis information reporter
	 */
	public Analysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super("", reporter);
		this.graph = graph;
		this.map = map;
	}
	
	/**
	 * Returns the map of workflow graph nodes.
	 * 
	 * @return The map of workflow graph nodes.
	 */
	public final WGNode[] getNodeMap() {
		return this.map;
	}
	
	/**
	 * Returns the workflow graph.
	 * 
	 * @return The workflow graph.
	 */
	public final WorkflowGraph getWorkflowGraph() {
		return this.graph;
	}

	/**
	 * An abstract method which defines the entry point of the analysis. As
	 * result it gives back a list of some error information.
	 * 
	 * @return A list of error information.
	 */
	protected abstract List<Annotation> analyze();

	/**
	 * This computation method must been used at runtime to guarantee a right
	 * behavior. Then, the method can be executed in parallel with other
	 * parallel tasks. Furthermore, this method automatically adds an entry of
	 * time measurement for this analysis.
	 * 
	 * @return A list of error information.
	 */
	public List<Annotation> compute() {
		reporter.startTimeMeasurement(graph, this.getClass().getName());
		List<Annotation> list = analyze();
		reporter.endTimeMeasurement(graph, this.getClass().getName());
		return list;
	}
}
