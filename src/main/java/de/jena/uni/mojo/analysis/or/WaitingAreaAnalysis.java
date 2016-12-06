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
package de.jena.uni.mojo.analysis.or;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.error.WaitingAreaAnnotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.sub.Data;

/**
 * Determines the waiting area for each or-join node.
 * As result, it gives information about this waiting area.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class WaitingAreaAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1508675035996945705L;

	/**
	 * A list of all edges within the workflow graph.
	 */
	private final List<Edge> edges;

	/**
	 * An array of bit sets where each bit set contains incoming edges of the
	 * node with the id of the position in the array.
	 */
	private final BitSet[] incoming;

	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	private final BitSet[] outgoing;
	
	/**
	 * The waiting area analysis constructor.
	 * 
	 * @param graph
	 *            The workflow graph for which the waiting areas are determined.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public WaitingAreaAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.edges = graph.getEdges();
		this.incoming = graph.getIncomingEdges();
		this.outgoing = graph.getOutgoingEdges();
	}
	
	@ Override
	protected List<Annotation> analyze() {
		List<Annotation> informations = new ArrayList<Annotation>();
		
		// Iterate over each or-join since we want to determine the 
		// waiting area for each of them
		for (WGNode orjoin: graph.getOrJoinList()) {
			
			// Get the set of incoming edges of the or-join (as copy)
			BitSet in = (BitSet) incoming[orjoin.getId()].clone();
			
			// Get the outgoing edge
			Edge outEdge = edges.get(outgoing[orjoin.getId()].nextSetBit(0));
			
			// Determine the post dominators of this edge.
			BitSet postDom = (BitSet) outEdge.postDominatorSet.clone();
			
			// Create a waiting area
			BitSet waitingArea = new BitSet(edges.size());
			
			// Remove all edges of the incoming edges which post dominate 
			// the outgoing edge
			in.andNot(postDom);
			
			// Perform a depth first search on the inverse workflow graph.
			for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
				depthFirstSearch(i, postDom, waitingArea);
			}
			
			// Set the waiting area of the or-join node.			
			Data data = (Data) orjoin.getExtraInformation();
			data.waitingArea.clear();
			data.waitingArea.or(waitingArea);
			
			// Add an annotation to the workflow how this or-join
			// works.
			WaitingAreaAnnotation annotation = new WaitingAreaAnnotation(this);
			
			// The printable node is the or-join
			annotation.addPrintableNode(orjoin);
			// The waiting area is the waiting area itself
			BitSet copy = (BitSet) ((Data) orjoin.getExtraInformation()).waitingArea.clone();
			annotation.setWaitingArea(copy);

			informations.add(annotation);
		}
		
		return informations;
	}

	/**
	 * This depth first search method searches in the inverse workflow graph
	 * for all edges with a path without a post dominator.
	 * 
	 * @param current The current edge id.
	 * @param pdom The (bit) set of post dominators.
	 * @param waitingArea The (bit) set of the current waiting area.
	 */
	private void depthFirstSearch(int current, BitSet pdom, BitSet waitingArea) {
		// The current edge is part of the waiting area.
		waitingArea.set(current);
		// Look at the predecessor edges.
		Edge cur = edges.get(current);
		
		// Get a copy of the predecessors of the current edge
		BitSet predCopy = (BitSet) incoming[cur.src.getId()].clone();
		
		// Remove all edges within the waiting area
		predCopy.andNot(waitingArea);
		// Remove all edges which post dominates the outgoing edge
		predCopy.andNot(pdom);
		
		// Visit each other 
		for (int pred = predCopy.nextSetBit(0); 
				 pred >= 0; 
				 pred = predCopy.nextSetBit(pred + 1)) {
			depthFirstSearch(pred, pdom, waitingArea);
		}
	}

}
