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
package com.braintribe.qa.tribefire.qatests.other;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.resource.specification.PdfSpecification;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.persistence.DeletionScope;
import com.braintribe.model.resourceapi.persistence.UploadSource;
import com.braintribe.model.resourceapi.persistence.UploadSourceResponse;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetSource;
import com.braintribe.qa.tribefire.qatests.deployables.access.AbstractPersistenceTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.IOTools;

/**
 * This class tests the Resource upload, update and download.
 *
 */
public abstract class AbstractResourcesTest extends AbstractPersistenceTest {

	protected List<File> tmpFiles = new ArrayList<>();

	@After
	public void cleanup() throws Exception {
		for (File f : tmpFiles) {
			f.delete();
		}
		tmpFiles.clear();
	}

	@Test
	@Ignore // pdf image specifications aren't created out of the box yet
	public void pdfUpload() throws Exception {
		PdfSpecification pdfSpecification = testSpecification("document-1.pdf", "application/pdf", PdfSpecification.class);

		assertThat(pdfSpecification.getPageCount()).isEqualTo(1);
	}

	@Test
	@Ignore // tiff image specifications aren't created out of the box yet
	public void tiffUpload() throws Exception {
		RasterImageSpecification imageSpecification = testSpecification("image.tif", "image/tiff", RasterImageSpecification.class);

		assertThat(imageSpecification.getPageCount()).isEqualTo(1);
		assertThat(imageSpecification.getPixelHeight()).isEqualTo(480);
		assertThat(imageSpecification.getPixelWidth()).isEqualTo(640);
	}

	@Test
	@Category(KnownIssue.class) // The test resource directory can't be found in the cloud...
	public void jpgUpload() throws Exception {
		RasterImageSpecification imageSpecification = testSpecification("image.jpg", "image/jpeg", RasterImageSpecification.class);

		assertThat(imageSpecification.getPageCount()).isEqualTo(1);
		assertThat(imageSpecification.getPixelHeight()).isEqualTo(480);
		assertThat(imageSpecification.getPixelWidth()).isEqualTo(640);
	}

	abstract protected String getUserName();

	private <T extends ResourceSpecification> T testSpecification(String filename, String mimeType, Class<T> specificationClass)
			throws FileNotFoundException, IOException {
		File tmpFile = new File(testDir(AbstractResourcesTest.class), filename);

		PersistenceGmSession uploadSession = newSession();

		FileInputStream fis = new FileInputStream(tmpFile);
		Resource resource = uploadSession.resources().create().name(tmpFile.getName()).store(fis);
		fis.close();

		assertThat(resource.getMimeType()).isEqualTo(mimeType);
		assertThat(resource.getSpecification()).isInstanceOf(specificationClass);

		return (T) resource.getSpecification();
	}

	@Test
	public void resourceUploadSmall() throws Exception {
		Resource resource = this.uploadWithResourceAccess(3);

		newSession().resources().delete(resource).scope(DeletionScope.binary).delete();

		PersistenceGmSession session = newSession();
		resource = session.query().entity(resource).refresh();
		assertThat(resource.getResourceSource()).isNotNull();
	}

	@Test
	public void resourceUploadMiddle() throws Exception {
		Resource resource = this.uploadWithResourceAccess(10240);

		ResourceSource resourceSource = resource.getResourceSource();

		newSession().resources().delete(resource).scope(DeletionScope.source).delete();

		PersistenceGmSession session = newSession();
		assertThat(resourceSource).isNotNull();
		resource = session.query().entity(resource).refresh();
		assertThat(resource.getResourceSource()).isNull();
		assertThatThrownBy(session.query().entity(resourceSource)::refresh).isNotNull(); // Different kinds of
																							// exceptions might be
																							// thrown
	}

	@Test
	public void resourceUploadLarge() throws Exception {
		Resource resource = this.uploadWithResourceAccess(10_240_000);

		newSession().resources().delete(resource).scope(DeletionScope.resource).delete();

		PersistenceGmSession session = newSession();
		assertThat(session.query().entity(resource).find()).isNull();
	}

	@Test
	public void resourceUpdate() throws Exception {
		Resource resource = uploadWithResourceAccess(3);
		resource = updateWithInputStream(1024, resource, false);
		resource = updateWithInputStream(1024, resource, true);
		resource = updateWithInputStream(10244567, resource, true);
	}

	@Test
	@Category(KnownIssue.class)
	// Source requests currently don't work because of the new security in place.
	// TODO: Either delete them entirely or find a way to support them again.
	public void resourceSourceRequests() throws Exception {
		File tmpFile = this.createTempFile(100);

		PersistenceGmSession uploadSession = newSession();

		TransientSource transientSource = TransientSource.T.create();
		transientSource.setGlobalId(UUID.randomUUID().toString());
		transientSource.setInputStreamProvider(() -> new FileInputStream(tmpFile));

		UploadSource uploadSource = UploadSource.T.create();
		uploadSource.setResourceSource(transientSource);

		UploadSourceResponse uploadSourceResponse = uploadSource.eval(uploadSession).get();
		Resource uploadedResource = uploadSourceResponse.getResource();

		try (InputStream is = newSession().resources().retrieve(uploadedResource).stream(); FileInputStream fis = new FileInputStream(tmpFile)) {
			assertThat(is).hasSameContentAs(fis);
		}

		PersistenceGmSession downloadSession = newSession();

		GetSource getSource = GetSource.T.create();
		getSource.setResourceSource(uploadedResource.getResourceSource());

		GetBinaryResponse getBinaryResponse = getSource.eval(downloadSession).get();
		Resource downloadedResource = getBinaryResponse.getResource();

		try (InputStream is = downloadedResource.openStream(); InputStream fis = new FileInputStream(tmpFile)) {
			assertThat(is).hasSameContentAs(fis);
		}
	}

