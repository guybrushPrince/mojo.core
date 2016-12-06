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
package de.jena.uni.mojo.analysis.edge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;


import de.jena.uni.mojo.Mojo;
import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * This analysis extracts the edges of the workflow graph node model.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class EdgeAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8163297782559502594L;

	/**
	 * A list of all edges within the workflow graph.
	 */
	private List<Edge> edges = new ArrayList<Edge>();

	/**
	 * An array of bit sets where each bit set contains incoming edges of the
	 * node with the id of the position in the array.
	 */
	private BitSet[] incoming;

	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	private BitSet[] outgoing;

	/**
	 * The constructor creates a new edge analysis.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The array node map.
	 * @param reporter
	 *            The analysis information.
	 */
	public EdgeAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
	}

	@Override
	protected List<Annotation> analyze() {
		// Determine the edges
		determineEdges();

		// Report the number of edges
		reporter.put(graph, AnalysisInformation.NUMBER_EDGES, edges.size());
		
		// Close the workflow graph
		graph.close(edges, incoming, outgoing);

		return Collections.emptyList();
	}

	/**
	 * Creates all edges of the workflow graph.
	 */
	private void determineEdges() {
		incoming = new BitSet[map.length];
		outgoing = new BitSet[map.length];

		// Determine the number of edges
		int numberEdges = 0;
		for (WGNode n : graph.getNodeListInclusive()) {
			numberEdges += n.getSuccessors().size();
		}
		for (int i = 0; i < incoming.length; i++) {
			incoming[i] = new BitSet(numberEdges);
			outgoing[i] = new BitSet(numberEdges);
		}

		// Create
		int counter = 0;
		for (WGNode src : graph.getNodeListInclusive()) {
			for (WGNode tgt : src.getSuccessors()) {
				Edge e = new Edge(counter++, src, tgt, numberEdges);
				outgoing[src.getId()].set(e.id);
				incoming[tgt.getId()].set(e.id);
				edges.add(e);
				if (Mojo.getCommand("VERBOSE").asBooleanValue())
					System.out.println("Created " + e);
			}
		}
	}
}
