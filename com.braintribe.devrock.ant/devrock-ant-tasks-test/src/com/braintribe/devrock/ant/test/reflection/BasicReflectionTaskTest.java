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
package com.braintribe.devrock.ant.test.reflection;

import java.io.File;

import org.junit.Test;

import com.braintribe.build.process.listener.MessageType;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.devrock.ant.test.TaskRunner;
import com.braintribe.devrock.ant.test.Validator;
import com.braintribe.devrock.ant.test.common.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * tests the reflection building task - in a positive case, i.e. the test checks a correct run.
 *  
 * @author pit
 *
 */
public class BasicReflectionTaskTest extends TaskRunner implements ProcessNotificationListener {	

	@Override
	protected String filesystemRoot() {	
		return "reflection.basic";
	}

	@Override
	protected RepoletContent archiveContent() {
		return RepoletContent.T.create();
	}
		

	@Override
	protected void additionalTasks() {
		// copy test artifact
		TestUtils.copy( new File(input, "initial"), output);
	}

	@Override
	protected void preProcess() {
	}

	@Override
	protected void postProcess() {
	}

	@Override
	public void acknowledgeProcessNotification(MessageType messageType, String msg) {
		System.out.println( msg);
		
	}

	@Test
	public void runReflectionTask() {
		process( new File( output, "build.xml"), "reflection", false, false);
				
		// assert
		Validator validator = new Validator();
		
		File cgen = new File( output, "class-gen");
		
		validator.validateReflectionResult(CompiledArtifactIdentification.parse("com.braintribe.devrock.test:test-artifact-model#2.0.4-pc"), cgen);
		
		validator.assertResults();		
	}
	
		
	
}
