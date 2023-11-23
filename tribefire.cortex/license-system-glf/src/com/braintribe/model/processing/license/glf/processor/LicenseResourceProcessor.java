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
package com.braintribe.model.processing.license.glf.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.license.License;
import com.braintribe.model.license.service.AbstractLicenseRequest;
import com.braintribe.model.license.service.UploadLicense;
import com.braintribe.model.license.service.UploadedLicense;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;

public class LicenseResourceProcessor extends AbstractDispatchingServiceProcessor<AbstractLicenseRequest, Object> {

	private static Logger logger = Logger.getLogger(LicenseResourceProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	@Override
	protected void configureDispatching(DispatchConfiguration<AbstractLicenseRequest, Object> dispatching) {
		dispatching.register(UploadLicense.T, this::uploadLicense);
	}

	protected UploadedLicense uploadLicense(ServiceRequestContext context, UploadLicense request) {

		Resource licenseResource = request.getLicenseResource();
		if (licenseResource == null) {
			throw new IllegalArgumentException("The license resource must be provided in this service request.");
		}

		Pair<String, byte[]> pair = getLicenseContentBytes(licenseResource);

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();
		Resource storedResource = cortexSession.resources().create().name(pair.first).mimeType("text/plain")
				.store(() -> new ByteArrayInputStream(pair.second));

		License license = searchLicense(cortexSession, storedResource);
		if (license == null) {
			throw new IllegalStateException("Could not find a license using the new resource " + storedResource);
		}

		if (request.getActivate()) {
			logger.debug(() -> "Activating new license " + license);
			license.setActive(true);
			cortexSession.commit();
		}

		UploadedLicense result = UploadedLicense.T.create();

		result.setResourceId(storedResource.getId());
		result.setLicenseId(license.getId());

		return result;
	}

	private License searchLicense(PersistenceGmSession cortexSession, Resource storedResource) {
		//@formatter:off
		SelectQuery selectQuery = new SelectQueryBuilder().from(License.T, "l")
			.join("l", License.licenseResource, "r")
			.select("l")
			.where()
				.property("r", Resource.id).eq(storedResource.getId())
			.done();
		//@formatter:on
		License license = cortexSession.query().select(selectQuery).first();

		return license;
	}

	private Pair<String, byte[]> getLicenseContentBytes(Resource licenseResource) {

		String name = licenseResource.getName();
		if (StringTools.isBlank(name)) {
			name = "license.sigxml.glf";
		}

		if (name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip")) {

			try (ZipInputStream zipIn = new ZipInputStream(licenseResource.openStream())) {

				ZipEntry entry = zipIn.getNextEntry();

				while (entry != null) {

					if (!entry.isDirectory() && entry.getName().endsWith(".sigxml.glf")) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOTools.pump(zipIn, baos);
						return new Pair<>(entry.getName(), baos.toByteArray());
					}
					zipIn.closeEntry();
					entry = zipIn.getNextEntry();
				}

			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not extract content of Resource " + licenseResource);
			}

		}

		logger.debug(() -> "Will process license resource as-is.");

		try (InputStream in = licenseResource.openStream()) {
			return new Pair<>(name, IOTools.slurpBytes(in));
		} catch (IOException ioe) {
			throw Exceptions.unchecked(ioe, "Could not read Resource " + licenseResource);
		}
	}

	@Configurable
	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

}
