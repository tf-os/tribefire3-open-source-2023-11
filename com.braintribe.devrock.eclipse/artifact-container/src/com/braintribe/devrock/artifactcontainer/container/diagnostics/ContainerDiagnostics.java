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
package com.braintribe.devrock.artifactcontainer.container.diagnostics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/*
 * 
 * wenn eine dependency den type jar (implizit etwa) hat und das addressierte pom den type pom und es einen jar part hat dann soll das ok sein - so dass velocity funzt.
 * wenn es eine pom dependency ist und ein nicht pom type im pom dann fehler
 * sollte aber letzter Fall ok sein und es existiert ein jar part dann darf er nicht kommen wegen des dep types = pom
 * 
 */

public class ContainerDiagnostics {
	private static String [] jarDeliveringPackages = {"jar", "bundle"};
	
	/**
	 * check's if a jar's missing and if so, whether it's not a solution with 
	 * packaging declared as "pom" or all requesters refer to it as type "pom".
	 * @param solution - the {@link Solution} to test 
	 * @return - true if the needs to be present for this solution, false otherwise
	 */
	/*
	public static boolean isJarMissing(String id, Solution solution) {
		// find the jar .. 
		PartTuple jarPartTuple = PartTupleProcessor.createJarPartTuple();
		for (Part part : solution.getParts()) {
			if (
					(PartTupleProcessor.compare(part.getType(), jarPartTuple)) &&
					(part.getLocation() != null)
				){						
				return false; // jar's present 
			}
		}
				
	
		return relevantForClasspathCheck(id, solution);
		
		
	} 
	*/
	
	/**
	 * checks if the solutions must contribute a jar to the class path 
	 * @param solution - the solution to check 
	 * @return - true if a jar is required, false otherwise 
	 */
	/*
	public static boolean jarRequiredCheckOld( Solution solution) {
		// a) check the packaging : 
		String packaging = solution.getPackaging();
		// null or jar -> required
		if (packaging == null || Arrays.asList(jarDeliveringPackages).contains( packaging)) {				
			// b) in ALL requesters : missing type or type "jar" -> requires a jar			
			Set<Dependency> requesters = solution.getRequestors();
			if (requesters != null) {
				boolean nonPomReferenceFound = false;
				for (Dependency dependency : requesters) {
					String type = dependency.getPackagingType();						
					if (
							type == null ||
							type.equalsIgnoreCase( "jar")
							) {
						nonPomReferenceFound = true;
						break;
					}
				}
				if (nonPomReferenceFound == true) {
					return true;
				}
			}				
		}						
		
		return false;
	}
	*/
	private static boolean isJarReference( Solution solution) {
		Set<Dependency> requesters = solution.getRequestors();
		if (requesters != null) {
			boolean nonPomReferenceFound = false;
			for (Dependency dependency : requesters) {
				String type = dependency.getType();						
				if (
						type == null ||
						type.equalsIgnoreCase( "jar")
						) {
					nonPomReferenceFound = true;
					break;
				}
			}
			if (nonPomReferenceFound == true) {
				return true;
			}
		}				
		return false;
	}
	private static boolean isPurePomReference( Solution solution) {
		Set<Dependency> requesters = solution.getRequestors();
		if (requesters != null) {
			boolean pomReferenceFound = true;
			for (Dependency dependency : requesters) {
				String type = dependency.getType();						
				if (
						type == null ||
						!type.equalsIgnoreCase( "pom")
						) {
					pomReferenceFound = false;
				}
			}
			if (pomReferenceFound == true) {
				return true;
			}
		}				
		return false;
	}
	
	/**
	 * retrieves the first classifier found within the requesters of a solution (aka the dependencies pointing to it)
	 * and issues a log entry with the classifiers contradicting it 
	 * @param solution
	 * @return
	 */
	public static String hasAnyClassifier(Solution solution) {
		Set<Dependency> requesters = solution.getRequestors();
		String classifier = null;
		Set<String> classifiers = new HashSet<String>();
		if (requesters != null) {		
			for (Dependency dependency : requesters) {
				String type = dependency.getClassifier();				
				if (type != null) {
					if (classifier == null) {
						classifier = type;
						classifiers.add( classifier);
					}
					else {
						if (!type.equalsIgnoreCase( classifier)) {
							classifiers.add(type);
						}
					}
				}
				else {
					classifiers.add( "<none>");
				}
			}
		}
		if (classifiers.size() > 1) {
			final String c = classifier;
			String ignored = classifiers.stream().filter( s -> {return !c.equalsIgnoreCase(s);}).collect( Collectors.joining(","));
			String msg = "Solution [" + NameParser.buildName(solution) + "] has multiple requesters with differing classifers, [" + classifier + "] taken, [" + ignored + "] ignored";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
			
		}
		return classifier;
	}
	
	/**
	 * returns true if any of the solutions requesters have a classifier "classes" attached 
	 * @param solution
	 * @return
	 */
	public static boolean hasClassesClassifier( Solution solution) {
		Set<Dependency> requesters = solution.getRequestors();
		if (requesters != null) {		
			for (Dependency dependency : requesters) {
				String type = dependency.getClassifier();						
				if (
						type != null &&
						type.equalsIgnoreCase( "classes")
						) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean relevantForClasspathCheck( ArtifactContainer container, ArtifactContainerUpdateRequestType mode, Solution solution, ContainerClasspathDiagnosticsListener listener) {
		String projectName = container.getProject().getProject().getName();
		String packaging = solution.getPackaging();
		// jar or bundle (mostly)
		if (packaging == null || Arrays.asList(jarDeliveringPackages).contains( packaging)) {
			// pure pom references in all dependencies, yet a non-pom packaging - yell & don't use
			if (isPurePomReference(solution)) {
				String msg = "Solution [" + NameParser.buildName(solution) + "]'s packaging is [" + packaging + "], but used as a pure [pom] reference in [" + projectName + "]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);	
				// don't use : no pom, yet pom references 
				if (listener != null) {
					listener.acknowledgeSolutionJarPackagedAndReferencedAsPom(container, mode, solution);
				}
				return false;
			}
			// use
			return true;
		}
		// pom packaged 
		if (packaging.equalsIgnoreCase("pom")) {
			// used as non-pom dependency, ok, but honk
			if (isJarReference(solution)) {
				String msg = "Solution [" + NameParser.buildName(solution) + "]'s packaging is [pom], but used as a jar reference in [" + projectName + "]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
				ArtifactContainerPlugin.getInstance().log(status);	
				// use : pom packaged, yet at least one jar reference
				if (listener != null) {
					listener.acknowledgeSolutionPomPackagedAndReferencedAsJarSolution(container, mode, solution);
				}
				return true;
			}
		}
		// must check whether a requesting dependency has the classes classifier set..
		if (hasClassesClassifier(solution)) {
			String msg = "Solution [" + NameParser.buildName(solution) + "]'s packaging is neither [jar] nor [bundle], but used via a dependency with classifier [classes] in " + projectName + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
			if (listener != null) {
				listener.acknowledgeSolutionNonJarPackagedAndReferencedAsClassesJarSolution(container, mode, solution);
			}
			return true;
		}
		
		// don't use : valid pom packaged, pure pom reference
		if (listener != null) {
			listener.acknowledgeSolutionPomPackagedAndReferencedAsPom( container, mode, solution);
		}
		return false;
	}
	
	
}
