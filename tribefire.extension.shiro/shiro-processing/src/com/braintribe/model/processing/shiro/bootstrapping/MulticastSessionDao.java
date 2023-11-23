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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.StillProcessing;
import com.braintribe.model.shiro.service.dist.DeleteSession;
import com.braintribe.model.shiro.service.dist.GetSession;
import com.braintribe.model.shiro.service.dist.SerializedSession;
import com.braintribe.model.shiro.service.dist.UpdateSession;
import com.braintribe.utils.Base64;
import com.braintribe.utils.RandomTools;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

public class MulticastSessionDao extends CachingSessionDAO {

	private static final Logger logger = Logger.getLogger(MulticastSessionDao.class);

	public static MulticastSessionDao INSTANCE;

	protected ConcurrentHashMap<String, Session> sessionStore = new ConcurrentHashMap<>();
	protected Evaluator<ServiceRequest> requestEvaluator;
	protected String instanceIdAsString;

	@Override
	protected void doUpdate(Session session) {
		INSTANCE.doUpdateInternal(session);
	}

	@Override
	protected void doDelete(Session session) {
		INSTANCE.doDeleteInternal(session);
	}

	@Override
	protected Serializable doCreate(Session session) {
		return INSTANCE.doCreateInternal(session);
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		return INSTANCE.doReadSessionInternal(sessionId);
	}

	protected void doUpdateInternal(Session session) {
		if (session == null) {
			return;
		}
		String id = session.getId().toString();
		if (sessionStore.containsKey(id)) {
			logger.debug(() -> "Updating local session with ID: " + id);
			sessionStore.put(id, session);
		} else {
			logger.debug(() -> "Sending an UpdateSession request with ID: " + id + " (local: " + instanceIdAsString + ")");
			UpdateSession upd = UpdateSession.T.create();
			upd.setShiroSessionId(id);
			String b64 = serialize(session);
			upd.setSerializedSession(b64);
			MulticastRequest mc = MulticastRequest.T.create();
			mc.setServiceRequest(upd);
			mc.eval(requestEvaluator).get();
		}
	}

	protected void doDeleteInternal(Session session) {
		if (session == null) {
			return;
		}
		String id = session.getId().toString();
		if (sessionStore.containsKey(id)) {
			logger.debug(() -> "Removing local session with ID: " + id);
			sessionStore.remove(id);
		} else {
			logger.debug(() -> "Sending a DeleteSession request with ID: " + id + " (local: " + instanceIdAsString + ")");
			DeleteSession del = DeleteSession.T.create();
			del.setShiroSessionId(id);
			MulticastRequest mc = MulticastRequest.T.create();
			mc.setServiceRequest(del);
			mc.eval(requestEvaluator).get();
		}
	}

	protected Serializable doCreateInternal(Session session) {
		if (session == null) {
			return null;
		}
		Serializable id = session.getId();
		if (id == null) {
			id = RandomTools.newStandardUuid() + "@" + instanceIdAsString;
			if (session instanceof SimpleSession) {
				((SimpleSession) session).setId(id);
			}
		}
		logger.debug("Storing local session with ID: " + id.toString());
		sessionStore.put(id.toString(), session);
		return id;
	}

	protected Session doReadSessionInternal(Serializable id) {
		if (id == null) {
			return null;
		}

		if (sessionStore.containsKey(id)) {
			logger.debug(() -> "Reading local session with ID: " + id);
			return sessionStore.get(id);
		} else {
			logger.debug(() -> "Sending a GetSession request with ID: " + id + " (local: " + instanceIdAsString + ")");
			GetSession get = GetSession.T.create();
			get.setShiroSessionId(id.toString());
			MulticastRequest mc = MulticastRequest.T.create();
			mc.setServiceRequest(get);
			mc.setTimeout((long) Numbers.MILLISECONDS_PER_SECOND * 20);
			MulticastResponse multicastResponse = mc.eval(requestEvaluator).get();

			Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
			for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

				InstanceId instanceId = entry.getKey();

				logger.trace(() -> "Received a response from instance: " + instanceId);

				ServiceResult serviceResult = entry.getValue();
				SerializedSession serSession = extractFromServiceResult(serviceResult);
				if (serSession != null) {
					String b64 = serSession.getSerializedSession();
					if (b64 != null) {
						logger.debug(() -> "Received a remote session with ID: " + id + " from " + instanceId);
						byte[] bytes = Base64.decode(b64);
						Session deserializedSession = SerializationUtils.deserialize(bytes);
						return deserializedSession;
					}
				}

			}
		}
		logger.debug(() -> "Received no remote session with ID: " + id);
		return null;
	}

	protected SerializedSession extractFromServiceResult(ServiceResult serviceResult) {
		if (serviceResult == null) {
			return null;
		}
		if (serviceResult instanceof ResponseEnvelope) {
			ResponseEnvelope envelope = (ResponseEnvelope) serviceResult;
			return (SerializedSession) envelope.getResult();
		} else if (serviceResult instanceof Failure) {
			return null;
		} else if (serviceResult instanceof StillProcessing) {
			return null;
		} else {
			logger.debug("Unsupported ServiceResult type: " + serviceResult);
			return null;
		}
	}

	public String getLocalSessionSerialized(String id) {
		Session session = sessionStore.get(id);
		if (session == null) {
			return null;
		}
		return serialize(session);
	}

	private static Session deserialize(String serializedB64) {
		try {
			byte[] bytes = Base64.decode(serializedB64);
			Session session = SerializationUtils.deserialize(bytes);
			return session;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not deserialize Session");
		}
	}
	private static String serialize(Session session) {
		String id = session.getId().toString();
		try {
			Collection<Object> attributeKeys = session.getAttributeKeys();
			if (attributeKeys != null) {
				for (Object key : attributeKeys) {
					Object attribute = session.getAttribute(key);
					if (attribute instanceof CodeVerifier && !(attribute instanceof SerializableCodeVerifier)) {
						SerializableCodeVerifier scv = SerializableCodeVerifier.create((CodeVerifier) attribute);
						session.setAttribute(key, scv);
						break;
					}
				}
			}
			byte[] serialized = SerializationUtils.serialize((Serializable) session);
			String b64 = Base64.encodeBytes(serialized);
			return b64;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not serialize Session with ID: " + id);
		}
	}

	public boolean updateIfExists(String id, String serializedSession) {
		if (!sessionStore.containsKey(id)) {
			return false;
		}
		Session session = deserialize(serializedSession);
		sessionStore.put(id, session);
		return true;
	}

	public boolean deleteIfExists(String id) {
		return (sessionStore.remove(id) != null);
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Configurable
	@Required
	public void setInstanceIdAsString(String instanceIdAsString) {
		this.instanceIdAsString = instanceIdAsString;
	}

}
