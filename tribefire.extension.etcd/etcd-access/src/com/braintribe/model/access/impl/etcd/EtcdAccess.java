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
package com.braintribe.model.access.impl.etcd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;

/**
 * Minimalistic Access implementation that uses etcd as the backend. It is intended to act as an Access for user
 * sessions and is therefore optimized for this usage. It may be used for other purposes but has not been tested.
 * 
 * 
 * @author Roman Kurmanowytsch
 */
public class EtcdAccess extends BasicAccessAdapter implements DestructionAware {

	protected Supplier<PersistenceGmSession> sessionProvider = null;

	protected String project = "";

	protected Supplier<Client> clientSupplier;

	protected HasStringCodec marshaller;

	protected EtcdProcessing processing;

	protected void connect() {
		if (processing == null) {
			synchronized (this) {
				if (processing == null) {

					processing = new EtcdProcessing(clientSupplier);
					processing.connect();
				}
			}
		}
	}

	protected String getKey(String id) {
		return project.concat("/access/").concat(super.getAccessId()).concat("/").concat(id);
	}

	@Override
	protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
		return super.queryPopulation(typeSignature);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {
		connect();

		String queriedId = idQuery(request);
		if (queriedId != null) {

			GetResponse response;
			try {
				response = processing.get(getKey(queriedId));
			} catch (Exception e) {
				throw new ModelAccessException("Could not get value for " + queriedId, e);
			}
			EntityQueryResult result = EntityQueryResult.T.create();
			String responseValue = EtcdProcessing.getResponseValue(response);
			if (responseValue != null) {
				GenericEntity ge = (GenericEntity) marshaller.getStringCodec().decode(responseValue);
				result.getEntities().add(ge);
			}
			return result;
		} else {
			return super.queryEntities(request);
		}
	}

	protected String idQuery(EntityQuery request) {
		if (request == null) {
			return null;
		}
		Restriction restriction = request.getRestriction();
		if (restriction == null) {
			return null;
		}
		Condition condition = restriction.getCondition();
		if (!(condition instanceof ValueComparison)) {
			return null;
		}
		ValueComparison vc = (ValueComparison) condition;

		if (!(vc.getLeftOperand() instanceof PropertyOperand)) {
			return null;
		}
		if (!(vc.getRightOperand() instanceof String)) {
			return null;
		}
		if (!vc.getOperator().equals(Operator.equal)) {
			return null;
		}

		String entityTypeSignature = request.getEntityTypeSignature();
		if (StringTools.isBlank(entityTypeSignature)) {
			return null;
		}
		String propName = ((PropertyOperand) vc.getLeftOperand()).getPropertyName();

		if (entityTypeSignature.equals("com.braintribe.model.usersession.UserSession")) {

			String sessionId = (String) vc.getRightOperand();
			if (propName.equals("sessionId")) {
				return sessionId;
			}

		} else {

			if (propName.equals("id") && (vc.getRightOperand() instanceof String)) {
				String id = (String) vc.getRightOperand();
				return id;
			}
		}

		return null;
	}

	@Override
	protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {
		connect();

		Map<String, String> map = null;
		try {
			List<String> keys = processing.getAllKeysWithPrefix(getKey(""));
			map = processing.getAllEntries(keys);
		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to load the population with prefix " + project, e);
		}
		List<GenericEntity> result = new ArrayList<>();

		if (map != null) {
			for (String value : map.values()) {
				GenericEntity ge = (GenericEntity) marshaller.getStringCodec().decode(value);
				result.add(ge);
			}
		}
		return result;
	}

	@Override
	protected Map<EntityReference, GenericEntity> loadEntitiesByType(Set<EntityReference> entityReferences, BasicPersistenceGmSession session)
			throws Exception {
		return super.loadEntitiesByType(entityReferences, session);
	}

	protected void store(GenericEntity ge) throws ModelAccessException {
		Object idObject = ge.getId();
		String id = null;
		if (idObject == null) {
			if (GMF.getTypeReflection().getType(ge).getTypeSignature().equals("com.braintribe.model.usersession.UserSession")) {
				UserSession us = (UserSession) ge;
				id = us.getSessionId();
				if (id != null) {
					ge.setId(id);
				}
			}
			if (id == null) {
				id = RandomTools.getRandom32CharactersHexString(true);
				ge.setId(id);
			}
		} else {
			id = idObject.toString();
		}

		ge.setGlobalId(id);
		ge.setPartition(super.defaultPartition());

		String encoded = marshaller.getStringCodec().encode(ge);

		try {
			processing.put(getKey(id), encoded, -1);
		} catch (Exception e) {
			throw new ModelAccessException("Could not write " + ge + " to etcd.", e);
		}
	}

	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {

		connect();

		Set<GenericEntity> created = context.getCreatedEntities();
		if (created != null) {
			for (GenericEntity ge : created) {
				store(ge);
			}
		}
		Set<GenericEntity> deleted = context.getDeletedEntities();
		if (deleted != null) {
			for (GenericEntity ge : deleted) {
				try {
					processing.delete(getKey(ge.getId()));
				} catch (Exception e) {
					throw new ModelAccessException("Could not delete entity " + ge, e);
				}
			}
		}
		Set<GenericEntity> updated = context.getUpdatedEntities();
		if (updated != null) {
			for (GenericEntity ge : updated) {
				store(ge);
			}
		}

	}

	@Configurable
	@Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Configurable
	@Required
	public void setProject(String project) {
		this.project = project;
	}
	@Configurable
	@Required
	public void setMarshaller(HasStringCodec marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public void preDestroy() {
		if (processing != null) {
			processing.preDestroy();
		}
	}

	@Configurable
	@Required
	public void setClientSupplier(Supplier<Client> clientSupplier) {
		this.clientSupplier = clientSupplier;
	}

}
