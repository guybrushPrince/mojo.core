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
package de.jena.uni.mojo.processes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.jena.uni.mojo.Mojo;

/**
 * The file handler handles the searching for files in the specified path.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class FileHandler {

	/**
	 * A list of files that where found during the search algorithm.
	 */
	private final List<File> files = new ArrayList<File>();

	/**
	 * A list of file names that should be searched.
	 */
	private final List<String> fileNames = new ArrayList<String>();

	/**
	 * Reads in all files in the given path.
	 */
	public void readFiles() {
		String path = Mojo.getCommand("PATH").asStringValue();
		// Try to read the files
		if (!fileNames.isEmpty()) {
			for (String file : fileNames) {
				File f = new File(file);

				if (!f.exists()) {
					f = new File(path + File.separatorChar + file);
				}
				if (f.exists()) {
					files.add(f);
				} else {
					System.err.println("Cannot find file: "
							+ f.getAbsolutePath());
				}
			}
		} else {
			if (!path.isEmpty()) {
				files.addAll(findFiles(new File(path)));
			}
		}
	}

	/**
	 * Find all files with appendix .bpmn.xml or .pnml in the given directory.
	 * 
	 * @param directory
	 *            The directory where starting the search.
	 * @return A list of files.
	 */
	private List<File> findFiles(File directory) {
		List<File> files = new ArrayList<File>();
		if (directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.isDirectory()) {
					files.addAll(findFiles(f));
				} else {
					for (String extension: Mojo.availableFileExtensions) {
						if (f.getAbsolutePath().endsWith("." + extension)) {
							files.add(f);
						}
					}					
				}
			}
		}
		return files;
	}

	/**
	 * Get the files list.
	 * 
	 * @return The file list.
	 */
	public List<File> getFiles() {
		return files;
	}

	/**
	 * Add a file name which should be searched.
	 * 
	 * @param fileName
	 *            The file name
	 */
	public void addFileName(String fileName) {
		fileNames.add(fileName);
	}
}
