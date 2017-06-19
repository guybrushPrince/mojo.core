/**
 * Copyright 2017 mojo Friedrich Schiller University Jena
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
import java.util.Stack;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

public class StrongComponentsAnalysis extends Analysis {

	/**
	 * The serial version UID 
	 */
	private static final long serialVersionUID = 868407652152225864L;
	
	/**
	 * A constant for the analysis reporter.
	 */
	public final static String STRONG_COMPONENT_NUMBER_VISITED_EDGES = "STRONG_COMPONENT_NUMBER_VISITED_EDGES";

	/**
	 * The list of edges.
	 */
	private final List<Edge> edges;
	
	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	private BitSet[] outgoing;

	/**
	 * The current index.
	 */
	private int index = 0;
	
	/**
	 * A counter for the components.
	 */
	private int componentsCounter = 0;

	/**
	 * The stack of edges.
	 */
	private Stack<Edge> stack = new Stack<Edge>();
	
	/**
	 * The number of edges which are visited by this algorithm.
	 */
	private int edgesVisited = 0;
	
	/**
	 * The list of components (cycles).
	 */
	private final ArrayList<BitSet> components = new ArrayList<BitSet>();


	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The array node map.
	 * @param reporter
	 *            The analysis information.
	 */
	public StrongComponentsAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.edges = graph.getEdges();
		this.outgoing = graph.getOutgoingEdges();
	}

	@Override
	protected List<Annotation> analyze() {
		//System.out.println(edges.size());
		for (Edge edge : edges) {
			if (edge.index == -1) strongConnect(edge);
		}
		// Put some information into the reporter about the number of
		// visited edges.
		reporter.put(graph, STRONG_COMPONENT_NUMBER_VISITED_EDGES, edgesVisited);
		return Collections.emptyList();
	}

	/**
	 * Perform the algorithm of Tarjan.
	 * 
	 * @param edge
	 *            The edge where it starts from.
	 */
	private void strongConnect(Edge edge) {
		this.edgesVisited++;
		edge.index = index;
		edge.lowlink = index++;

		stack.push(edge);

		BitSet out = this.outgoing[edge.tgt.getId()];
		for (int s = out.nextSetBit(0); s >= 0; s = out.nextSetBit(s + 1)) {
			Edge succ = edges.get(s);
			if (succ.index == -1) {
				strongConnect(succ);
				edge.lowlink = Math.min(edge.lowlink, succ.lowlink);
			} else if (stack.contains(succ)) {
				edge.lowlink = Math.min(edge.lowlink, succ.index);
			}
		}

		if (edge.lowlink == edge.index) {
			BitSet comp = new BitSet(edges.size());
			int component = this.componentsCounter;
			int numEdges = 0;
			Edge current;
			Edge first = null;
			do {
				this.edgesVisited++;
				current = stack.pop();
				if (first == null) first = current;
				current.component = component;
				comp.set(current.id);
				numEdges++;
				if (numEdges > 1) current.inCycle = true;
			} while (current != edge);
			if (numEdges > 1) {
				first.inCycle = true;
				this.componentsCounter++;
				this.components.add(comp);
			}
		}
	}
	
	/**
	 * Get the components.
	 * @return The components as ArrayList.
	 */
	public ArrayList<BitSet> getComponents() {
		return this.components;
	}

}
