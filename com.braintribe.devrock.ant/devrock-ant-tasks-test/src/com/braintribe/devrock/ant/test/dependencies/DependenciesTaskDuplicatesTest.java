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
package com.braintribe.devrock.ant.test.dependencies;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.build.process.listener.MessageType;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.devrock.ant.test.TaskRunner;
import com.braintribe.devrock.ant.test.Validator;
import com.braintribe.devrock.ant.test.common.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;

/**
 * tests a simple dependencies-task run
 *  
 * @author pit
 *
 */
public class DependenciesTaskDuplicatesTest extends TaskRunner implements ProcessNotificationListener {
	
	private List<String> resultClasspathProperty;
	private List<String> resultClasspathFileset;

	@Override
	protected String filesystemRoot() {	
		return "dependencies.duplicates";
	}

	@Override
	protected RepoletContent archiveContent() {
		return archiveInput( "simplest.tree.definition.yaml");
	}
		

	@Override
	protected void additionalTasks() {
		// copy build file 
		TestUtils.copy( new File(input, "build.xml"), new File(output, "build.xml"));
		// copy pom file
		TestUtils.copy( new File(input, "pom.xml"), new File(output, "pom.xml"));
	}

	@Override
	protected void preProcess() {
	}

	@Override
	protected void postProcess() {
		resultClasspathProperty = loadNamesFromFilesetDump( new File(output, "classpath.txt"));
		resultClasspathFileset = loadNamesFromFilesetDump( new File(output, "classpath.fileset.txt"));
	}

	@Override
	public void acknowledgeProcessNotification(MessageType messageType, String msg) {
		System.out.println( msg);
		
	}

	@Test
	public void runSimpleDependenciesTasks() {
		process( new File( output, "build.xml"), "dependencies");
		
		List<String> expectations = new ArrayList<>();
		expectations.add( "a-1.0.1.jar");
		expectations.add( "b-1.0.1.jar");
		expectations.add( "t-1.0.1.jar");
		expectations.add( "x-1.0.1.jar");
		expectations.add( "y-1.0.1.jar");
		expectations.add( "z-1.0.1.jar");
		
		// assert
		Validator validator = new Validator();		
		validator.validate( "classpath", resultClasspathProperty, expectations);
		
		validator.validate( "classpath.fileset", resultClasspathFileset, expectations);
		
		validator.assertResults();		
	}
	
		
	
}
