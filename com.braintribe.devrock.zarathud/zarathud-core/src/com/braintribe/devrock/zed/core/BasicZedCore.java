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
package com.braintribe.devrock.zed.core;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.zed.analyze.BasicZedArtifactAnalyzer;
import com.braintribe.devrock.zed.api.ZedCore;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.core.ResourceScanner;
import com.braintribe.devrock.zed.api.core.ZedArtifactAnalyzer;
import com.braintribe.devrock.zed.commons.ConversionHelper;
import com.braintribe.devrock.zed.commons.UrlWrapperCodec;
import com.braintribe.devrock.zed.commons.ZedException;
import com.braintribe.devrock.zed.context.BasicZedAnalyzerContext;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.scan.BasicResourceScanner;
import com.braintribe.devrock.zed.scan.ScannerResult;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAnnotationsNature;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * the {@link BasicZedCore}: analyzes the jar reflecting the passed {@link BindingArtifact}<br/>
 * <br/>
 * <b>returns</b> an {@link Artifact} that contains information about:<br/>
 * all enum as {@link EnumEntity}<br/>
 * all classes as {@link ClassEntity}<br/>
 * all interfaces as {@link InterfaceEntity}<br/>  
 * <br/>
 * <b>requires</b>:<br/>
 * an instance of the {@link BasicResourceScanner}<br/>
 * an instance of the {@link BasicAnalyzer}<br/>
 * a {@link Collection} of {@link Solution} including the one that is reflecting the {@link BindingArtifact}<br/>  
 * 
 * @author pit
 *
 */
public class BasicZedCore implements ZedCore {
	private static Logger log = Logger.getLogger(BasicZedCore.class);
	private static final String ANNO_SUPRESS_ISSUES = "com.braintribe.common.annotations.SuppressCodeAnalysisIssues";
	private static final String ANNO_SUPRESS_ISSUE = "com.braintribe.common.annotations.SuppressCodeAnalysisIssue";
	
	private Collection<AnalysisArtifact> classpathSolutions;
	private Collection<String> classesDirectories;
	private Map<String, AnalysisArtifact> artifactsRepresentedByClasses; 
	
	private List<AnalysisDependency> declaredTerminalDependencies;
	
	private ResourceScanner resourceScanner = new BasicResourceScanner();
	private ZedArtifactAnalyzer artifactScanner = new BasicZedArtifactAnalyzer();

	private Map<URL, Artifact> urlToBindingArtifactMap = CodingMap.createHashMapBased(new UrlWrapperCodec());	
	private BasicPersistenceGmSession session;
	
	
	private File javaRuntimeLibrary;

	private BasicZedAnalyzerContext context;	
	
	@Required @Configurable
	public void setClasspath(Collection<AnalysisArtifact> solutions) {
		this.classpathSolutions = solutions;
	}
	
	@Configurable
	public void setClassesDirectories(Collection<String> classesDirectories) {
		this.classesDirectories = classesDirectories;
	}
	
	@Configurable
	public void setArtifactsRepresentedByClasses(Map<String, AnalysisArtifact> artifactsRepresentedByClasses) {
		this.artifactsRepresentedByClasses = artifactsRepresentedByClasses;
	}
	
	@Required @Configurable
	public void setDeclared(List<AnalysisDependency> solutions) {
		this.declaredTerminalDependencies = solutions;
	}
	
	@Configurable
	public void setResourceScanner(ResourceScanner resourceScanner) {
		this.resourceScanner = resourceScanner;
	}
	@Configurable
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	
	
