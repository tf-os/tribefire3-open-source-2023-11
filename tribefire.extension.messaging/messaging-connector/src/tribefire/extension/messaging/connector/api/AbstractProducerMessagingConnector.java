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
package tribefire.extension.messaging.connector.api;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.service.produce.ProduceMessageResult;
import tribefire.extension.messaging.service.reason.general.MarshallingError;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;

public abstract class AbstractProducerMessagingConnector implements ProducerMessagingConnector {

	private static final Logger logger = Logger.getLogger(AbstractProducerMessagingConnector.class);
	private static final Marshaller marshaller = new JsonStreamMarshaller();

	// TODO: this should be done in a nice way
	private String binaryPersistenceExternalId;

	//@formatter:off
	private static final GmSerializationOptions SERIALIZATION_OPTIONS =
			GmSerializationOptions.deriveDefaults()
					.setOutputPrettiness(OutputPrettiness.none)
					.stabilizeOrder(false)
					.writeEmptyProperties(false)
					.writeAbsenceInformation(false)
					.build();
	//@formatter:on

	// -----------------------------------------------------------------------
	// PRODUCE
	// -----------------------------------------------------------------------

	@Override
	public ProduceMessageResult sendMessage(List<Message> messages, ServiceRequestContext requestContext, PersistenceGmSession session) {
		checkResourceMapping(messages);

		messages.forEach(m -> {
			persistResourcesWhereApplicable(m, requestContext, session);
			byte[] bytes = marshallMessage(m);
			deliverMessageString(bytes, m.getTopics());
			logger.error(">>>>>>>>>>>>>>>>> DELIVERED MESSAGE: " + new String(bytes));
		});
		return ProduceMessageResult.T.create();
	}

	protected void setBinaryPersistenceExternalId(String binaryPersistenceExternalId) {
		this.binaryPersistenceExternalId = binaryPersistenceExternalId;
	}

	// -----------------------------------------------------------------------
	// ABSTRACT
	// -----------------------------------------------------------------------

	protected abstract void deliverMessageString(byte[] message, Set<String> topics);

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	public CheckResultEntry health() {
		return actualHealth();
	}

	protected abstract CheckResultEntry actualHealth();

	// -----------------------------------------------------------------------
	// HELPERS - MARSHALLING
	// -----------------------------------------------------------------------
	private void checkResourceMapping(List<Message> messages) {
		messages.forEach(m -> {
			if (!m.getResourceMapping().isEmpty()) {
				throw new UnsatisfiedMaybeTunneling(
						Reasons.build(ArgumentNotSatisfied.T).text("'" + Message.resourceMapping + "' must not be set").toMaybe());
			}
		});
	}

	private byte[] marshallMessage(Message rawMessage) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			marshaller.marshall(bos, rawMessage, SERIALIZATION_OPTIONS);
			return bos.toByteArray();
		} catch (MarshallException e) {
			logger.debug(e.getMessage());
			throw new UnsatisfiedMaybeTunneling(
					Reasons.build(MarshallingError.T).text("Marshalling error: " + e.getMessage()).enrich(r -> r.setValue(rawMessage)).toMaybe());
		}
	}

	private void persistResourcesWhereApplicable(Message message, ServiceRequestContext requestContext, PersistenceGmSession session) {
		EntityCollector collector = new EntityCollector();
		collector.visit(message);
		Set<GenericEntity> entities = collector.getEntities();
		//@formatter:off
		entities.stream()
				.filter(Resource.class::isInstance)
				.map(Resource.class::cast)
				.filter(message::shouldPersist)
				.forEach(r-> {
					Resource storedBinary = persistBinary(r, requestContext, session.newEquivalentSession());
					enrichResourceMapping(message, r, storedBinary);
				});
		//@formatter:on
		logger.info("AbstractProducerMessagingConnector: Persisted " + message.getResourceMapping().size() + " binaries for message " + message);
	}

	private Resource persistBinary(Resource resource, ServiceRequestContext requestContext, PersistenceGmSession session) {
		StoreBinary req = session.create(StoreBinary.T);
		resource.attach(session);

		req.setCreateFrom(resource);
		req.setPersistResource(true);
		// TODO: should be done in a good way
		if (CommonTools.isEmpty(binaryPersistenceExternalId)) {
			req.setServiceId("binaryProcessor.fileSystem");
		} else {
			req.setServiceId(binaryPersistenceExternalId);
		}

		req.setDomainId("cortex"); // TODO: not sure???
		StoreBinaryResponse storeBinaryResponse = req.eval(requestContext).get();
		return storeBinaryResponse.getResource();
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------
	private void enrichResourceMapping(Message message, Resource sentResource, Resource storedResource) {
		// in case we have a transient Resource we use as the reference the globalId of the TransientSource
		// in case of all other Resource we either use the id (on priority) or the globalId
		// @formatter:off
		String reference = sentResource.isTransient()
				? sentResource.getResourceSource().getGlobalId()
				: Optional.ofNullable(sentResource.getGlobalId()).orElseGet(sentResource::getId);
		// @formatter:on
		Objects.requireNonNull(reference, "Either id or globalId must be set");

		message.getResourceMapping().put(reference, storedResource);
	}

}
