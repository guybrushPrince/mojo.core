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
package de.jena.uni.mojo.util.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * Store holding references between the workflow graph nodes and the bpmn nodes.
 * 
 * @author Norbert Spiess
 * 
 */
public class ElementStore {

	/**
	 * A counter which counts the number of nodes.
	 */
	private int nodeCounter = 0;

	/**
	 * A list of workflow graph nodes.
	 */
	private final List<WGNode> nodeList = new ArrayList<>();

	/**
	 * Clear the store.
	 */
	public void clear() {
		nodeCounter = 0;
		nodeList.clear();
	}

	/**
	 * Add a single workflow graph node to the node list.
	 * 
	 * @param node
	 *            The workflow graph node to add.
	 */
	public void addToNodeList(final WGNode node) {
		nodeList.add(node);
		sortNodeList();
	}

	/**
	 * Adds a list of workflow graph nodes to the node list.
	 * 
	 * @param elements
	 *            A list of workflow graph nodes to add.
	 */
	public void addToNodeList(final List<WGNode> elements) {
		nodeList.addAll(elements);
		sortNodeList();
	}

	/**
	 * Get the node list.
	 * 
	 * @return A list of workflow graph nodes.
	 */
	public List<WGNode> getNodeList() {
		return nodeList;
	}

	/**
	 * Get a specific node by index.
	 * 
	 * @param idx
	 *            The index of the node.
	 * @return The workflow graph node.
	 */
	public WGNode getNode(final int idx) {
		return nodeList.get(idx);
	}

	/**
	 * Sort the node list by their ids.
	 */
	private void sortNodeList() {
		Collections.sort(nodeList);
	}

	/**
	 * Create a workflow graph node with a specific type.
	 * 
	 * @param type
	 *            The type of the workflow graph node.
	 * @return The created workflow graph node.
	 */
	public WGNode createNode(final Type type) {
		final WGNode node = new WGNode(nodeCounter++, type);
		addToNodeList(node);
		return node;
	}
}
