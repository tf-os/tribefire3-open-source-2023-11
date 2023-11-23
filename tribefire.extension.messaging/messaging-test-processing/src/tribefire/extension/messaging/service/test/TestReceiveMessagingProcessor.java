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
package tribefire.extension.messaging.service.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.DuplexStreamProvider;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.webrpc.client.TempFileInputStreamProviders;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;

import tribefire.extension.messaging.model.Envelop;
import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessage;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessageResult;

public class TestReceiveMessagingProcessor implements ServiceProcessor<ProcessConsumedMessage, ProcessConsumedMessageResult> {
	private tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor deployable;

	private static final Logger logger = Logger.getLogger(TestReceiveMessagingProcessor.class);
	private static final EntityQuery PERSISTED_SELECT_QUERY = EntityQueryBuilder.from(Resource.T).where().property("name")
			.ilike("infected_persisted*").done();
	public static final String PERSISTED_FILE = "infected_persisted.txt";
	public static final String PERSISTED_JSON = "infected_persisted.json";
	private String externalId;
	private PersistenceGmSessionFactory sessionFactory;

	// -----------------------------------------------------------------------
	// PROCESSING
	// -----------------------------------------------------------------------

	@Override
	public ProcessConsumedMessageResult process(ServiceRequestContext requestContext, ProcessConsumedMessage request) {
		logger.info(() -> externalId + "A Message has been Received!");

		Envelop envelop = request.getEnvelop();
		if (envelop.getMessages().isEmpty()) {
			logger.warn(() -> externalId + "Received an empty 'Envelop', this is weirdo...");
		} else {
			// Here we set the last message as the only into the envelope just in case something is going wrong for whatever reason
			Message message = envelop.getMessages().get(envelop.getMessages().size() - 1);
			envelop.setMessages(Collections.singletonList(message));

			// Delete all possible dupes of files, if Message is a Resource -> we persist the resource, else -> we marshal the
			// object into JSON and store it in a json file
			PersistenceGmSession session = sessionFactory.newSession("cortex");
			deleteDupes(session, message.getValues());

			String encodedMessage = marshallMessage(envelop);
			persistStringToFile(session, encodedMessage);

			persistResource(session, message);

			logger.info(() -> externalId + "Message has being processed successfully, file stored.");
		}

		ProcessConsumedMessageResult result = ProcessConsumedMessageResult.T.create();
		return result;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private String marshallMessage(Envelop envelop) {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build();
		return marshaller.encode(envelop, options);
	}

	private <T extends GenericEntity> void deleteDupes(PersistenceGmSession session, Map<String, T> resources) {
		final List<Resource> entries = session.query().entities(PERSISTED_SELECT_QUERY).list();
		logger.info(externalId + "Found: " + entries.size() + "test entries");
		resources.values().forEach(r -> {
			if (r instanceof Resource) {
				entries.remove(r);
			}
		});
		entries.forEach(session::deleteEntity);
		session.commit();
		logger.info(() -> externalId + "Deleted: " + entries.size() + " test entries.");
	}

	private void persistStringToFile(PersistenceGmSession session, String messageString) {
		//@formatter:off
		session.resources().create()
				.name(PERSISTED_JSON)
				.mimeType("application/json")
				.store(() -> new ByteArrayInputStream(messageString.getBytes()));
		//@formatter:on
		checkFileWasCreated(session);
	}

	private void persistResource(PersistenceGmSession session, Message message) {
		List<GenericEntity> values = new ArrayList<>(message.getValues().values());
		values.forEach(v -> {
			if (v instanceof Resource r && !message.getResourceMapping().isEmpty()) {
				try {
					DuplexStreamProvider streamProvider = TempFileInputStreamProviders.create("resourceInputstreamProvider");
					CallStreamCapture capture = CallStreamCapture.T.create();
					capture.setOutputStreamProvider(streamProvider);
					StreamBinary req = StreamBinary.T.create();
					Resource binaryResource = message.shouldPersist(r)
							? message.getPersistedResourceSilent(message.getResourceMapping().keySet().iterator().next())
							: r;

					req.setResource(binaryResource);
					req.setCapture(capture);
					req.setDomainId("cortex");
					req.setServiceId("binaryProcessor.fileSystem");
					StreamBinaryResponse response = req.eval(session).get();

					Resource store = session.resources().create().name(PERSISTED_FILE).mimeType(((Resource) v).getMimeType()).store(streamProvider);
					logger.info(externalId + ". File was successfully stored.");

				} catch (IOException e) {
					logger.error("Could not read stream from Resource. " + e.getMessage());
				}

				checkFileWasCreated(session);
			}
		});
	}

	private void checkFileWasCreated(PersistenceGmSession session) {
		List<Resource> entries = session.query().entities(PERSISTED_SELECT_QUERY).list();
		if (entries.size() == 1) {
			logger.info(() -> externalId + "Successfully persisted test resource " + entries.iterator().next().getName());
		} else {
			logger.error(() -> externalId + "Unable to perform test resource persistence: "
					+ (entries.isEmpty() ? "Did not create resource" : "Several resources present after creation!"));
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor deployable) {
		this.deployable = deployable;
		this.externalId = "[" + deployable.getExternalId() + "] ";
	}

	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