	@Configurable
	public void setJavaRuntimeLibrary(File javaRuntimeLibrary) {
		this.javaRuntimeLibrary = javaRuntimeLibrary;
	}
	@Configurable
	public void setArtifactScanner(ZedArtifactAnalyzer artifactScanner) {
		this.artifactScanner = artifactScanner;
	}
	
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.api.ZedCore#getAnalyzingContext()
	 */
	public ZedAnalyzerContext getAnalyzingContext() {
		return context;
	}
		
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.api.ZedCore#analyseJar(com.braintribe.model.zarathud.data.Artifact)
	 */
	public Artifact analyseJar(Artifact terminalArtifact) throws ZedException {
		context = new BasicZedAnalyzerContext();
		// add classpath 
		context.setClasspath(classpathSolutions);
		// add session
		if (session != null) {
			context.setSession(session);			
		}
				
		
		//
		// PREPROCESSING
		// 
				
		AnalysisArtifact solutionToScan = null;
		Resource jarToScan = null;
		
		// if a directory for classes is been passed, include it into the URL list  
		if (classesDirectories != null && classesDirectories.size() > 0) {
			for (String classesDirectory : classesDirectories) {
				File classesDirectoryFile = new File( classesDirectory);
				try {
					URL buildDirectoryUrl = classesDirectoryFile.toURI().toURL();				
					urlToBindingArtifactMap.put( buildDirectoryUrl, terminalArtifact);				
				} catch (MalformedURLException e) {
					String msg= String.format("cannot build URL from file [%s]", classesDirectoryFile);
					log.warn( msg, e);
				}			
			}
		}
		
		// extract the jars from the solutions and add them to the URL list 
		for (AnalysisArtifact solution : classpathSolutions) {
			
			// note the main jar - if any 
			if (
					(classesDirectories == null || classesDirectories.size() == 0) &&
					solution.getGroupId().equalsIgnoreCase( terminalArtifact.getGroupId()) &&
					solution.getArtifactId().equalsIgnoreCase( terminalArtifact.getArtifactId()) && 
					solution.getVersion().equalsIgnoreCase( terminalArtifact.getVersion())
				)  {
				solutionToScan = solution;
				jarToScan = extractJarFromSolution(solution);					
			}
			 
			Resource jar = extractJarFromSolution(solution);
			
			if (jar == null) {
				String msg="no jar found for artifact [" + terminalArtifact.toStringRepresentation() + "]";
				log.warn( msg);
				return null;
			}
			try {	
				File jarFile = resourceToFile( jar);				
				URL url = jarFile.toURI().toURL();			
				urlToBindingArtifactMap.put(url, ConversionHelper.toArtifact(context, solution));
			} catch (MalformedURLException e) {
				String msg="cannot build URL from file [" + jar + "]";
				log.warn( msg, e);
			}
		}
		
		try {
			if (javaRuntimeLibrary != null) {
				URL url = javaRuntimeLibrary.toURI().toURL();			
				urlToBindingArtifactMap.put(url, context.artifacts().runtimeArtifact(context));
			}
		} catch (MalformedURLException e) {
			String msg="cannot insert java runtime URL from file [" + javaRuntimeLibrary.getAbsolutePath() + "]";
			log.warn( msg, e);
		}
		 
		// additional stuff - some projects are not represented by jars - as in the passed classpath
		if (artifactsRepresentedByClasses != null && !artifactsRepresentedByClasses.isEmpty()) {
			List<AnalysisArtifact> artifactsToBeAddedToDependencies = new ArrayList<>( artifactsRepresentedByClasses.size());
			
			for (Map.Entry<String, AnalysisArtifact> entry : artifactsRepresentedByClasses.entrySet()) {
				String classesDirectoryName = entry.getKey();
				File classesDirectory = new File( classesDirectoryName);
				AnalysisArtifact solution = entry.getValue();
				// 
				artifactsToBeAddedToDependencies.add( solution);
				
				if (!classesDirectory.exists()) {
					String msg="Passed directory for [" + solution.asString() + "] doesn't exist:" + classesDirectoryName;
					log.warn( msg);
					continue;
				}
				try {
					URL buildDirectoryUrl = classesDirectory.toURI().toURL();				
					urlToBindingArtifactMap.put( buildDirectoryUrl, ConversionHelper.toArtifact(context, solution));
				} catch (MalformedURLException e) {
					String msg="cannot build URL from class folder of [" + solution.asString() + "] : " + classesDirectoryName;
					log.warn( msg, e);
				}	
			}
			context.setAdditionsToClasspath( artifactsToBeAddedToDependencies);
		}
		
		//
		// SCAN 
		//
		
		// scan terminal or classes directory  
		ScannerResult result = null;		
		if (classesDirectories == null || classesDirectories.size() == 0) {
			// no classes directory -> the terminal must be part of the solution list 
			if ( solutionToScan == null) {
				String msg="cannot find corresponding jar for terminal [" + terminalArtifact.getGroupId() + ":" + terminalArtifact.getArtifactId() + "#" + terminalArtifact.getVersion();
				log.error( msg);
				throw new ZedException(msg);			
			}
			// scan the jar
			File fileToScan = resourceToFile(jarToScan);
			try {
				result = resourceScanner.scanJar( fileToScan);				
			} catch (IOException e) {
				String msg="cannot scan jar [" + fileToScan.getAbsolutePath() + "]";
				log.error( msg, e);
				
				throw new ZedException(msg, e);
			} 	
		}
		else {			
			// scan the directory
			for (String classesDirectory : classesDirectories) {
				try {				
					File buildDirectoryFile = new File( classesDirectory);				
					ScannerResult singleResult = resourceScanner.scanFolder( buildDirectoryFile);
					if (result == null) {
						result = singleResult;
					}
					else {
						result.merge(singleResult);
					}					
				} catch (Exception e) {
					String msg= String.format("cannot scan folder [%s]", classesDirectory);
					log.error( msg, e);
					throw new ZedException(msg, e);
				}			
			}
		}
		
		
		//
		// run the analysis 
		// 
		if (javaRuntimeLibrary != null) {
			try {
				context.setRuntimeJar( javaRuntimeLibrary.toURI().toURL());
			} catch (MalformedURLException e) {
				String msg="cannot extract file URL from passed java runtime library file [" + javaRuntimeLibrary.getAbsolutePath() + "]";
				log.error( msg, e);
				throw new ZedException(msg, e);
			}
		}
		context.setTerminalArtifact( terminalArtifact);		
		context.setTerminalScanData( result);		
		context.setDeclaredTerminalDependencies(declaredTerminalDependencies);
		
		

		context.setUrlToArtifactMap(urlToBindingArtifactMap);
		URLClassLoader urlClassLoader = new URLClassLoader(urlToBindingArtifactMap.keySet().toArray( new URL[0]), getExtensionClassLoader());
		context.setClassLoader( urlClassLoader);				
		
				
		Artifact artifact;
		try {
			artifact = artifactScanner.analyzeArtifact(context, result.getClasses());
		} catch (UnsupportedRequestTypeException e) {
			String msg="cannot analyze input classes";
			log.error( msg, e);
			throw new ZedException(msg, e);
		}
		
		return artifact;
	}
		
	
	private File resourceToFile(Resource jar) {
		if (jar instanceof FileResource) {
			FileResource fileResource = (FileResource) jar;
			File file = new File( fileResource.getPath());
			return file;
		}
		else {
			log.error( "currently only Resources of type FileResource are supported, not :" + jar.entityType().getTypeName());
		}
		return null;
	}

