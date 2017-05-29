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
package de.jena.uni.mojo.analysis.edge.abundance;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Definition;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.edge.dominance.DominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.AbundanceAnnotation;
import de.jena.uni.mojo.error.AbundanceCycleAnnotation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * This analysis finds all causes of potential abundances in the workflow graph.
 * In the first step it determines the phi-functions of each virtual variable,
 * where each virtual variable represents one fork node. Afterwards it reduces
 * these phi-functions to necessary ones.
 * 
 * @author Dipl.-Inf. Thomas Prinz
 * 
 */
public class AbundanceAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7041645510287999997L;

	/**
	 * A constant which describes analysis information.
	 */
	public final static String ABUNDANCE_NUMBER_VISITED_EDGES = "ABUNDANCE_NUMBER_VISITED_EDGES";

	/**
	 * A list of all edges within the workflow graph.
	 */
	public final List<Edge> edges;

	/**
	 * An array of bit sets where each bit set contains incoming edges of the
	 * node with the id of the position in the array.
	 */
	public final BitSet[] incoming;

	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	public final BitSet[] outgoing;

	/**
	 * A counter that counts the definitions.
	 */
	private int definitionsCounter = 0;

	/**
	 * A list of created definitions.
	 */
	private final ArrayList<Definition> definitions = new ArrayList<Definition>();

	/**
	 * A list of found errors.
	 */
	private final LinkedList<Annotation> errors = new LinkedList<Annotation>();

	/**
	 * A counter that stores the number of visited edges during the analysis.
	 */
	private int edgesVisited = 0;

	/**
	 * The constructor of the abundance analysis.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 * @param edgeAnalysis
	 *            The dominator edge analysis.
	 */
	public AbundanceAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, DominatorEdgeAnalysis edgeAnalysis) {
		super(graph, map, reporter);
		this.edges = edgeAnalysis.edges;
		this.incoming = edgeAnalysis.incoming;
		this.outgoing = edgeAnalysis.outgoing;
	}

	@Override
	protected List<Annotation> analyze() {
		// IMPORTANT: A dominator edge analysis must have been already performed

		// Step 1: Compute the dominance frontier for each outgoing edge
		// of the fork nodes of the workflow graph.
		// It is already calculated by the dominator edge analysis.
		// (Uses concepts of "Efficiently Computing Static Single
		// Assignment Form and the Control Dependence Graph", Cytron et al.
		// p. 466).

		// Step 2: Determine the places for phi-functions
		setPhiFunctions();
		
		// Step 2b: Determine the dependencies of the edges
		determineExecDependencies();

		// Step 3:
		// Build the network graph
		NetworkGraph network = new NetworkGraph(graph, map, this.reporter);
		// For each fork initialize it
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		for (WGNode fork: graph.getForkList()) {
			network.transformFor(fork);
			for (Definition def: definitions) {
				if (def.getForkEdge().tgt.getId() == fork.getId()) {
					if (def.getEdge().src.getType() == Type.JOIN ||
							def.getEdge().src.getType() == Type.OR_JOIN ||
							!network.setCapacities(fork, def.getEdge())) continue;
					// Determine the max flow
					errors.addAll(network.compute());
					List<BitSet> paths = network.getLastResult(); 
					
					if (paths.size() == 1) {
						// This is NO!! synchronization point						
					} else {
						// This is a synchronization point
						WGNode src = def.getEdge().src;
						if (src.getType() != Type.JOIN &&
							src.getType() != Type.OR_JOIN) {
							reporter.startIgnoreTimeMeasurement(graph, this.getClass().getName());
							// Define a new abundance annotation
							AbundanceAnnotation annotation;
							if (src.getType() == Type.FORK || 
									src.getType() == Type.OR_FORK) {
								annotation = new AbundanceCycleAnnotation(this);
							} else {
								annotation = new AbundanceAnnotation(this);
							}

							// The printable node is the merge
							annotation.addPrintableNode(src);

							// It has a node that causes the failure - the opening node,
							// i.e., the start of the component
							annotation.addOpeningNode(fork);

							// Map all nodes of all paths to one set
							for (BitSet path: paths) {
								annotation.addPathToFailure(path);
							}

							if (src.getType() == Type.FORK || 
									src.getType() == Type.OR_FORK) {
								reporter.add(
										graph,
										AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION_LOOP,
										1);
							} else {
								reporter.add(
										graph,
										AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION_NORMAL,
										1);
							}
							errors.add(annotation);
							reporter.endIgnoreTimeMeasurement(graph, this.getClass().getName());
						}
					}
				}
			}
		}
		if (network != null) {
			edgesVisited += network.getVisitedEdges();
		}

		// Store information about the analysis.
		reporter.put(graph, AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION,
				errors.size());
		reporter.put(graph, ABUNDANCE_NUMBER_VISITED_EDGES, edgesVisited);

		return errors;
	}

	/**
	 * Sets the phi-functions for each virtual variable of each fork node.
	 */
	private void setPhiFunctions() {
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		for (WGNode fork : forks) {
			// An edge is visited
			edgesVisited++;

			// Get the incoming edge
			Edge in = edges.get(incoming[fork.getId()].nextSetBit(0));

			// Get the edges where the virtual variables are defined
			// (i.e., the outgoing edges of the fork)
			BitSet defineEdges = (BitSet) outgoing[fork.getId()].clone();
			while (!defineEdges.isEmpty()) {
				// An edge is visited
				edgesVisited++;

				int next = defineEdges.nextSetBit(0);
				defineEdges.clear(next);
				Edge n = edges.get(next);
				
				// Create a definition for the outgoing edge.
				Definition outDef = new Definition(definitionsCounter++,
						n, in);
				definitions.add(outDef);

				for (int s = n.dominanceFrontierSet.nextSetBit(0); s >= 0; s = n.dominanceFrontierSet
						.nextSetBit(s + 1)) {
					// An edge is visited
					edgesVisited++;

					Edge syncEdge = edges.get(s);
					if (!in.syncEdges.get(syncEdge.id)) {
						in.syncEdges.set(syncEdge.id);


						Definition def = new Definition(definitionsCounter++,
								syncEdge, in);
						definitions.add(def);
						def.isPhi = true;

						if (in.postDominatorList.getLast().id != syncEdge.id
								&& n.postDominatorList.getLast().id != syncEdge.id) {
							defineEdges.set(syncEdge.id);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Performs a depth first search on the `allowed` edges.
	 * @param current The current edge.
	 * @param allowed The allowed edges.
	 * @param visited The already visited edges.
	 */
	private void depthFirstSearch(
			int current, 
			BitSet allowed, 
			BitSet visited) {
		edgesVisited++;
		visited.set(current);
		Edge e = this.edges.get(current);
		BitSet succ = (BitSet) this.outgoing[e.tgt.getId()].clone();
		succ.andNot(visited);
		succ.and(allowed);
		for (int s = succ.nextSetBit(0); s >= 0; s = succ.nextSetBit(s + 1)) {
			depthFirstSearch(s, allowed, visited);
		}
	}
	
	/**
	 * Determines the execution dependencies of each meeting point.
	 */
	private void determineExecDependencies() {
		// Build an all edges bit set
		BitSet edgesSet = new BitSet(this.edges.size());
		edgesSet.set(0, this.edges.size());
		BitSet oldReachable = new BitSet(this.edges.size());
		BitSet remaining = new BitSet(this.edges.size());
		BitSet reachable = new BitSet(this.edges.size());
		
		// Determine a set of all outgoing edges
		BitSet outgoingJoins = new BitSet(this.edges.size());
		BitSet joins = this.graph.getJoinSet();
		for (int join = joins.nextSetBit(0); join >= 0; join = joins.nextSetBit(join + 1)) {
			outgoingJoins.or(this.outgoing[join]);
		}
		
		// The start edge
		int start = outgoing[graph.getStart().getId()].nextSetBit(0);
		
		// Determine the execution dependencies for each edge.
		for (Definition def: definitions) {
			edgesVisited++;			
			Edge cur = def.getEdge();
			if (cur.src.getType() == Type.JOIN || 
				cur.src.getType() == Type.OR_JOIN) continue;
			
			reachable.set(0, this.edges.size());
			reachable.clear(cur.id);
			oldReachable.set(0, this.edges.size());
			do {
				oldReachable.and(reachable);
				edgesVisited++;
				
				// Perform a modified depth first search on remaining edges
				remaining.clear();
				depthFirstSearch(start, reachable, remaining);
				reachable.and(remaining);

				for (int join = joins.nextSetBit(0); join >= 0; join = joins.nextSetBit(join + 1)) {
					edgesVisited++;
					remaining.or(reachable);
					remaining.and(this.incoming[join]);
					if (remaining.cardinality() < this.incoming[join].cardinality()) {
						reachable.andNot(this.outgoing[join]);
					}
				}
			} while (oldReachable.cardinality() > reachable.cardinality());
					
			// We have reached a stable point, i.e., no edges can be eliminated
			// from the graph anymore.
			// Determine the edges being dependent from the current edge. These
			// all edges being not in the all set.
			remaining.set(0, this.edges.size());
			remaining.andNot(reachable);
			remaining.and(outgoingJoins); // Only those of joins are welcome
			cur.dependent.or(remaining);			
		}
	}
}
