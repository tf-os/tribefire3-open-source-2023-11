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
package tribefire.cortex.testrunner;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.zip.ZipContext;

import tribefire.cortex.testrunner.api.ModuleTestRunner;
import tribefire.cortex.testrunner.api.RunTests;

public class TestRunningProcessor implements ServiceProcessor<RunTests, Resource> {

	private static final Logger log = Logger.getLogger(TestRunningProcessor.class);

	private List<ModuleTestRunner> testRunners;

	private ResourceBuilder resourceBuilder;

	@Required
	public void setTestRunners(List<ModuleTestRunner> testRunners) {
		this.testRunners = testRunners;
	}

	@Required
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	@Override
	public Resource process(ServiceRequestContext requestContext, RunTests request) {
		File testResultsRoot = newTempDir();

		testRunners.forEach(r -> runTests(r, request, testResultsRoot));

		try (ZipContext zc = Archives.zip().pack(testResultsRoot)) {
			Resource result = resourceBuilder.newResource() //
					.withMimeType("application/zip") //
					.withName("test-results.zip") //
					.usingOutputStream(zc::to);

			return result;

		} finally {
			try {
				FileTools.deleteDirectoryRecursively(testResultsRoot);
			} catch (IOException e) {
				log.warn("Unable to delete directory with test results:" + testResultsRoot.getAbsolutePath(), e);
			}
		}
	}

	private void runTests(ModuleTestRunner testRunner, RunTests request, File testResultsRoot) {
		File testRunnerOutputFolder = new File(testResultsRoot, testRunner.testRunnerSelector());
		testRunner.runTests(request, testRunnerOutputFolder);
	}

	private File newTempDir() {
		try {
			return Files.createTempDirectory(getClass().getSimpleName()).toFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