	/**
	 * @return - the current extension classloader, ie. the first parent of the system one. 
	 */
	private ClassLoader getExtensionClassLoader() {
		 ClassLoader cl =new Object(){}.getClass().getEnclosingClass().getClassLoader();
         ClassLoader prev = null;
         while(cl!=null){
             prev=cl;
             cl=cl.getParent();
         }
         return prev;
	}
	
	/**
	 * extract the relevant jar from the solution 
	 * @param solution - the {@link Solution} to get the jar as file from
	 * @return - the file that points to the jar 
	 */
	private Resource extractJarFromSolution( AnalysisArtifact solution) {	
		PartIdentification jarPart = PartIdentifications.jar;
		
		return solution.getParts().values().stream()
				.filter( p -> {
						return jarPart.compare( PartIdentification.parse(p.getType())) == 0;
				})
				.findFirst()
				.map( p -> {
					return p.getResource();
				})
				.orElse(null);			
	}
	
	private FingerPrint extractSuppressAnnotation( GenericEntity owner, AnnotationEntity ae) {
		// value is mandatory 
		AnnotationValueContainer avc = ae.getMembers().get("value");
		String issue = avc.getSimpleStringValue();
		
		FingerPrint fp = FingerPrintExpert.build( owner, issue);
		// fingerprint is optional 
		AnnotationValueContainer avf = ae.getMembers().get("fingerprint");
		if (avf != null) {
			String fingerPrintExtension = avf.getSimpleStringValue();
			if (fingerPrintExtension.length() > 0) {
				fp = FingerPrintExpert.attach(fp, fingerPrintExtension);
			}
		}
		return fp;
		
	}

	@Override
	public Map<FingerPrint, ForensicsRating> collectSuppressAnnotations(Artifact terminalArtifact) {
		Map<FingerPrint, ForensicsRating> result = new HashMap<>();
		EntityCollector collector = new EntityCollector();
		collector.visit( terminalArtifact.getEntries());
		Set<GenericEntity> entities = collector.getEntities();
		for (GenericEntity ge : entities) {
			if (ge instanceof HasAnnotationsNature) {
				HasAnnotationsNature a = (HasAnnotationsNature) ge;
				for (TypeReferenceEntity tfr : a.getAnnotations()) {
					ZedEntity referencedType = tfr.getReferencedType();
					if (referencedType instanceof AnnotationEntity == false)
						continue;
					AnnotationEntity ae = (AnnotationEntity) referencedType;
					String name = ae.getDeclaringInterface().getReferencedType().getName();															
					if (name == null) {
						continue;
					}
					// is a ForwardDeclation
					if (name.equalsIgnoreCase(ANNO_SUPRESS_ISSUE)) {
						FingerPrint fp = extractSuppressAnnotation(ge, ae);
						result.put( fp, ForensicsRating.IGNORE);
					}
					else if (name.equalsIgnoreCase(ANNO_SUPRESS_ISSUES)) {
						Map<String, AnnotationValueContainer> members = ae.getMembers();
						for (AnnotationValueContainer avc : members.values()) {
							AnnotationEntity owner = avc.getOwner();
							FingerPrint fp = extractSuppressAnnotation(ge, owner);
							result.put( fp, ForensicsRating.IGNORE);
						}
												
					}
				} 							
			}
		}
		return result;
	}

	
	
}
