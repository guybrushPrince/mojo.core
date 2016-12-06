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
import de.jena.uni.mojo.analysis.CoreAnalysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.error.marker.Marker;
import de.jena.uni.mojo.interpreter.AbstractEdge;
import de.jena.uni.mojo.interpreter.IdInterpreter;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * Defines an annotation for the workflow graph, i.e., of an info, a warning or
 * an error information (not a Java annotation).
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class Annotation {

	/**
	 * The name of the opening nodes attribute.
	 */
	public final static String OPENING_NODES = "OpeningNodes";

	/**
	 * The name of the printable nodes attribute.
	 */
	public final static String PRINTABLE_NODES = "PrintableNodes";

	/**
	 * The name of the involved nodes attribute.
	 */
	public final static String INVOLVED_NODES = "InvolvedNodes";

	/**
	 * The category of annotation (of {@link EAlarmCategory}).
	 */
	private final EAlarmCategory category;

	/**
	 * The nodes which should be visualized.
	 */
	private final Set<WGNode> printableNodes = new HashSet<WGNode>();

	/**
	 * The nodes which open the failure (e.g. a fork for an abundance)
	 */
	private final Set<WGNode> openingNodes = new HashSet<WGNode>();

	/**
	 * The involved nodes.
	 */
	private final Set<WGNode> involvedNodes = new HashSet<WGNode>();

	/**
	 * A textual description of the annotation.
	 */
	private final String description;

	/**
	 * The analysis which has caused the annotation.
	 */
	private final CoreAnalysis analysis;

	public Annotation(EAlarmCategory category, String description,
			CoreAnalysis analysis) {
		this.category = category;
		this.description = description;
		this.analysis = analysis;
	}

	/**
	 * Get a list of the printable nodes.
	 * 
	 * @return The printable nodes.
	 */
	public List<WGNode> getPrintableNodes() {
		return new ArrayList<WGNode>(this.printableNodes);
	}

	/**
	 * Add a collection of printable nodes.
	 * 
	 * @param nodes
	 *            The nodes to add.
	 */
	public void addPrintableNodes(Collection<? extends WGNode> nodes) {
		this.printableNodes.addAll(nodes);
		this.involvedNodes.addAll(nodes);
	}

	/**
	 * Add a printable node.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addPrintableNode(WGNode node) {
		this.printableNodes.add(node);
		this.involvedNodes.add(node);
	}

	/**
	 * Get a list of the opening nodes.
	 * 
	 * @return The opening nodes.
	 */
	public List<WGNode> getOpeningNodes() {
		return new ArrayList<WGNode>(this.openingNodes);
	}

	/**
	 * Add a collection of opening nodes.
	 * 
	 * @param nodes
	 *            The nodes to add.
	 */
	public void addOpeningNodes(Collection<? extends WGNode> nodes) {
		this.openingNodes.addAll(nodes);
		this.involvedNodes.addAll(nodes);
	}

	/**
	 * Add a opening node.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addOpeningNode(WGNode node) {
		this.openingNodes.add(node);
		this.involvedNodes.add(node);
	}

	/**
	 * Get a list of the involved nodes.
	 * 
	 * @return The involved nodes.
	 */
	public List<WGNode> getInvolvedNodes() {
		return new ArrayList<WGNode>(this.involvedNodes);
	}

	/**
	 * Add a collection of involved nodes.
	 * 
	 * @param nodes
	 *            The nodes to add.
	 */
	public void addInvolvedNodes(Collection<? extends WGNode> nodes) {
		this.involvedNodes.addAll(nodes);
	}

	/**
	 * Add an involved node.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addInvolvedNode(WGNode node) {
		this.involvedNodes.add(node);
	}

	/**
	 * Get the alarm category.
	 * 
	 * @return The alarm category of this annotation.
	 */
	public EAlarmCategory getAlarmCategory() {
		return this.category;
	}

	/**
	 * Get the description of this annotation.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Get the analysis which had defined this annotation.
	 * 
	 * @return The analysis.
	 */
	public CoreAnalysis getAnalysis() {
		return this.analysis;
	}

	/**
	 * This method should be overriden by an implementing class. However, this
	 * class provides a default implementation. The method prints the
	 * information about the annotation.
	 * 
	 * @param interpreter The interpreter which interprets the process nodes.
	 * 
	 * @return The information about the annotation.
	 */
	public void printInformation(IdInterpreter interpreter) {
		System.out.printf("\t%-20s%n", category.name() + ":");
		System.out.printf("\t\t%s%n", "Description: " + description);
		System.out.printf("\t\t%-35s: %s%n", "Opening nodes (WFG)",
				openingNodes.toString());
		System.out.printf("\t\t%-35s: %s%n", "Annotated nodes (WFG)",
				printableNodes.toString());
		System.out.printf("\t\t%-35s: %s%n", "Involved nodes (WFG)",
				involvedNodes.toString());

		System.out.printf("\t\t%-35s: %s%n", "Opening nodes (Process)",
				getInterpretedOpeningNodes().toString());
		System.out.printf("\t\t%-35s: %s%n", "Annotated nodes (Process)",
				getInterpretedPrintableNodes().toString());
		System.out.printf("\t\t%-35s: %s%n", "Involved nodes (Process)",
				getInterpretedInvolvedNodes().toString());

	}

	/**
	 * Returns a list of the printable nodes. However, instead of taking the
	 * workflow graph nodes, it uses the elements which are annotated to the
	 * nodes (e.g. BPMN elements).
	 * 
	 * @return A list of elements of the origin process.
	 */
	public List<Object> getInterpretedPrintableNodes() {
		return extractOriginalNodes(printableNodes);
	}

	/**
	 * Returns a list of the opening nodes. However, instead of taking the
	 * workflow graph nodes, it uses the elements which are annotated to the
	 * nodes (e.g. BPMN elements).
	 * 
	 * @return A list of elements of the origin process.
	 */
	public List<Object> getInterpretedOpeningNodes() {
		return extractOriginalNodes(openingNodes);
	}

	/**
	 * Returns a list of the involved nodes. However, instead of taking the
	 * workflow graph nodes, it uses the elements which are annotated to the
	 * nodes (e.g. BPMN elements).
	 * 
	 * @return A list of elements of the origin process.
	 */
	public List<Object> getInterpretedInvolvedNodes() {
		return extractOriginalNodes(involvedNodes);
	}

	/**
	 * Interprets the annotation and returns a marker that could be used, for
	 * example, to decorate a process with failure annotations.
	 * 
	 * @param interpreter
	 *            An id interpreter.
	 * @return A marker of the annotation.
	 */
	public Marker interpret(IdInterpreter interpreter) {
		// Create a new marker
		Marker marker = new Marker(this.description);

		// Create information about printable nodes
		marker.addProcessAnnotation(PRINTABLE_NODES,
				getIdString(getInterpretedPrintableNodes(), interpreter), 0);

		// Create information about opening nodes
		marker.addProcessAnnotation(OPENING_NODES,
				getIdString(getInterpretedOpeningNodes(), interpreter), 1);

		// Create information about involved nodes
		marker.addProcessAnnotation(INVOLVED_NODES,
				getIdString(getInterpretedInvolvedNodes(), interpreter), 2);

		return marker;
	}	

	/**
	 * Extracts the workflow graph nodes from a given bit set into a list of
	 * workflow graph nodes.
	 * 
	 * @param set
	 *            The node ids within a bit set.
	 * @return A list of extracted elements.
	 */
	protected List<WGNode> extractWorkflowNodes(BitSet set) {
		WGNode[] map = ((Analysis) this.getAnalysis()).getNodeMap();

		ArrayList<WGNode> extracted = new ArrayList<WGNode>();

		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
			extracted.add(map[i]);
		}
		return extracted;
	}

	/**
	 * Extracts the original process elements from a given node collection to a
	 * new list.
	 * 
	 * @param nodes
	 *            The nodes which should be extracted.
	 * @return A list of extracted elements.
	 */
	protected List<Object> extractOriginalNodes(
			Collection<? extends WGNode> nodes) {
		ArrayList<Object> interpreted = new ArrayList<Object>();
		for (WGNode node : nodes) {
			interpreted.addAll(node.getProcessElements());
		}
		return interpreted;
	}

	/**
	 * Extracts abstract edges with original process nodes from a given
	 * path of workflow graph edges.
	 * 
	 * @param edges
	 *            The edges which should be extracted.
	 * @return A list of extracted abstract edges.
	 */
	protected List<AbstractEdge> extractAbstractPath(
			List<? extends Edge> edges) {

		ArrayList<AbstractEdge> interpreted = new ArrayList<AbstractEdge>();
		
		for (Edge edge: edges) {
			// Get the src and the target
			WGNode src = edge.src;
			WGNode tgt = edge.tgt;
			
			for (Object source: src.getProcessElements()) {
				for (Object target: tgt.getProcessElements()) {
					interpreted.add(new AbstractEdge(source, target));
				}
			}
		}
		
		return interpreted;
	}
	
	/**
	 * Extracts an edge path from a bit set.
	 * @param path The path as bit set.
	 * @return The path as edge list.
	 */
	protected List<Edge> extractEdgePath(BitSet path) {
		// Get the workflow graph
		WorkflowGraph graph = ((Analysis) analysis).getWorkflowGraph();
		
		// Get the edges
		List<Edge> edges = graph.getEdges();
		
		ArrayList<Edge> edgePath = new ArrayList<Edge>();
		
		for (int p = path.nextSetBit(0); p >= 0; p = path.nextSetBit(p + 1)) {
			edgePath.add(edges.get(p));
		}
		
		return edgePath;
	}

	/**
	 * Extracts an id string of a set of abstract nodes. How the id could be
	 * extracted is defined by an id interpreter.
	 * 
	 * @param nodes
	 *            The collection of nodes.
	 * @param interpreter
	 *            The id interpreter.
	 * @return A string containing comma-separated ids
	 */
	protected String getIdString(final Collection<Object> nodes,
			final IdInterpreter interpreter) {
		final StringBuilder idString = new StringBuilder();

		// Extract the ids and append them in a string.
		for (Object node : nodes) {
			idString.append(interpreter.extractId(node));
			idString.append(",");
		}

		// Remove the last comma
		String idStr = idString.toString();
		if (idStr.endsWith(",")) {
			idStr = idStr.substring(0, idStr.length() - 1);
		}

		return idStr;
	}

}