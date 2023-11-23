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
package com.braintribe.eclipse.model.nature;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.statushandlers.StatusManager;

import com.braintribe.build.artifacts.mc.wire.pomreader.PomReaderSetup;
import com.braintribe.build.artifacts.mc.wire.pomreader.PomReaders;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.build.model.ModelDeclarations;

public class ModelBuilder extends IncrementalProjectBuilder {
	public static final String ID = "com.braintribe.eclipse.model.nature.ModelBuilder";
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		try {
			return internalBuild(kind, args, monitor);
		} catch (Exception e) {
			String msg = "error building model declaration file";
			ModelBuilderStatus status = new ModelBuilderStatus(msg, e);
			StatusManager.getManager().handle(status);
			throw e instanceof CoreException ? (CoreException) e : new CoreException( status);
		}
	}
	
	protected IProject[] internalBuild(int kind, Map<String, String> args, IProgressMonitor monitor) throws Exception {

		IProject project = getProject();
		
		IResourceDelta delta = getDelta(project);

		boolean full = kind == FULL_BUILD;
		
		boolean isRelevantChange = full || isRelevantDelta(delta);
		
		if (!isRelevantChange)
			return null;
		
		if (project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			String[] runtimeClasspathEntries = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
			
			URL[] cp = convertClasspath(runtimeClasspathEntries);
			
			/*File projectFolder = javaProject.getUnderlyingResource().getLocation().toFile();
			File srcFolder = new File(projectFolder, "src");
			IResource findMember = project.findMember("src/model-declaration.xml");
			
			if (findMember == null) {
				IFolder src = project.getFolder("src");
				IFile file = src.getFile("model-declaration.xml");
				file.create(new ByteArrayInputStream(new byte[0]), IResource.NONE, null);
			}
			
			runUpdateModelDeclaration(cp, srcFolder);
			
			findMember = project.findMember("src/model-declaration.xml");
			if (findMember != null) {
				findMember.refreshLocal(IResource.DEPTH_ZERO, null);
			}*/
			
			IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
			
			List<File> outputLocations = getOutputLocations(javaProject);
			// target folder is first (and currently only) location here
			File targetFolder = outputLocations.get(0);
			
			for (IClasspathEntry entry: rawClasspath) {
				if (entry.getContentKind() == IClasspathEntry.CPE_SOURCE) {
					IPath outputLocation = entry.getOutputLocation();
					File outputLocationFile = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(outputLocation).getRawLocation().toFile();
					outputLocations.add(outputLocationFile);
				}
			}
			
			File pomFile;
			IResource pomResource = project.findMember("pom.xml");
			if (pomResource != null) {
				pomFile = pomResource.getLocation().toFile();
			}
			else {
				String msg = "project [" + project.getName() + "] has no pom";
				ModelBuilderStatus status = new ModelBuilderStatus(msg, IStatus.ERROR);
				StatusManager.getManager().handle(status);
				return null;
			}
			
			runUpdateModelDeclaration(cp, outputLocations, pomFile, targetFolder);
		}
		
		return null;
	}
	
	private List<File> getOutputLocations(IJavaProject javaProject) throws JavaModelException {
		List<File> outputLocations = new ArrayList<>();
		
		IPath wsOutputLocation = javaProject.getOutputLocation();
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(wsOutputLocation);
		final File outputFolder = ifile.getRawLocation().toFile();
		outputLocations.add(outputFolder);
		return outputLocations;
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		IJavaProject javaProject = JavaCore.create(project);
		List<File> outputLocations = getOutputLocations(javaProject);
		
		for (File outputLocation: outputLocations) {
			File modelDeclarationCandidate = new File(outputLocation, "model-declaration.xml");
			if (modelDeclarationCandidate.exists())
				modelDeclarationCandidate.delete();
		}
		super.clean(monitor);
	}

	private boolean isRelevantDelta(IResourceDelta delta) {
		// TODO : check incremental logic, currently always full blown build
		return true;
		/*
		if (delta == null)
			return true;
		
		IPath iPath = delta.getFullPath();
		if ("java".equals(iPath.getFileExtension())) {
			int kind = delta.getKind();
			if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED  || kind == IResourceDelta.CHANGED)
				return true;
		}
		
		IResource resource = delta.getResource();
		if (resource != null && resource.getName().equalsIgnoreCase("pom.xml")) {
			return true;
		}
		for (IResourceDelta resourceDelta: delta.getAffectedChildren()) {
			if (isRelevantDelta(resourceDelta))
				return true;
		}
		
		return false;
		*/
	}

	private void runUpdateModelDeclaration(URL[] cp, List<File> outputFolders, File pomFile, File targetFolder) throws Exception {

		PomReaderSetup pomReaderSetup = PomReaders.build().configureWireContext(b -> {
			b.bindContract(PomReaderExternalContract.class, "com.braintribe.eclipse.model.nature.wire.pomreader.space.PomReaderExternalSpace");
		}).done();
		
		ModelDeclarations.buildModelDeclaration(pomReaderSetup.pomReader(), Arrays.asList(cp), outputFolders, pomFile, targetFolder);
		/*
			ClassLoader classLoader = new URLClassLoader(cp);
			Class<?> mainClass = Class.forName("com.braintribe.model.build.UpdateModelDeclaration", true, classLoader);
			
			Method mainMethod = mainClass.getMethod("main", String[].class);
			
			String outputFolderPath = toPath(outputFolders);
			
			mainMethod.invoke(null, (Object)new String[]{outputFolderPath});
			*/
	}

	private String toPath(List<File> outputFolders) {
		return outputFolders.stream()
				.map(f -> f.getAbsolutePath())
				.collect(Collectors.joining(File.pathSeparator));
	}

	private URL[] convertClasspath(String[] runtimeClasspathEntries) {
			URL cp[] = new URL[runtimeClasspathEntries.length];
			
			
			for (int i = 0; i < cp.length; i++) {
				IPath path = new Path(runtimeClasspathEntries[i]);
				File file = path.toFile();
				try {
					cp[i] = file.toURI().toURL();
				} catch (MalformedURLException e) {
					String msg = "error while converting classpath entry [" + file + "] to a url";
					ModelBuilderStatus status = new ModelBuilderStatus(msg, e);
					StatusManager.getManager().handle(status);
					throw new RuntimeException(msg, e);
				}
			}
			return cp;
	}
	
}

