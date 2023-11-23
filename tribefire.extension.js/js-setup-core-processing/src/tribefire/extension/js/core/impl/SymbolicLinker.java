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
package tribefire.extension.js.core.impl;



import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.utils.FileTools;


public class SymbolicLinker {		
	public static void createSymbolicLink( File source, File target, boolean relativize) throws IOException {		
		Path sourcePath = source.getAbsoluteFile().toPath();
		Path targetPath = target.getAbsoluteFile().toPath();
				
		Path targetPathToUse = relativize ? sourcePath.getParent().relativize( targetPath) : targetPath;
		Files.createSymbolicLink(sourcePath, targetPathToUse);				
	}
	
	public static void ensureSymbolicLink( File source, File target, boolean relativize) {		
		Path sourcePath = source.getAbsoluteFile().toPath();
		Path targetPath = target.getAbsoluteFile().toPath();
		
		Path targetPathToUse = relativize ? sourcePath.getParent().relativize( targetPath) : targetPath;
		
		try {
			
			if (Files.exists(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
				if (Files.isSymbolicLink(sourcePath) && Files.readSymbolicLink(sourcePath).equals(targetPathToUse)) 
					return;
					
				FileTools.deleteRecursivelySymbolLinkAware(source);
			}
			
			Files.createSymbolicLink(sourcePath, targetPathToUse);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not ensure symbolic link from [" + sourcePath + "] to [" + targetPathToUse + "]. Check user rights!", e);
		}				
	}

	public static void createCopy(File source, File target) {
		FileTools.copyFileOrDirectory(source, target);		
	}
	
	/**
	 * determines all link names that should be create for the passed solution.
	 * if mulitple requesters are found, all differing names are returned -> as they all need to get a link
	 * @param solution
	 * @return
	 */
	public static List<String> determineLinkName(Solution solution) {
		Set<Dependency> requestors = solution.getRequestors();
		if (requestors == null || requestors.isEmpty()) {
			return Collections.singletonList(solution.getGroupId() + "." + solution.getArtifactId() + "-" + VersionProcessor.toString(solution.getVersion())); 
		}
		else {	
			List<String> names = new ArrayList<>();
			for (Dependency dependency : requestors) {						
				String name = determineLinkName(solution, dependency);
				if (!names.contains(name)) {
					names.add(name);
				}
			}
			return names;
		}		
	}

	/**
	 * generate a link name as defined by the dependency passed
	 * @param solution - the {@link Solution}
	 * @param dependency - the dependency 
	 * @return - the name
	 */
	private static String determineLinkName(Solution solution, Dependency dependency) {		
		VersionRange range = dependency.getVersionRange();
		String versionExpression;
		if (!range.getInterval()) {
			versionExpression = VersionProcessor.toString(solution.getVersion());
		}
		else if (range.getOpenLower() || !range.getOpenUpper()) {
			versionExpression =  VersionRangeProcessor.toString(range);
		}
		else {
			Version lower = range.getMinimum();
			VersionMetricTuple lowerMetric = VersionProcessor.getVersionMetric(lower);
			
			Version upper = range.getMaximum();
			VersionMetricTuple upperMetric = VersionProcessor.getVersionMetric( upper);
											
			if (
					upperMetric.major == (lowerMetric.major + 1) && 
					lowerMetric.minor == 0 && 
					upperMetric.minor == 0
				) {					
					versionExpression =  lowerMetric.major + "~";
			}					
			else if (
					upperMetric.major.compareTo(lowerMetric.major) == 0 &&
					upperMetric.minor == (lowerMetric.minor + 1)
					) { 					
				versionExpression = "" + lowerMetric.major + "." + lowerMetric.minor + "~";
			}
			else {
				versionExpression = VersionRangeProcessor.toString(range);
			}								
		}
		String name = solution.getGroupId() + "." + solution.getArtifactId() + "-" + versionExpression;
		return name;
	}
	
	public static void main(String[] args) {
		for (String arg : args) {
			String [] vs = arg.split("@");
			Dependency dependency = NameParser.parseCondensedDependencyName(vs[0]);
			Solution solution = NameParser.parseCondensedSolutionName( vs[1]);
			solution.getRequestors().add(dependency);
			List<String> linkNames = determineLinkName(solution);
			for (String linkName : linkNames) {			
				System.out.println( vs[0] + " -> " + vs[1] + " : " + linkName);
			}
		}
	}
	
	
}
