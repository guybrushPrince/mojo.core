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
package de.jena.uni.mojo.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.interpreter.IdInterpreter;
import de.jena.uni.mojo.reader.Reader;

/**
 * 
 * @author Dipl.-Inf. Thomas Prinz
 *
 */
public interface SourcePlugin {

	public String getName();

	public String getVersion();

	public String getFileExtension();

	public Reader getReader(String processName, File file, AnalysisInformation information, Charset encoding)
			throws IOException;
	
	public Reader getReader(String processName, String stream, AnalysisInformation information, Charset encoding);

	public IdInterpreter getIdInterpreter();
	
}
