/**
 * Copyright 2013 mojo Friedrich Schiller University Jena
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
package de.jena.uni.mojo.util.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.List;


import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * Class for exporting the graphs to the hard disk.
 * 
 * @author Norbert Spiess
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class WorkflowGraphExporter {

	/**
	 * Export the given graphs to the dot format used by Grahpviz to the given
	 * path.
	 * 
	 * @param graphs
	 *            A list of workflow graphs.
	 * @param path
	 *            The path to export.
	 * @param name
	 *            The name of the file
	 */
	public static void exportToDot(final List<WorkflowGraph> graphs,
			final String path, final String name) {

		int i = 0;
		for (final WorkflowGraph graph : graphs) {
			try {
				final File file = new File(path + name + "_" + (i++) + ".dot");
				if (file.exists()) {
					file.delete();
					file.createNewFile();
				}
				final PrintWriter writer = new PrintWriter(file);

				writer.println(printGraphDot(graph));

				writer.close();

				System.out.println("Exported graph to "
						+ file.getAbsolutePath());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a string representation of the graph nodes and their edges
	 * 
	 * @param wgGraph
	 *            The workflow graph.
	 * @return A string representation of the graph.
	 */
	private static String printGraphDot(final WorkflowGraph wgGraph) {
		final StringBuilder graph = new StringBuilder();
		graph.append("digraph EasyGraph {\n");

		// Print all nodes
		final WGNode start = wgGraph.getStart();
		graph.append(nodeToGraphDot(wgGraph, start));
		for (final WGNode node : wgGraph.getNodeList()) {
			graph.append(nodeToGraphDot(wgGraph, node));
		}
		graph.append(nodeToGraphDot(wgGraph, wgGraph.getEnd()));
		graph.append("}");

		return graph.toString();
	}

	/**
	 * Handle a single node and its outgoing edges
	 * 
	 * @return A string representation of a node and of its edges.
	 */
	public static String nodeToGraphDot(final WorkflowGraph graph,
			final WGNode node) {
		final StringBuilder builder = new StringBuilder();

		builder.append(node.getId() + "[label=\"" + node.getId() + "\\n"
				+ node.getType());

		builder.append("\", shape=circle];\n");

		if (graph.isClosed()) {
			BitSet outgoing = graph.getOutgoingEdges()[node.getId()];
			for (int o = outgoing.nextSetBit(0); o >= 0; o = outgoing
					.nextSetBit(o + 1)) {
				Edge out = graph.getEdges().get(o);
				builder.append(node.getId() + "->" + out.tgt.getId()
						+ "[label=\"" + o + "\"];\n");
			}
		} else {
			for (final WGNode suc : node.getSuccessors()) {
				builder.append(node.getId() + "->" + suc.getId()
						+ "[label=\"\"];\n");
			}
		}

		return builder.toString();
	}
}
