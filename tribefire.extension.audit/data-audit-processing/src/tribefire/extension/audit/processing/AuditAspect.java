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
package tribefire.extension.audit.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.NormalizedCompoundManipulation;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/*
 * TODO:
 * 
 * Fix Payload Preliminary References
 * 
 * OK: Add order number to specify order of events (maybe: add milliseconds, nanoseconds to date)
 * 
 * OK: Add cleanup mechanism
 * 
 * OK Check why Ids are not in correct order
 * 
 * OK: extension of manipulation record (e.g. IP address)
 * 
 * OK: alter functionality to allow to track ALL except for designated entity types
 * 
 * 
 */
public class AuditAspect implements AccessAspect {
	private static final Logger logger = Logger.getLogger(AuditAspect.class);

	private Supplier<PersistenceGmSession> auditSessionProvider;
	private Supplier<Set<String>> userRolesProvider;
	private Set<String> untrackedRoles;

	protected ManipulationRecordCreator manipulationRecordCreator;

	protected boolean active = true;

	@Override
	public void configurePointCuts(PointCutConfigurationContext context) {
		try {

			if (!this.active) {
				return;
			}

			context.addPointCutBinding(AccessJoinPoint.applyManipulation, new AuditInterceptor());

		} catch (Exception e) {
			throw new RuntimeException("Configuring PointCuts failed.", e);
		}
	}

	private class AuditInterceptor implements AroundInterceptor<ManipulationRequest, ManipulationResponse> {

		@Override
		public ManipulationResponse run(AroundContext<ManipulationRequest, ManipulationResponse> context) throws InterceptionException {
			if (AuditAspect.this.active == false) {
				try {
					return context.proceed();
				} catch (Exception e) {
					throw new InterceptionException("Error while executing the context call.", e);
				}
			}

			boolean doNotTrack = false;
			try {
				if (AuditAspect.this.userRolesProvider != null && AuditAspect.this.untrackedRoles != null) {
					Set<String> roles = AuditAspect.this.userRolesProvider.get();
					if ((roles != null) && (roles.size() > 0)) {

						Set<String> intersection = new HashSet<String>(roles);
						intersection.retainAll(AuditAspect.this.untrackedRoles);
						if (intersection.size() > 0) {
							if (logger.isTraceEnabled()) {
								logger.trace("The current user has the role(s) " + intersection + ". Therefore not tracking the manipulation.");
							}
							doNotTrack = true;
						}
					}
				}
			} catch (Exception e) {
				throw new InterceptionException("Error while trying to compare user roles with untrackedRoles", e);
			}

			if (doNotTrack) {
				return context.proceed();
			}

			Manipulation appliedManipulation = null;
			Manipulation inducedManipulation = null;
			ManipulationResponse response = null;

			appliedManipulation = context.getRequest().getManipulation();

			NormalizedCompoundManipulation normalizedManipulation = Normalizer.normalize(appliedManipulation);

			TypeUsageInfoIndex typeUsageInfoIndex = new TypeUsageInfoIndex(context.getSession());

			normalizedManipulation.inline().forEach(typeUsageInfoIndex::register);

			typeUsageInfoIndex.onRegistrationCompleted();

			// TODO track erroneous attempts as well (maybe?)
			response = context.proceed();
			inducedManipulation = response.getInducedManipulation();

			try {
				PersistenceGmSession session = openAuditSession(context);
				manipulationRecordCreator.createRecords(typeUsageInfoIndex, session, normalizedManipulation, inducedManipulation, context);
			} catch (Exception e) {
				logger.error("Error while trying to record the manipulations or post-processing the record creation.", e);
			}
			return response;
		}

		private PersistenceGmSession openAuditSession(AroundContext<ManipulationRequest, ManipulationResponse> context) {
			if (auditSessionProvider != null) {
				return auditSessionProvider.get();
			} else {
				return context.getSystemSession().newEquivalentSession();
			}
		}
	}

	/* Getters and Setters */

	@Required
	@Configurable
	public void setManipulationRecordCreator(ManipulationRecordCreator manipulationRecordCreator) {
		this.manipulationRecordCreator = manipulationRecordCreator;
	}

	@Configurable
	public void setUntrackedRoles(Set<String> untrackedRoles) {
		this.untrackedRoles = untrackedRoles;
	}

	@Configurable
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	@Configurable
	@Required
	public void setAuditSessionProvider(Supplier<PersistenceGmSession> auditSessionProvider) {
		this.auditSessionProvider = auditSessionProvider;
	}

	@Configurable
	public void setActive(boolean active) {
		this.active = active;
	}
}
