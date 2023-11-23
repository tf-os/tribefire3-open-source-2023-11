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
package com.braintribe.model.processing.sp.invocation;

import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessIdentificationLookup;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRuleSet;
import com.braintribe.model.processing.sp.commons.ProcessStateChangeContextImpl;
import com.braintribe.model.spapi.StateChangeProcessorInvocation;
import com.braintribe.model.spapi.StateChangeProcessorInvocationPacket;

/**
 * an abstract implementation of the invocation
 * 
 *  handles an invocation of the the type @link StateChangeProcessorInvocation}
 * <br/>
 * requires: <br/>
 * {@link AccessIdentificationLookup} : to retrieve the top-level access behind the id 
 * {@link StateChangeProcessorRuleSet} : to retrieve the rule set behind the id 
 * <br/>
 * 
 * @author pit
 * @author dirk
 *
 */
public abstract class AbstractSpInvocation extends AbstractSpProcessing {
	
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private static Logger log = Logger.getLogger(AbstractSpInvocation.class);
	
	private StateChangeProcessorRuleSet processorRuleSet;
	private PersistenceGmSessionFactory sessionFactory;
	private PersistenceGmSessionFactory systemSessionFactory;
	
	@Required
	public void setProcessorRuleSet(StateChangeProcessorRuleSet processorRuleSet) {
		this.processorRuleSet = processorRuleSet;
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @param systemSessionFactory
	 */
	public void setSystemSessionFactory(
			PersistenceGmSessionFactory systemSessionFactory) {
		this.systemSessionFactory = systemSessionFactory;
	}
	
	/**
	 * actually process the invocation 
	 * 
	 * @param invocation - the {@link StateChangeProcessorInvocation} that contains the information
	 */
	protected void processInvocation( StateChangeProcessorInvocation invocation ) {
		
		String ruleId = invocation.getRuleId();
		String processorId = invocation.getProcessorId();
		try {
			PersistenceGmSession userSession = sessionFactory.newSession(invocation.getAccessId());
			PersistenceGmSession systemSession = systemSessionFactory.newSession(invocation.getAccessId());

			// process invocation
			EntityProperty entityProperty = invocation.getEntityProperty();
			EntityReference entityReference = invocation.getEntityReference();
			EntityType<GenericEntity> entityType = typeReflection.getEntityType(entityReference.getTypeSignature());

			ProcessStateChangeContextImpl<GenericEntity> stateChangeContext = new ProcessStateChangeContextImpl<GenericEntity>(
					userSession, systemSession, invocation.getEntityReference(), entityProperty,
					invocation.getManipulation());
			
			stateChangeContext.setEntityType(entityType);
			
			StateChangeProcessorRule rule = null;
			rule = processorRuleSet.getProcessorRule( ruleId);			
			
			@SuppressWarnings("unchecked")
			StateChangeProcessor<GenericEntity, GenericEntity> processor = (StateChangeProcessor<GenericEntity, GenericEntity>) rule.getStateChangeProcessor( processorId);
			process(ruleId, processorId, processor, stateChangeContext,invocation.getCustomData());
		} catch (StateChangeProcessorException e1) {
			String msg = "cannot find processor rule [" + processorId + "]";
			log.error(msg, e1);
		} catch (GmSessionException e) {
			String msg = "cannot create session for access " + invocation.getAccessId();
			log.error(msg, e);
		}
	}
	
	protected void processInvocationPacket(StateChangeProcessorInvocationPacket invocationPacket) {
		List<StateChangeProcessorInvocation> invocations = invocationPacket.getInvocations();
		
		if (invocations != null) {
			for (StateChangeProcessorInvocation invocation: invocations) {
				// process  
				processInvocation(invocation);
			}
		}
	}
}
