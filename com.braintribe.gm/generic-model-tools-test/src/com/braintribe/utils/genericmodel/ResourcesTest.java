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
package com.braintribe.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.tools.gm.GmTestTools;

public class ResourcesTest {

	protected List<File> tmpFiles = new ArrayList<>();
	protected List<File> tmpDirs = new ArrayList<>();

	@After
	public void cleanup() throws Exception {
		for (File f : tmpFiles) {
			f.delete();
		}
		tmpFiles.clear();
		for (File d : tmpDirs) {
			File[] fs = d.listFiles();
			if (fs != null) {
				for (File f : fs) {
					f.delete();
				}
			}
			d.delete();
		}
		tmpDirs.clear();
	}

	@Test
	@Category(KnownIssue.class)
	public void testStandaloneResourceAccess() throws Exception {

		File tmpFile = File.createTempFile("GmSessionFactoriesTest", ".xml");
		tmpFiles.add(tmpFile);

		PersistenceGmSession session = GmTools.newSessionWithSmoodAccess(tmpFile);
		if (!(session instanceof BasicPersistenceGmSession)) {
			return;
		}
		BasicPersistenceGmSession basicSession = (BasicPersistenceGmSession) session;

		File tmpDir = File.createTempFile("GmSessionFactoriesTest-storage", ".dir");
		tmpDirs.add(tmpDir);
		tmpDir.delete();
		tmpDir.mkdirs();

		File file = new File("pom.xml");

		GmTestTools.attachStandaloneResources(basicSession, tmpDir);

		FileInputStream in = new FileInputStream(file);
		Resource uploadedResource = session.resources().create().store(in);
		session.commit();

		assertThat(uploadedResource).isNotNull();

		PersistenceGmSession session2 = GmTools.newSessionWithSmoodAccess(tmpFile);
		if (!(session2 instanceof BasicPersistenceGmSession)) {
			return;
		}
		BasicPersistenceGmSession basicSession2 = (BasicPersistenceGmSession) session2;
		GmTestTools.attachStandaloneResources(basicSession2, tmpDir);

		EntityQuery query = EntityQueryBuilder.from(Resource.class).where().property(Resource.id).eq(uploadedResource.getId()).done();
		Resource downloadResource = session2.query().entities(query).unique();

		InputStream is = session2.resources().retrieve(downloadResource).stream();
		FileInputStream fis = new FileInputStream(file);

		try {
			assertThat(is).isEqualTo(fis);
		} finally {
			fis.close();
			is.close();
		}
	}

}
