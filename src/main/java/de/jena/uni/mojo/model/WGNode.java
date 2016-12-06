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
package de.jena.uni.mojo.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing a workflow graph node with a distinct id and a type of
 * {@link Type}.
 * 
 * @author Norbert Spiess
 * 
 */
public class WGNode implements Comparable<WGNode> {

	/**
	 * Possible node types of the workflow graph nodes.
	 * 
	 * @author Norbert Spiess
	 * 
	 */
	public enum Type {
		START, END, ACTIVITY, FORK, JOIN, SPLIT, MERGE, UNDEFINED, OR_FORK, OR_JOIN;

		/**
		 * Get the respective counter type.
		 * 
		 * @param type
		 *            The type for which the counter type should be determined.
		 * @return The counter type.
		 * @throws IllegalArgumentException
		 *             if there exists no counter element (start, end, activity,
		 *             undefined)
		 */
		public static Type getCounter(final Type type) {
			switch (type) {
			case FORK:
				return JOIN;
			case JOIN:
				return FORK;
			case SPLIT:
				return MERGE;
			case MERGE:
				return SPLIT;
			case OR_FORK:
				return OR_JOIN;
			case OR_JOIN:
				return OR_FORK;
			default:
				throw new IllegalArgumentException("Type: " + type
						+ " has no counterpart.");
			}
		}
	}

	/**
	 * The type of the node (Fork, Join, etc.)
	 */
	private Type type;

	/**
	 * The id of the node (starting from zero)
	 */
	private final int id;

	/**
	 * A list of direct predecessor nodes.
	 */
	private final List<WGNode> predecessors = new ArrayList<WGNode>();

	/**
	 * A bitset of direct predecessor nodes. Is needed for performance issues.
	 */
	private final BitSet predsBitSet = new BitSet();

	/**
	 * A list of direct successor nodes.
	 */
	private final List<WGNode> successors = new ArrayList<WGNode>();

	/**
	 * A bitset of direct successor nodes. Is needed for performance issues.
	 */
	private final BitSet succsBitSet = new BitSet();

	/**
	 * Extra information that can be stored for each node.
	 */
	private Object extraInformation;

	/**
	 * A set that contains all process elements this node belongs to.
	 */
	private final HashSet<Object> processElements = new HashSet<>();

	/**
	 * In this field, we can store some source code or something else.
	 */
	private String code;

	/**
	 * Create a new workflow graph node with a distinct id and type.
	 * 
	 * @param id
	 *            The id of the workflow graph node.
	 * @param type
	 *            The type of the workflow graph node.
	 */
	public WGNode(final int id, final Type type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public String toString() {
		return "N(" + type + "," + id + ")";
	}

	@Override
	public int compareTo(final WGNode arg0) {
		if (id < arg0.id) {
			return -1;
		} else if (id > arg0.id) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Get the distinct id of this node.
	 * 
	 * @return The id of this node.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get all the direct predecessors of this node.
	 * 
	 * @return A list of direct predecessor nodes.
	 */
	public List<WGNode> getPredecessors() {
		return predecessors;
	}

	/**
	 * Get all direct successors of this node.
	 * 
	 * @return A list of all direct successors of this node.
	 */
	public List<WGNode> getSuccessors() {
		return successors;
	}

	/**
	 * Add a direct predecessor to this node.
	 * 
	 * @param node
	 *            The node that should be added as a predecessor.
	 */
	public void addPredecessor(final WGNode node) {
		predecessors.add(node);
		predsBitSet.set(node.id);
	}

	/**
	 * Try to remove a direct predecessor of this node.
	 * 
	 * @param node
	 *            The direct predecessor to remove.
	 * @return true if the list contained the node, false otherwise.
	 */
	public boolean removePredecessor(final WGNode node) {
		predsBitSet.clear(node.id);
		return predecessors.remove(node);
	}

	/**
	 * Add a direct successor to this node.
	 * 
	 * @param node
	 *            The node to add as direct successor.
	 */
	public void addSuccessor(final WGNode node) {
		successors.add(node);
		succsBitSet.set(node.id);
	}

	/**
	 * Try to remove a direct successor of this node.
	 * 
	 * @param node
	 *            The successor node to remove.
	 * @return true if the list contained the node, false otherwise
	 */
	public boolean removeSuccessor(final WGNode node) {
		succsBitSet.clear(node.id);
		return successors.remove(node);
	}

	/**
	 * Get the type of the node.
	 * 
	 * @return The type of the node.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the type of this node.
	 * 
	 * @param type
	 *            The type to set.
	 */
	public void setType(final Type type) {
		this.type = type;
	}

	/**
	 * Add the elements as direct predecessors of this node.
	 * 
	 * @param elements
	 *            New direct predecessors of this node.
	 */
	public void addPredecessors(final List<WGNode> elements) {
		this.predecessors.addAll(elements);
		for (final WGNode wgNode : elements) {
			predsBitSet.set(wgNode.id);
		}
	}

	/**
	 * Add the nodes as successors of this node.
	 * 
	 * @param elements
	 *            New direct successors of this node.
	 */
	public void addSuccessors(final List<WGNode> elements) {
		this.successors.addAll(elements);
		for (final WGNode wgNode : elements) {
			succsBitSet.set(wgNode.id);
		}
	}

	/**
	 * Delete all the direct successors.
	 */
	public void clearSuccessors() {
		successors.clear();
		succsBitSet.clear();
	}

	/**
	 * Delete all the direct predecessors.
	 */
	public void clearPredecessors() {
		predecessors.clear();
		predsBitSet.clear();
	}

	/**
	 * Get the predecessors as a {@link BitSet}.
	 * 
	 * @return A bitset of predecessors.
	 */
	public BitSet getPredecessorsBitSet() {
		return predsBitSet;
	}

	/**
	 * Get the successors as a {@link BitSet}.
	 * 
	 * @return A bitset of successors.
	 */
	public BitSet getSuccessorsBitSet() {
		return succsBitSet;
	}

	/**
	 * Get the field for additional information. Meant for extending plugins to
	 * store temporary data per node.
	 * 
	 * @return The extra information as arbitrary object.
	 */
	public Object getExtraInformation() {
		return extraInformation;
	}

	/**
	 * Set the field for additional information. Meant for extending plugins to
	 * store temporary data per node.
	 * 
	 * @param additionalInformation
	 *            The extra information.
	 */
	public void setExtraInformation(final Object additionalInformation) {
		this.extraInformation = additionalInformation;
	}

	/**
	 * Get the {@link Object}s this node references to.
	 * 
	 * @return A set of object that (may) represents process elements.
	 */
	public Set<Object> getProcessElements() {
		return processElements;
	}

	/**
	 * Add a {@link Object} as a reference to this node.
	 * 
	 * @param element
	 *            The process element which is represented by this node.
	 */
	public void addProcessElement(final Object element) {
		this.processElements.add(element);
	}

	/**
	 * Add multiple {@link Object}s as a reference to this node.
	 * 
	 * @param processElements
	 *            process elements which are represented by this node.
	 */
	public void addProcessElements(final Set<Object> processElements) {
		this.processElements.addAll(processElements);
	}

	/**
	 * Set the attached source code of this element.
	 * 
	 * @param code
	 *            The attached source code.
	 */
	public void setCode(final String code) {
		this.code = code;
	}

	/**
	 * Get the attached source code of this element
	 * 
	 * @return empty string if there is none.
	 */
	public String getCode() {
		return code == null ? "" : code;
	}
}
