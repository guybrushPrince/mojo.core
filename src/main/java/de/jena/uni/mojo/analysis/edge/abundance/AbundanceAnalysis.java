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
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.edge.StrongComponentsAnalysis;
import de.jena.uni.mojo.analysis.edge.dominance.DominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.AbundanceAnnotation;
import de.jena.uni.mojo.error.AbundanceCycleAnnotation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WGNode.Type;
import de.jena.uni.mojo.model.WorkflowGraph;

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
	 * A list of found errors.
	 */
	private final LinkedList<Annotation> errors = new LinkedList<Annotation>();

	/**
	 * A counter that stores the number of visited edges during the analysis.
	 */
	private int edgesVisited = 0;
	
	/**
	 * Stores whether an edge has definitions or not
	 */
	private final BitSet hasDefinitions;
	
	/**
	 * The meeting points for each fork
	 */
	private final BitSet[] meetingPoints;
	
	/**
	 * The components (parts of the graph) which build cycles
	 */
	private final ArrayList<BitSet> components;
	
	/**
	 * A bitset containing each outgoing edge of all join nodes. 
	 */
	private BitSet outgoingJoins;

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
	 * @param strongAnalysis
	 * 			  The strong connected components analysis.
	 */
	public AbundanceAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, DominatorEdgeAnalysis edgeAnalysis,
			StrongComponentsAnalysis strongAnalysis) {
		super(graph, map, reporter);
		this.edges = edgeAnalysis.edges;
		this.incoming = edgeAnalysis.incoming;
		this.outgoing = edgeAnalysis.outgoing;
		this.hasDefinitions = new BitSet(edges.size());
		this.meetingPoints = new BitSet[map.length];
		// Initialize the sets for the meeting points
		for (WGNode fork: graph.getForkList()) {
			this.meetingPoints[fork.getId()] = new BitSet(edges.size());
		}
		for (WGNode orfork: graph.getOrForkList()) {
			this.meetingPoints[orfork.getId()] = new BitSet(edges.size());
		}
		this.components = strongAnalysis.getComponents();
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
		
		// Step 2a: Determine the scope of the forks
		determineBonds();

		// Step 2b: Determine the places for phi-functions
		setPhiFunctions();
		
		// Step 2c: Determine the dependencies of the edges
		determineExecDependencies();
		
		// Step 3:
		// Build the network graph
		NetworkGraph network = new NetworkGraph(graph, map, this.reporter);
		// For each fork initialize it
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		BitSet checked = new BitSet(edges.size());
		for (WGNode fork: graph.getForkList()) {
			boolean isTransformed = false;
			// Clear the already checked set
			checked.clear();
			BitSet meetPoints = this.meetingPoints[fork.getId()];
			for (int m = meetPoints.nextSetBit(0); m >= 0; m = meetPoints.nextSetBit(m + 1)) {
				Edge meetingPoint = edges.get(m);
				if (meetingPoint.src.getType() == Type.JOIN ||
						meetingPoint.src.getType() == Type.OR_JOIN ||
						checked.get(meetingPoint.id)) continue;
				
				Edge in = edges.get(incoming[fork.getId()].nextSetBit(0));
				
				// We do not have to visit meeting points which are an outgoing edge
				// of a fork if the fork is not within a cycle.
				if (meetingPoint.src.getId() == fork.getId() && !in.bond.get(in.id)) continue;
				
				// Transform the network graph if needed.
				if (!isTransformed) {
					network.transformFor(fork);
					isTransformed = true;
				}
				// Set the capacities
				if (!network.setCapacities(fork, meetingPoint)) continue;
				// Determine the max flow
				errors.addAll(network.compute());
				List<BitSet> paths = network.getLastResult();
				
				checked.set(meetingPoint.id);
				
				if (paths.size() <= 1) {
					// This is NO!! synchronization point						
				} else {
					// This is a synchronization point
					WGNode src = meetingPoint.src;
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
		if (network != null) {
			edgesVisited += network.getVisitedEdges();
		}
		
		// Store information about the analysis.
		reporter.put(graph, AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION,
				errors.size());
		reporter.put(graph, ABUNDANCE_NUMBER_VISITED_EDGES, edgesVisited);
		
		/*int sum = 0;
		for (BitSet mp: this.meetingPoints) {
			if (mp != null) sum += mp.cardinality();
		}
		
		reporter.put(graph, "SUM_MEETING_POINTS", sum);*/
		
		return errors;
	}
	
	/**
	 * Determine the necessary edges which should be visited for each fork.
	 */
	private void determineBonds() {
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		BitSet allowed = new BitSet(edges.size());
		for (WGNode fork: forks) {
			allowed.set(0, edges.size());
			int in = this.incoming[fork.getId()].nextSetBit(0);
			Edge inEdge = edges.get(in);
			int pdom = inEdge.postDominatorList.getLast().id;
			allowed.clear(pdom);
			for (int s = outgoing[fork.getId()].nextSetBit(0); s >= 0; s = outgoing[fork.getId()].nextSetBit(s + 1)) {
				depthFirstSearch(s, allowed, inEdge.bond);
			}
			inEdge.bond.set(pdom);			
		}
	}

	/**
	 * Sets the phi-functions for each virtual variable of each fork node.
	 */
	private void setPhiFunctions() {
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		BitSet defineEdges = new BitSet(this.edges.size());
		for (WGNode fork : forks) {
			// An edge is visited
			edgesVisited++;

			// Get the incoming edge
			Edge in = edges.get(incoming[fork.getId()].nextSetBit(0));

			// Get the edges where the virtual variables are defined
			// (i.e., the outgoing edges of the fork)
			defineEdges.clear();
			defineEdges.or(outgoing[fork.getId()]);
			while (!defineEdges.isEmpty()) {
				// An edge is visited
				edgesVisited++;

				int next = defineEdges.nextSetBit(0);
				defineEdges.clear(next);
				Edge n = edges.get(next);
				
				// If the edge is the outgoing edge of the fork...
				if (n.src.getId() == fork.getId()) {
					// ... and the fork is within a cycle ...
					if (in.bond.get(in.id)) {
						// ... then it could be important meeting point
						this.meetingPoints[fork.getId()].set(n.id);
						this.hasDefinitions.set(n.id);
					}
				} else {
					// It is an important meeting point
					meetingPoints[fork.getId()].set(n.id);
					if (n.src.getType() != Type.JOIN && 
						n.src.getType() != Type.OR_JOIN) {
						this.hasDefinitions.set(n.id);
					}
				}

				for (int s = n.dominanceFrontierSet.nextSetBit(0); s >= 0; s = n.dominanceFrontierSet
						.nextSetBit(s + 1)) {
					// An edge is visited
					edgesVisited++;

					Edge syncEdge = edges.get(s);
					if (!in.syncEdges.get(syncEdge.id)) {
						in.syncEdges.set(syncEdge.id);


						meetingPoints[fork.getId()].set(syncEdge.id);
						if (syncEdge.src.getType() != Type.JOIN && 
							syncEdge.src.getType() != Type.OR_JOIN) {
							this.hasDefinitions.set(syncEdge.id);
						}

						if (in.postDominatorList.getLast().id != syncEdge.id
								&& n.postDominatorList.getLast().id != syncEdge.id) {
							if (in.bond.get(syncEdge.id)) {
								defineEdges.set(syncEdge.id);
							}
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
		int numEdges = this.edges.size();
		
		// Build an all edges bit set
		BitSet oldReachable = new BitSet(numEdges);
		BitSet remaining = new BitSet(numEdges);
		BitSet reachable = new BitSet(numEdges);
		
		// Determine a set of all outgoing edges
		this.outgoingJoins = new BitSet(numEdges);
		BitSet ogJoins = this.outgoingJoins;
		BitSet joins = this.graph.getJoinSet();
		for (int join = joins.nextSetBit(0); join >= 0; join = joins.nextSetBit(join + 1)) {
			ogJoins.or(this.outgoing[join]);
		}
		
		// Determine the execution dependencies for each edge.
		List<WGNode> forks = new ArrayList<WGNode>(graph.getForkList());
		forks.addAll(graph.getOrForkList());
		for (WGNode fork: forks) {
			BitSet meetingPoints = this.meetingPoints[fork.getId()];
			int in = this.incoming[fork.getId()].nextSetBit(0);
			Edge inEdge = edges.get(in);
			for (int m = meetingPoints.nextSetBit(0); m >= 0; m = meetingPoints.nextSetBit(m + 1)) {
				Edge cur = edges.get(m);
				edgesVisited++;
				
				boolean ignore = true;
				// If the current meeting point is in a cycle ...
				if (cur.inCycle) {
					BitSet comp = (BitSet) this.components.get(cur.component).clone();
					comp.and(this.hasDefinitions);
					// ... and it lies with at least one other meeting point within the cycle...
					if (!comp.isEmpty()) {
						// ... we cannot ignore it.
						ignore = false;
					}
				}
	
				if (ignore) continue;
				if (!this.hasDefinitions.get(cur.id)) continue;
				
				reachable.or(inEdge.bond);
				reachable.clear(cur.id);
				oldReachable.set(0, numEdges);
				do {
					oldReachable.and(reachable);
					edgesVisited++;
					
					// Perform a modified depth first search on remaining edges
					remaining.clear();
					depthFirstSearch(in, reachable, remaining);
					reachable.and(remaining);
	
					for (int join = joins.nextSetBit(0); join >= 0; join = joins.nextSetBit(join + 1)) {
						edgesVisited++;
						remaining.clear();
						remaining.or(inEdge.bond);
						remaining.and(this.incoming[join]);
						int inEdges = remaining.cardinality();
						
						remaining.clear();
						remaining.or(reachable);
						remaining.and(this.incoming[join]);
						if (remaining.cardinality() < inEdges) {
							reachable.andNot(this.outgoing[join]);
						}
					}
				} while (oldReachable.cardinality() > reachable.cardinality());
						
				// We have reached a stable point, i.e., no edges can be eliminated
				// from the graph anymore.
				// Determine the edges being dependent from the current edge. These
				// all edges being not in the all set.
				//remaining.set(0, numEdges);
				remaining.or(inEdge.bond);
				remaining.andNot(reachable);
				remaining.and(this.outgoingJoins); // Only those of joins are welcome
				remaining.and(this.meetingPoints[fork.getId()]);
				cur.dependentFork.put(in, (BitSet) remaining.clone());
			}
		}
	}
}
