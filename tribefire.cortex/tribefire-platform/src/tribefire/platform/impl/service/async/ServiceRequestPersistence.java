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
package tribefire.platform.impl.service.async;

import java.util.Date;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.model.execution.persistence.JobState;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.persistence.ServiceRequestJob;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;

public class ServiceRequestPersistence {

	protected HasStringCodec stringCodec;

	public ServiceRequestJob persistServiceRequest(PersistenceGmSession session, String correlationId, AsynchronousRequest asyncRequest,
			String queueId, StopWatch stopWatch) {

		ServiceRequestJob job = session.create(ServiceRequestJob.T);
		job.setId(correlationId);
		job.setCreationTimestamp(new Date());
		job.setStartTimestamp(new Date());
		job.setLastStatusUpdate(new Date());
		job.setQueueId(queueId);
		job.setPriority(asyncRequest.getPriority());
		job.setClientSessionId(asyncRequest.getSessionId());

		ServiceRequest embeddedServiceRequest = asyncRequest.getServiceRequest();
		if (embeddedServiceRequest != null) {
			String typeSignature = embeddedServiceRequest.type().getTypeSignature();
			job.setRequestTypeSignature(typeSignature);
			String shortName = embeddedServiceRequest.entityType().getShortName();
			job.setRequestTypeShortName(shortName);
			String description = embeddedServiceRequest.toSelectiveInformation();
			if (StringTools.isBlank(description) || description.equals(typeSignature) || description.startsWith(shortName + "[@")) {
				description = shortName;
			}
			job.setRequestSelectiveInformation(description);
		}

		stopWatch.intermediate("createJob");

		ServiceRequest managedInstance = cloneServiceRequest(session, asyncRequest, job);

		String serializedRequest = stringCodec.getStringCodec().encode(managedInstance);
		job.setSerializedRequest(serializedRequest);

		stopWatch.intermediate("serializedRequest");

		job.setState(JobState.pending);

		stopWatch.intermediate("createdJob");

		return job;
	}

	protected ServiceRequest cloneServiceRequest(PersistenceGmSession session, ServiceRequest payload, ServiceRequestJob job) {

		ServiceRequest clonedServiceRequest = payload.entityType().clone(new StandardCloningContext() {
			@Override
			public <T> T getAssociated(GenericEntity entity) {
				T associated = super.getAssociated(entity);
				if (associated != null) {
					return associated;
				}
				if (entity instanceof Resource) {
					Resource r = (Resource) entity;
					if (r.isTransient()) {
						Resource clonedResource = session.resources().create().md5(r.getMd5()).name(r.getName()).mimeType(r.getMimeType())
								.store(r::openStream);
						job.getRequestResources().put(clonedResource.getId(), clonedResource);

						registerAsVisited(entity, clonedResource);

						return (T) clonedResource;
					}
				}
				return associated;
			}
		}, payload, StrategyOnCriterionMatch.reference);

		return clonedServiceRequest;
	}

	@Required
	public void setStringCodec(HasStringCodec stringCodec) {
		this.stringCodec = stringCodec;
	}

}
