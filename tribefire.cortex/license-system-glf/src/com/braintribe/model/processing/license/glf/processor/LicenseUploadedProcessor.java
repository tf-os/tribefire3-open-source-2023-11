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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.license.License;
import com.braintribe.model.processing.license.glf.GlfLicenseManager;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

public class LicenseUploadedProcessor implements StateChangeProcessor<Resource, GenericEntity>, StateChangeProcessorRule, StateChangeProcessorMatch {

	protected static Logger logger = Logger.getLogger(LicenseUploadedProcessor.class);

	protected String myProcessorId = LicenseUploadedProcessor.class.getSimpleName();
	protected Supplier<String> usernameProvider = null;

	protected String licenseFileExtension = ".glf";

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {

		if (context.isForLifecycle()) {
			return Collections.emptyList();			
		}
		
		if (this.licenseFileExtension == null) {
			//No check needed
			return Collections.emptyList();
		}

		EntityType<?> contextType = context.getEntityType();
		boolean isCorrectType = Resource.T.isAssignableFrom(contextType);
		if (!isCorrectType)
			return Collections.emptyList();


		EntityProperty property = context.getEntityProperty();
		String propertyName = property.getPropertyName();
		boolean isCorrectProperty = propertyName.equals(Resource.name);
		if (!isCorrectProperty)
			return Collections.emptyList();


		PropertyManipulation propManipulation = context.getManipulation();
		if (!(propManipulation instanceof ChangeValueManipulation)) {
			if (logger.isTraceEnabled())
				logger.trace("The manipulation is "+propManipulation);

			return Collections.emptyList();
		}

		ChangeValueManipulation cvManipulation = (ChangeValueManipulation) propManipulation;
		Object newValueObject = cvManipulation.getNewValue();
		if (newValueObject == null)
			return Collections.emptyList();

		String newValue = (String) newValueObject;
		newValue = newValue.toLowerCase();
		
		if (logger.isTraceEnabled())
			logger.trace("Checking new resource name "+newValue);

		if (newValue.endsWith(this.licenseFileExtension)) {
			if (logger.isDebugEnabled())
				logger.debug("Found a license file");

			return Collections.<StateChangeProcessorMatch>singletonList( this);
		}


		return Collections.emptyList();
	}

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.afterOnlyCapabilities();
	}

	@Override
	public String getProcessorId() {
		return this.myProcessorId;
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor() {
		return this;
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<Resource> context, GenericEntity customContext) throws StateChangeProcessorException {
		Resource resource = context.getProcessEntity();
		PersistenceGmSession session = context.getSession();

		com.auxilii.glf.client.License glfLicense = null;
		try {
			ProvidedLicenseResourceLoader resourceLoader = new ProvidedLicenseResourceLoader(session, resource);
			glfLicense = com.auxilii.glf.client.License.loadLicensePreliminary(GlfLicenseManager.LICENSE_KEY, resourceLoader);
			if (!glfLicense.isValid()) {
				throw new Exception("The uploaded license is not valid.");
			}
		} catch (Exception e) {
			logger.warn("Could not validate the license file.", e);
			return;
		}


		License license = session.create(License.T);
		license.setLicenseResource(resource);
		license.setUploadDate(new Date());
		license.setActive(false);
		String username = null;
		try {
			username = this.usernameProvider.get();
			license.setUploader(username);
		} catch (Exception e) {
			logger.debug("Could not get the current user.", e);
		}

		try {
			Date expiryDate = glfLicense.getExpiryDate();
			if (expiryDate != null) {
				license.setExpiryDate(expiryDate);
			}
			String licensee = glfLicense.getLicensee();
			if (licensee != null) {
				license.setLicensee(licensee);
			}
			String licensor = glfLicense.getLicensor();
			if (licensor != null) {
				license.setLicensor(licensor);
			}
			Date issueDate = glfLicense.getIssueDate();
			if (issueDate != null) {
				license.setIssueDate(issueDate);
			}
			String licenseeAccount = glfLicense.getLicenseeAccount();
			if (licenseeAccount != null) {
				license.setLicenseeAccount(licenseeAccount);
			}
		} catch(Exception e ) {
			logger.warn("Could not get information from the license file.", e);
		}


		try {
			session.commit();
		} catch (Exception e) {
			logger.error("Could not create the new License entity based on the resource "+resource.getId(), e);
		}
	}

	@Configurable
	public void setLicenseFileExtension(String licenseFileExtension) {
		if (licenseFileExtension != null) {
			this.licenseFileExtension = licenseFileExtension.toLowerCase();
		} else {
			this.licenseFileExtension = null;
		}
	}
	@Required
	public void setUsernameProvider(Supplier<String> usernameProvider) {
		this.usernameProvider = usernameProvider;
	}

	@Override
	public String getRuleId() {
		return myProcessorId;
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor(String processorId) {		
		return this;
	}

}
