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
package tribefire.extension.messaging.service.utils;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;

import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerDiffEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;

public class MessageComposer {
	private static final Logger logger = Logger.getLogger(MessageComposer.class);

	public static Message createMessage(ProducerEventRule rule, ServiceRequest request, Object response,
			List<GenericEntity> before, List<GenericEntity> after, ServiceRequestContext requestContext) {
		Message message = Message.T.create();
		message.setResourceBinaryPersistence(rule.getFilePersistenceStrategy());
		message.setKey(
				LocalizedString.T.create().putDefault(rule.getName() + ": automatic messaging from: " + new Date()));

		if (rule.requiresRequest()) {
			addToMessage("request", message, request, rule);
		}

		if (rule.requiresDiff() || rule.requiresResponse()) {
			addDiff(rule, before, after, message);
			addResponse(rule, response, message);
		}

		if (rule.requiresUserInfo()) {
			message.addValue("user", getUser(requestContext));
		}

		return message;
	}

	public static User getUser(ServiceRequestContext requestContext) {
		User user = null;
		Optional<UserSession> userSessionOpt = requestContext.getAspect(UserSessionAspect.class);
		if (userSessionOpt.isPresent()) {
			UserSession userSession = userSessionOpt.get();
			if (userSession.getUser() != null) {
				user = userSession.getUser();
			}
		}
		return user;
	}

	private static void addToMessage(String key, Message message, GenericEntity o, ProducerEventRule rule) {
		GenericEntity cleanEntry = (GenericEntity) EntryCleaner.cleanEntry(rule.getPropertiesToIncludeByType(o), o);
		message.addValue(key, cleanEntry);
	}

	private static void addDiff(ProducerEventRule rule, List<GenericEntity> before, List<GenericEntity> after,
			Message message) {
		if (rule instanceof ProducerDiffEventRule r) {
			GenericEntity entity;
			if (!before.isEmpty()) {
				entity = before.get(0);
			} else {
				entity = after.get(0);
			}

			String extractionPath = r.getExtractionPathMatchingByType(entity);

			PropertyVisitor visitor = new PropertyVisitor();
			if (extractionPath != null && !extractionPath.isBlank()) {
				before = before.stream().map(b -> visitor.visit(b, extractionPath)).map(b -> (GenericEntity) b)
						.toList();
				after = after.stream().map(a -> visitor.visit(a, extractionPath)).map(a -> (GenericEntity) a).toList();
			}

			if (!before.isEmpty()) {
				entity = before.get(0);
			} else {
				entity = after.get(0);
			}

			Set<String> propertiesToInclude = r.getPropertiesToIncludeByType(entity);

			if (propertiesToInclude.isEmpty()) {
				String msg = format("No fieldsToInclude mapped for type %s found! Please consider adding some!",
						before.get(0).entityType().getTypeSignature());
				logger.error(msg);
				throw new UnsatisfiedMaybeTunneling(Reasons.build(ArgumentNotSatisfied.T).text(msg).toMaybe());

			}

			ComparisonResult comparisonResult = new PropertyByProperty(r.getDiffType(), r.getListedPropertiesOnly(),
					propertiesToInclude, r.getAddEntries()).checkEquality(before, after);
			message.addValue("diff", comparisonResult);
		}
	}

	private static void addResponse(ProducerEventRule rule, Object response, Message message) {
		if (rule.requiresResponse()) {
			if (response instanceof UnsatisfiedMaybeTunneling ex) {
				addToMessage("reason", message, ex.whyUnsatisfied(), rule);
			} else if (response instanceof GenericEntity e) {
				addToMessage("response", message, e, rule);
			} else {
				Logger.getLogger(Message.class).warn(String.format(
						"Skipped sending of message due to response: %s is not being a subclass of GenericEntity",
						Optional.ofNullable(response).map(Object::getClass).orElse(null)));
			}
		}
	}

	private MessageComposer() {
	}
}
