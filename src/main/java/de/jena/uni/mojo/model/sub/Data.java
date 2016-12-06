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
package de.jena.uni.mojo.model.sub;

import java.util.BitSet;

/**
 * This class has stored most information about the analysis. Since we now have
 * build our analysis on the edges instead of the nodes, this class contains
 * less information.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Data {

	/**
	 * An enumeration of the working state of the node where this data belongs
	 * to.
	 * 
	 * @author Dipl.-Inf. Thomas M. Prinz
	 * 
	 */
	public enum WorkingState {
		UNWORKED, UNFINISHED, FINISHED
	}

	/**
	 * A final bit set which defines the waiting area of a corresponding node.
	 */
	public final BitSet waitingArea;

	/**
	 * The constructor needs the number of nodes of the workflow graph.
	 * 
	 * @param nodes
	 *            The number of nodes of the workflow graph.
	 */
	public Data(int nodes) {
		waitingArea = new BitSet(nodes);
	}
}
