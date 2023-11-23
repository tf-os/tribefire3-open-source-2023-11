// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.console.output;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.white;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ConsoleOutputFiles {

	public static final String INDENT = "   ";
	public static final String NEW_LINE = "\n";

	protected ConsoleOutputFiles() {
		// noop
	}

	/**
	 * Fold Paths that are found in foldPath. This means, do not follow them deeper but shortcut them with "..."
	 * 
	 */
	public static void outputProjectionDirectoryTree(Path dir, List<Path> foldPath) {
		println(yellow(indentText(directoryTree(dir, true, foldPath), 1)));
		println(text(""));
	}

	public static void outputProjectionDirectoryTree(Path dir) {
		println(yellow(indentText(directoryTree(dir, true, new ArrayList<>()), 1)));
		println(text(""));
	}

	public static ConsoleOutput artifactNameOutput(String artifactName, int indentCount) {
		String[] artifactNameParts = artifactName.split(":|#");
		return artifactNameOutput(artifactNameParts[0], artifactNameParts[1], artifactNameParts[2], indentCount);
	}

	public static ConsoleOutput artifactNameOutput(String groupId, String artifactId, String version, int indentCount) {
		return sequence(text(indentText("", indentCount)), brightBlack(groupId), brightBlack(":"), white(artifactId), brightBlack("#"),
				version.contains("-pc") ? yellow(version) : green(version));
	}

	private static String indentText(String text, int indentCount) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentCount; i++) {
			sb.append(INDENT);
		}
		if (text.isEmpty()) {
			return sb.toString();
		}
		return text.replaceAll("(?m)^", sb.toString());
	}

	private static String directoryTree(Path dir, boolean skipRoot, List<Path> foldPaths) {
		StringBuilder output = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		appendDirectory(dir, indent, "───", output, skipRoot, foldPaths);
		return output.toString();
	}

	private static void appendDirectory(Path dir, StringBuilder indent, String prefix, StringBuilder output, boolean skipRoot, List<Path> foldPaths) {
		output.append(NEW_LINE);
		output.append(indent);
		if (!skipRoot) {
			output.append(prefix);
			output.append(dir.getFileName().toString());
			output.append("/");
		}
		if (prefix.equals("├──")) {
			indent.append("│").append(INDENT);
		} else {
			indent.append(INDENT);
		}

		File[] filesArray = dir.toFile().listFiles();
		if (filesArray == null || filesArray.length == 0) {
			return;
		}
		List<File> files = Arrays.asList(filesArray);

		for (Path fold : foldPaths) {
			if (dir.compareTo(fold) == 0) {
				appendFile(Paths.get("..."), new StringBuilder(indent), "└──", output);
				return; // stop here
			}
		}

		sortFilesInNaturalOrder(files);
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).isDirectory()) {
				if (i != files.size() - 1) {
					appendDirectory(files.get(i).toPath(), new StringBuilder(indent), "├──", output, false, foldPaths);
				} else {
					appendDirectory(files.get(i).toPath(), new StringBuilder(indent), "└──", output, false, foldPaths);
				}
			} else {
				if (i != files.size() - 1) {
					appendFile(files.get(i).toPath(), new StringBuilder(indent), "├──", output);
				} else {
					appendFile(files.get(i).toPath(), new StringBuilder(indent), "└──", output);
				}
			}
		}
	}

	private static void sortFilesInNaturalOrder(List<File> files) {
		files.sort(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && !o2.isDirectory()) {
					return -1;
				} else if (!o1.isDirectory() && o2.isDirectory()) {
					return 1;
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			}

		});
	}

	private static void appendFile(Path file, StringBuilder indent, String prefix, StringBuilder output) {
		output.append(NEW_LINE);
		output.append(indent);
		output.append(prefix);
		output.append(file.getFileName().toString());
	}

}
