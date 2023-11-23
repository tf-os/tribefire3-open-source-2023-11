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
package com.braintribe.devrock.greyface.view.tab.parameter;

import java.io.File;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.devrock.greyface.process.retrieval.LocalSingleDirectoryDependencyResolver;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class LocalFileSystemScanner {	

	public class Tuple {
		public String condensedArtifactName;
		public String directory;
	}
	
	public Tuple scanLocalFileSystem(Display display) {		
		FileDialog fDialog = new FileDialog( display.getActiveShell(), SWT.OPEN | SWT.SINGLE);
		
		fDialog.setFilterExtensions( new String [] {"*.pom", "*.jar", "*.javadoc"});
		String name = fDialog.open();
		if (name == null)
			return null;
		
		Tuple tuple = new Tuple();
		File file = new File( name);
		if (file.isDirectory()) {		
			tuple.directory = file.getAbsolutePath();
			return tuple;
		}
		
		 
		File directory = file.getParentFile();
		tuple.directory = directory.getAbsolutePath();
			
		// extract data to text field.. 			
		Artifact artifact = null;
		if (name.endsWith( ".pom") == false) {
			artifact = NameParser.parseFileName( new File( name));					
			String condensedName = artifact.getArtifactId() + "-" + VersionProcessor.toString( artifact.getVersion());
			tuple.condensedArtifactName = condensedName;			
			return tuple;			
		} else {				
			try {
				ArtifactPomReader reader = GreyfaceScope.getScope().getPomReader();						
				LocalSingleDirectoryDependencyResolver localDependencyResolver = new LocalSingleDirectoryDependencyResolver();
				localDependencyResolver.setLocalDirectory( directory.getAbsolutePath());
				reader.setDependencyResolver( localDependencyResolver);									
				Solution solution = reader.read( UUID.randomUUID().toString(), name);
				String condensedName = NameParser.buildName( solution);
				tuple.condensedArtifactName = condensedName;
				return tuple;
			} catch (PomReaderException e) {
				return tuple;
			}
		}		
	}
	
	public Tuple scanForLocalMavenCompatibleRoot( Display display) {
		DirectoryDialog dialog = new DirectoryDialog(display.getActiveShell());
		String name = dialog.open();
		if (name == null)
			return null;
		
		Tuple tuple = new Tuple();
		File file = new File( name);
		if (file.isDirectory()) {		
			tuple.directory = file.getAbsolutePath();
			return tuple;
		}
		return null;
	}
}
