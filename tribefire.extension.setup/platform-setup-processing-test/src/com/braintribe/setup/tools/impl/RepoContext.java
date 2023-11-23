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
package com.braintribe.setup.tools.impl;

import java.io.File;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class RepoContext implements AutoCloseable {

	public final String testName;
	public final String repoName;
	public final Launcher launcher;
	public final File repoFolder;

	public RepoContext(String testName, RepoletContent content) {
		this.testName = testName;
		this.repoName = "repo-" + testName;

		// @formatter:off
		this.repoFolder = new File("ignored/" + repoName);
		this.launcher = Launcher.build()
				.repolet()
//				.name(repoName)
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(content)
					.close()
				.close()
			.done();
		// @formatter:on

		lanuch();
	}

	private void lanuch() {
		FileTools.deleteDirectoryRecursivelyUnchecked(repoFolder);
		FileTools.ensureFolderExists(repoFolder);

		launcher.launch();
	}

	private static final Logger log = Logger.getLogger(RepoContext.class);

	@Override
	public void close() {
		try {
			FileTools.deleteDirectoryRecursivelyUnchecked(repoFolder);
		} catch (Exception e) {
			log.warn("Unable to cleanup repo folder: " + repoFolder.getAbsolutePath());
		}
		launcher.shutdown();
	}

}
