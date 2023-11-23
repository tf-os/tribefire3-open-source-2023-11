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
package com.braintribe.model.smood.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.access.smood.bms.BinaryManipulationStorage;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smood.test.model.ResourceList;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.provider.Holder;

public class SmoodAccessTest {

	protected List<File> tmpFiles = new ArrayList<>();

	@After
	public void cleanup() {
		for (File f : tmpFiles) {
			f.delete();
		}
	}

	@Test
	public void testDeleteFromManipulationBuffer() throws Exception {
		
		GmMetaModel model = new NewMetaModelGeneration().buildMetaModel("test:ResourceModel", Arrays.asList(Resource.T));

		XmlAccess xmlAccess = new XmlAccess();
		xmlAccess.setFilePath(new File("data.xml"));
		xmlAccess.setModelProvider(new Holder<GmMetaModel>(model));
		
		// File bufferFile = new File("buffer.bin");
		// bufferFile.delete();
		
		BinaryManipulationStorage bms = new BinaryManipulationStorage();
		bms.setStorageFile(new File("buffer.bin"));
		
		
		SmoodAccess smoodAccess = new SmoodAccess();
		
		smoodAccess.setDataDelegate(xmlAccess);
		smoodAccess.setManipulationBuffer(bms);
		smoodAccess.setAccessId("test");
		smoodAccess.setInitialBufferFlush(true);
		smoodAccess.setReadWriteLock(new MutuallyExclusiveReadWriteLock());
		smoodAccess.getDatabase();
		
		PersistenceGmSession session = new BasicPersistenceGmSession(smoodAccess);
		
		Resource resource = session.create(Resource.T);
		
		session.commit();
		
		session.deleteEntity(resource);
		session.commit();
		
		smoodAccess = new SmoodAccess();
		
		smoodAccess.setDataDelegate(xmlAccess);
		smoodAccess.setManipulationBuffer(bms);
		smoodAccess.setAccessId("test");
		smoodAccess.setInitialBufferFlush(true);
		smoodAccess.setReadWriteLock(new MutuallyExclusiveReadWriteLock());
		smoodAccess.getDatabase();
	}
	
	@Ignore
	public void testDeleteAndUpdateWithManipulationBuffer() throws Exception {

		GmMetaModel model = new NewMetaModelGeneration().buildMetaModel("test:ResourceModelWithList",
				Arrays.asList(Resource.T, ResourceList.T));

		XmlAccess xmlAccess = new XmlAccess();
		File dataFile = new File("data.xml");
		tmpFiles.add(dataFile);
		xmlAccess.setFilePath(dataFile);
		xmlAccess.setModelProvider(new Holder<GmMetaModel>(model));

		BinaryManipulationStorage bms = new BinaryManipulationStorage();
		File binFile = new File("buffer.bin");
		tmpFiles.add(binFile);
		bms.setStorageFile(binFile);

		SmoodAccess smoodAccess = new SmoodAccess();
		smoodAccess.setDataDelegate(xmlAccess);
		smoodAccess.setManipulationBuffer(bms);
		smoodAccess.setAccessId("test");
		smoodAccess.setInitialBufferFlush(true);
		smoodAccess.getDatabase();

		String id = UUID.randomUUID().toString();

		PersistenceGmSession session = new BasicPersistenceGmSession(smoodAccess);
		Resource resource = session.create(Resource.T);
		resource.setId(id);
		resource.setName("dummy.txt");
		session.commit();
		

		ResourceList list = session.create(ResourceList.T);
		list.getResourceList().add(resource);
		
		session.deleteEntity(resource);
		//session.commit();
		try {
		session.commit();
		} catch(Exception e) {
			e.printStackTrace();
			//ignore
		}

		/*
		resource.setName("thisshouldfail.txt");
		try {
			session.commit();
		} catch(Exception e) {
			//ignore
		}
		 */
		
		smoodAccess = new SmoodAccess();

		smoodAccess.setDataDelegate(xmlAccess);
		smoodAccess.setManipulationBuffer(bms);
		smoodAccess.setAccessId("test");
		smoodAccess.setInitialBufferFlush(true);
		smoodAccess.getDatabase();

		PersistenceGmSession checkSession = new BasicPersistenceGmSession(smoodAccess);
		Resource checkResource = checkSession.query().entity(Resource.T, id).require();

		Assert.assertEquals(id, checkResource.getId());
	}
}
