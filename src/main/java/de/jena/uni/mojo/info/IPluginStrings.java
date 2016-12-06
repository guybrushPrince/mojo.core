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
package de.jena.uni.mojo.info;

/**
 * Interface holding all strings.
 * 
 * @author Norbert Spiess
 * 
 */
public interface IPluginStrings {
	/**
	 * "Can't find a shape for the target node of the sequence "
	 */
	String LOGGER_INFO_NO_TARGET_SHAPE_FOR_SEQUENCE = "Can't find a shape for the target node of the sequence ";

	/**
	 * "Can't find a shape for the source node of the sequence "
	 */
	String LOGGER_INFO_NO_SOURCE_SHAPE_FOR_SEQUENCE = "Can't find a shape for the source node of the sequence ";

	/**
	 * Gateway has only one in and outgoing edge.
	 */
	String GATEWAY_AS_ACTIVITY = "Gateway has only one in and outgoing edge. Handled as Task.";

	/**
	 * Element type not supported. Incoming and outgoing edges are ignored.
	 */
	String NOT_SUPPORTED = "Element type not supported. Will be seen as task.";

	/**
	 * Business process is not fully connected.
	 */
	String PROCESS_NOT_CONNECTED = "Business process is not fully connected.";

	/**
	 * Element is connected to itself.
	 */
	String DIRECT_LOOP = "Element is connected to itself.";

	/**
	 * Start element has at least one incoming edge.
	 */
	String START_WITH_PREDECESSOR = "Start element has at least one incoming edge. Handled as Task.";

	/**
	 * No start detectable. The graph contains a loop that supresses the
	 * detection.
	 */
	String NO_START = "No start detectable. The graph contains a loop that supresses the detection.";

	/**
	 * No start detectable. The graph contains a loop that supresses the
	 * detection.
	 */
	String NO_END = "No end detectable. The graph contains a loop that suppresses the detection.";

	/**
	 * Unexpected type
	 */
	String UNEXPECTED_TYPE = "Unexpected type ";
}
