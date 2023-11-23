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
package com.braintribe.model.generic.reflection.cloning;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.cloning.model.City;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;

/**
 * @author peter.gazdik
 */
public class SimpleCloning_Standard_Test extends SimpleCloning_Base {

	@Test
	@Override
	public void simplyCopying() {
		cc = new StandardCloningContext();

		runSimplyCopying();
	}

	@Test
	@Override
	public void copyOnASession() {
		GmSession session = new BasicNotifyingGmSession();

		cc = new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return session.create(entityType);
			}
		};

		runCopyOnASession(session);
	}

	@Test
	@Override
	public void doNotCopyIdStuff() {
		cc = new StandardCloningContext() {
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {

				return !property.isIdentifying() && !property.isGlobalId();
			}
		};

		runDoNotCopyIdStuff();
	}

	@Test
	@Override
	public void referenceOriginalPropertyValue() {
		cc = new StandardCloningContext() {
			@Override
			public <T> T getAssociated(GenericEntity e) {
				return e instanceof City ? (T) e : super.getAssociated(e);
			}
		};

		runReferenceOriginalPropertyValue();
	}

	@Test
	@Override
	public void stringifyIdInPreProcess() {
		cc = new StandardCloningContext() {
			@Override
			public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
				return stringifyId(instanceToBeCloned);
			}
		};

		runStringifyIdInPreProcess();
	}

	@Test
	@Override
	public void stringifyIdInPostProcess() {
		cc = new StandardCloningContext() {
			@Override
			public Object postProcessCloneValue(GenericModelType propertyOrElementType, Object o) {
				return o instanceof Long ? "" + o : o;
			}
		};

		runStringifyIdInPostProcess();
	}

}
