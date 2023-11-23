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
package tribefire.extension.cache.util;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class CloningContextUtil {

	/**
	 * Create a {@link StandardCloningContext} that clones in another session. ATTENTION: this method is also cloning id
	 * and globalId!
	 * 
	 * @param targetSession
	 *            session to which to be cloned - target session
	 * @return {@link CloningContext}
	 */
	public static CloningContext createStandardCloningContextIntoSession(PersistenceGmSession targetSession) {
		CloningContext cloningContext = new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return targetSession.create(entityType);
			}
		};
		return cloningContext;
	}

	/**
	 * Create a {@link StandardCloningContext} that clones into a transient entity. ATTENTION: this method is also
	 * cloning id and globalId!
	 * 
	 * @return {@link CloningContext}
	 */
	public static CloningContext createStandardCloningContextIntoSession() {
		CloningContext cloningContext = new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return entityType.create();
			}
		};
		return cloningContext;
	}

	/**
	 * Create a {@link StandardCloningContext} that clones in another session.
	 * 
	 * @param targetSession
	 *            session to which to be cloned - target session
	 * @return {@link CloningContext}
	 */
	public static CloningContext createStandardCloningContextIntoSessionWithoutIdAndGlobalId(PersistenceGmSession targetSession) {
		CloningContext cloningContext = new StandardCloningContext() {

			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				// covers id, partition and gloabalId
				return !property.isIdentifying() && !property.isGlobalId();
			}

			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return targetSession.create(entityType);
			}
		};
		return cloningContext;
	}

	/**
	 * Create a {@link StandardCloningContext} that clones into a transient entitiy.
	 * 
	 * @return {@link CloningContext}
	 */
	public static CloningContext createStandardCloningContextIntoSessionWithoutIdAndGlobalId() {
		CloningContext cloningContext = new StandardCloningContext() {

			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				// covers id, partition and gloabalId
				return !property.isIdentifying() && !property.isGlobalId();
			}

			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return entityType.create();
			}
		};
		return cloningContext;
	}
}
