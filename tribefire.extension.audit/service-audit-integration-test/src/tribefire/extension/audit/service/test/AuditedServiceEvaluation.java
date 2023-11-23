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
package tribefire.extension.audit.service.test;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.utils.genericmodel.GmTools;

import tribefire.extension.audit.model.ServiceAuditRecord;
import tribefire.extension.audit.model.deployment.meta.AuditPreservationDepth;

public class AuditedServiceEvaluation<S extends DomainRequest, R> {
	private PersistenceGmSession dataSession;
	private EntityType<S> type;
	private S request;
	private Maybe<R> responseMaybe;
	private boolean recordExpected = true;
	private EntityType<? extends Reason> expectedReason; 
	
	private Consumer<R> responseValidator = r -> {};
	private Consumer<S> requestEnricher = r -> {};
	private Consumer<ServiceAuditRecord> auditRecordValidator = r -> {};
	
	private BiConsumer<Maybe<R>, Maybe<R>> preservedResultValidator = (pr, or) -> {};
	private BiConsumer<S, S> preservedRequestValidator = (ps, os) -> {};
	
	private AuditPreservationDepth requestPreservation;
	private AuditPreservationDepth resultPreservation;
	
	private ServiceAuditRecord auditRecord;
	
	public AuditedServiceEvaluation(PersistenceGmSession dataSession, EntityType<S> type) {
		this.dataSession = dataSession;
		this.type = type;
	}

	public AuditedServiceEvaluation<S, R> requestEnricher(Consumer<S> requestEnricher) {
		this.requestEnricher = requestEnricher;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> responseValidator(Consumer<R> responseValidator) {
		this.responseValidator = responseValidator;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> auditRecordValidator(Consumer<ServiceAuditRecord> auditRecordValidator) {
		this.auditRecordValidator = auditRecordValidator;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> recordExpected(boolean recordExpected) {
		this.recordExpected = recordExpected;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> expectedReason(EntityType<? extends Reason> expectedReason) {
		this.expectedReason = expectedReason;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> requestPreservation(AuditPreservationDepth requestPreservation) {
		this.requestPreservation = requestPreservation;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> resultPreservation(AuditPreservationDepth resultPreservation) {
		this.resultPreservation = resultPreservation;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> preservedRequestValidator(BiConsumer<S, S> preservedRequestValidator) {
		this.preservedRequestValidator = preservedRequestValidator;
		return this;
	}
	
	public AuditedServiceEvaluation<S, R> preservedResultValidator(BiConsumer<Maybe<R>, Maybe<R>> preservedResultValidator) {
		this.preservedResultValidator = preservedResultValidator;
		return this;
	}
	
	public void evaluate() {
		Date start = new Date();
		
		request = type.create();
		request.setDomainId(dataSession.getAccessId());
		requestEnricher.accept(request);
		
		responseMaybe = (Maybe<R>)request.eval(dataSession).getReasoned();
		
		if (responseMaybe.isUnsatisfied()) {
			if (expectedReason != null) {
				if (!expectedReason.isInstance(responseMaybe.whyUnsatisfied())) {
					Assertions.fail("Expected Reason of type " + expectedReason.getTypeSignature() + " but got " +  responseMaybe.whyUnsatisfied().entityType().getTypeSignature());
				}
			}
			else {
				Assertions.fail("Person with id [one] should have been returned but instead we got a reason: " + responseMaybe.whyUnsatisfied().stringify());
			}
		}
		else {
			if (expectedReason != null) {
				Assertions.fail("Missing expected Reason " + expectedReason.getTypeSignature());
			}
			else {
				R response = responseMaybe.get();
				responseValidator.accept(response);
			}
		}
		
		Date end = new Date();
		
		auditRecord = dataSession.query().entities(ServiceAuditTestEntityQueries.latestAuditRecord(start, end).limit(1)).first();
		
		validateAuditRecord();
	}
	
	private void validateAuditRecord() {
		if (recordExpected) {
			validateCommonScalars();
			auditRecordValidator.accept(auditRecord);
			validatePreservations();
		}
		else {
			Assertions.assertThat(auditRecord).as("Found unexpected ServiceAuditRecord").isNull();
		}
	}

	private void validateCommonScalars() {
		Assertions.assertThat(auditRecord).as("Missing ServiceAuditRecord").isNotNull();

		// output request:response association for analysis purposes
		System.out.println(request.entityType().getTypeSignature() + ": " +  GmTools.getDescription(auditRecord));

		// check common attribute ServiceAuditRecord.date is already checked by TestAuditRecording.getLatestAuditRecord
		
		Assertions.assertThat(auditRecord.getRequestType()).as("Recorded requestType is not matching").isEqualTo(type.getTypeSignature());
		Assertions.assertThat(auditRecord.getDomainId()).as("Unexpected ServiceAuditRecord.domainId").isEqualTo(dataSession.getAccessId());
		Assertions.assertThat(auditRecord.getSatisfied()).as("Unexpected ServiceAuditRecord.satisfied").isEqualTo(expectedReason == null);
		
		Assertions.assertThat(auditRecord.getExecutionTimeInMs()).as("Missing ServiceAuditRecord.executionTime").isNotNull();
		Assertions.assertThat(auditRecord.getCallId()).as("Missing ServiceAuditRecord.callId").isNotNull();
		Assertions.assertThat(auditRecord.getParentCallId()).as("ServiceAuditRecord.parentCallId should be different to ServiceAuditRecord.callId").isNotEqualTo(auditRecord.getCallId());
		
		Assertions.assertThat(auditRecord.getUser()).as("Unexpected ServiceAuditRecord.user").isEqualTo(dataSession.getSessionAuthorization().getUserId());
		Assertions.assertThat(auditRecord.getUserIpAddress()).as("Missing ServiceAuditRecord.userIpAddress").isNotNull();
	}
	
	private void validatePreservations() {
		Resource requestResource = auditRecord.getRequest();
		Resource resultResource = auditRecord.getResult();
		
		if (requestPreservation != null) {
			Assertions.assertThat(requestResource).as("Missing ServiceAuditRecord.request").isNotNull();
			checkRequestPreservation(requestResource);
		}
		else {
			Assertions.assertThat(requestResource).as("Unexpected ServiceAuditRecord.request").isNull();
		}
		
		if (resultPreservation != null) {
			Assertions.assertThat(resultResource).as("Missing ServiceAuditRecord.result").isNotNull();
			checkResultPreservation(resultResource);
		}
		else {
			Assertions.assertThat(resultResource).as("Unexpected ServiceAuditRecord.result").isNull();
		}
	}

	private void checkRequestPreservation(Resource resource) {
		S preservedRequest = unmarshallPreservation(resource);
		preservedRequestValidator.accept(preservedRequest, request);
	}
	
	private void checkResultPreservation(Resource resource) {
		final Maybe<R> preservedResultMaybe;
		
		if (responseMaybe.isUnsatisfied()) {
			Unsatisfied unsatisfied = unmarshallPreservation(resource);
			preservedResultMaybe = unsatisfied.toMaby();
		}
		else {
			R response = unmarshallPreservation(resource);
			preservedResultMaybe = Maybe.complete(response);
		}
		
		preservedResultValidator.accept(preservedResultMaybe, responseMaybe);
	}
	
	private <T> T unmarshallPreservation(Resource resource) {
		Assertions.assertThat(resource.getMimeType()).as("Resource has wrong mimetype").isEqualTo("application/json");
		
		try (InputStream in = resource.openStream()) {
			return (T)new JsonStreamMarshaller().unmarshall(in);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