	protected Resource uploadWithResourceAccess(int size) throws Exception {
		File tmpFile = this.createTempFile(size);

		PersistenceGmSession uploadSession = newSession();

		FileInputStream fis = new FileInputStream(tmpFile);
		Resource resource = uploadSession.resources().create().name(tmpFile.getName()).store(fis);
		fis.close();

		this.checkUploadedResource(tmpFile, resource);

		return resource;
	}

	protected Resource updateWithInputStream(int size, Resource resourceToUpdate, boolean deleteOldResourceSource) throws Exception {

		File tmpFile = this.createTempFile(size);

		Object oldSourceId = resourceToUpdate.getResourceSource().getId();
		String oldSourceGlobalId = resourceToUpdate.getResourceSource().getGlobalId();

		PersistenceGmSession uploadSession = newSession();

		ResourceSource oldResourceSource = getUpdatedEntity(resourceToUpdate.getResourceSource());

		Resource resource;

		// Make sure the binary can be found without sending the resource source as part of the request
		resourceToUpdate.setResourceSource(null);

		try (FileInputStream fis = new FileInputStream(tmpFile)) {
			resource = uploadSession.resources().update(resourceToUpdate).name(tmpFile.getName()).deleteOldResourceSource(deleteOldResourceSource)
					.store(fis);
		}

		assertThat(resource.getGlobalId()).isEqualTo(resourceToUpdate.getGlobalId());
		assertThat(resource.getId().toString()).isEqualTo(resourceToUpdate.getId().toString());

		if (resource.getResourceSource().getGlobalId() != null) {
			// This is only relevant in accesses that assign globalIds, i.e. SmoodAccess
			assertThat(resource.getResourceSource().getGlobalId()).isNotEqualTo(oldSourceGlobalId);
		}

		assertThat(resource.getResourceSource().getId().toString()).isNotEqualTo(oldSourceId);

		if (oldResourceSource != null) {
			if (deleteOldResourceSource) {
				assertThat(getUpdatedEntity(oldResourceSource)).isNull();
			} else {
				assertThat(getUpdatedEntity(oldResourceSource)).isNotNull();
			}
		}

		this.checkUploadedResource(tmpFile, resource);

		return resource;
	}

	protected abstract PersistenceGmSession newSession();

	protected void checkUploadedResource(File file, Resource uploadedResource) throws Exception {
		assertThat(uploadedResource.getName()).isEqualTo(file.getName());

		PersistenceGmSession downloadSession = newSession();
		Resource downloadResource = getUpdatedEntity(uploadedResource);

		assertThat(downloadResource).isNotNull();
		assertThat(downloadResource.getName()).isEqualTo(uploadedResource.getName());
		assertThat(downloadResource.getId().toString()).isEqualTo(uploadedResource.getId().toString());
		assertThat(downloadResource.getFileSize()).isEqualTo(file.length());
		assertThat(downloadResource.getCreated()).isNotNull();
		assertThat(downloadResource.getCreator()).isEqualTo(getUserName());
		assertThat(downloadResource.getTags()).isEmpty();
		assertThat(downloadResource.getMd5()).isEqualTo(CommonTools.asString(IOTools.getMD5CheckSum(new FileInputStream(file))));
		// Depending on the system's mime type detector one of these may be detected:
		assertThat(downloadResource.getMimeType()).isNotBlank();

		// Make sure the binary can be found without sending the resource source as part of the request
		downloadResource.setResourceSource(null);

		try (InputStream is = downloadSession.resources().retrieve(downloadResource).stream(); FileInputStream fis = new FileInputStream(file)) {
			assertThat(is).hasSameContentAs(fis);
		}
	}

	private <T extends GenericEntity> T getUpdatedEntity(T entity) {
		if (entity == null) {
			return null;
		}

		EntityQuery query = EntityQueryBuilder.from(entity.entityType()).where().property(GenericEntity.id).eq(entity.getId()).done();
		return newSession().query().entities(query).unique();
	}

	protected File createTempFile(int length) throws Exception {
		File tmpFile = File.createTempFile("ResourcesTest", ".tmp");
		if (length > 0) {
			FileOutputStream fos = new FileOutputStream(tmpFile);
			Random rnd = new Random();
			int bytesWritten = 0;
			int bufferSize = Math.min(Math.max(length / 10, 1024), length);
			byte[] bytes = new byte[bufferSize];
			while (bytesWritten < length) {
				rnd.nextBytes(bytes);
				int len = Math.min(length - bytesWritten, bufferSize);
				fos.write(bytes, 0, len);
				bytesWritten += len;
			}
			fos.close();
		}
		tmpFile.deleteOnExit();
		tmpFiles.add(tmpFile);
		return tmpFile;
	}
}
