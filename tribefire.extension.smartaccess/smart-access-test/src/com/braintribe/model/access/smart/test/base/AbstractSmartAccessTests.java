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
package com.braintribe.model.access.smart.test.base;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Before;

import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.smart.test.builder.SmartDataBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.Address;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.EntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.accessA.discriminator.DiscriminatorEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.BookA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.Id2UniqueEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.StandardSmartIdentifiable;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.query.smart.test.setup.BasicSmartSetupProvider;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartSetupProvider;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public abstract class AbstractSmartAccessTests {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected AccessSetup setup;
	protected Smood smoodA, smoodB;
	protected SmartDataBuilder bA, bB;
	protected SmartAccess smartAccess;
	protected ManagedGmSession localSession;

	@Before
	public void setup() throws Exception {
		setup = newAccessSetup();

		smoodA = setup.getAccessA().getDatabase();
		smoodB = setup.getAccessB().getDatabase();

		bA = new SmartDataBuilder(smoodA, accessIdA);
		bB = new SmartDataBuilder(smoodB, accessIdB);

		smartAccess = setup.getSmartAccess();
		localSession = new BasicManagedGmSession();
	}

	protected AccessSetup newAccessSetup() {
		return new AccessSetup(getSmartSetupProvider().setup());
	}
	
	protected SmartSetupProvider getSmartSetupProvider() {
		return BasicSmartSetupProvider.INSTANCE;
	}

	// ######################################################
	// ## . . . . . Instantiating smart entities . . . . . ##
	// ######################################################

	protected SmartAddress smartAddress(Address delegateEntity) {
		return newInstance(SmartAddress.T, delegateEntity);
	}

	protected SmartPersonA smartPerson(PersonA delegateEntity) {
		return newInstance(SmartPersonA.T, delegateEntity);
	}

	protected SmartPersonB smartPerson(PersonB delegateEntity) {
		return newInstance(SmartPersonB.T, delegateEntity);
	}

	protected Company smartCompany(CompanyA delegateEntity) {
		return newInstance(Company.T, delegateEntity);
	}

	protected Car smartCar(CarA delegateEntity) {
		return newInstance(Car.T, delegateEntity);
	}

	protected FlyingCar smartFlyingCar(CarA delegateEntity) {
		return newInstance(FlyingCar.T, delegateEntity);
	}

	protected SmartItem smartItem(ItemB delegateEntity) {
		return newInstance(SmartItem.T, delegateEntity);
	}

	protected SmartBookA smartBookA(BookA delegateEntity) {
		return newInstance(SmartBookA.T, delegateEntity);
	}

	protected SmartBookB smartBookB(BookB delegateEntity) {
		return newInstance(SmartBookB.T, delegateEntity);
	}

	protected SmartManualA smartManualA(ManualA delegateEntity) {
		return newInstance(SmartManualA.T, delegateEntity);
	}

	protected SmartReaderA smartReaderA(ReaderA delegateEntity) {
		return newInstance(SmartReaderA.T, delegateEntity);
	}

	protected CompositeKpaEntity smartCompositeKpa(CompositeKpaEntityA delegateEntity) {
		return newInstance(CompositeKpaEntity.T, delegateEntity);
	}

	protected CompositeIkpaEntity smartCompositeIkpa(CompositeIkpaEntityA delegateEntity) {
		return newInstance(CompositeIkpaEntity.T, delegateEntity);
	}

	protected SmartConstantPropEntityA smartConstantPropEntityA(ConstantPropEntityA delegateEntity) {
		return newInstance(SmartConstantPropEntityA.T, delegateEntity);
	}

	protected SmartConstantPropEntityA2 smartConstantPropEntityA2(ConstantPropEntityA2 delegateEntity) {
		return newInstance(SmartConstantPropEntityA2.T, delegateEntity);
	}

	protected SmartConstantPropEntitySubA smartConstantPropEntitySubA(ConstantPropEntitySubA delegateEntity) {
		return newInstance(SmartConstantPropEntitySubA.T, delegateEntity);
	}

	protected SmartDiscriminatorType1 smartDiscriminatorType1(DiscriminatorEntityA delegateEntity) {
		return newInstance(SmartDiscriminatorType1.T, delegateEntity);
	}

	protected SmartDiscriminatorType2 smartDiscriminatorType2(DiscriminatorEntityA delegateEntity) {
		return newInstance(SmartDiscriminatorType2.T, delegateEntity);
	}

	protected <T extends StandardSmartIdentifiable> T newInstance(EntityType<T> entityType, StandardIdentifiable delegateEntity) {
		String partition = delegateEntity instanceof EntityA ? accessIdA : accessIdB;

		return newInstance(entityType, delegateEntity.getId(), partition);
	}

	protected <T extends StandardIdentifiable> T newInstance(EntityType<T> entityType, Long id, String partition) {
		T result = queryLocal(entityType, id, partition);

		if (result == null) {
			result = localSession.create(entityType);
			result.setId(id);
			result.setPartition(partition);
		}

		return result;
	}

	protected SmartStringIdEntity smartStringIdEntity(StandardIdEntity delegateEntity) {
		String id = "" + delegateEntity.getId();

		SmartStringIdEntity result = queryLocal(SmartStringIdEntity.T, id, accessIdB);

		if (result == null) {
			result = localSession.create(SmartStringIdEntity.T);
			result.setId(id);
			result.setPartition(accessIdB);
		}

		return result;
	}

	private <T extends GenericEntity> T queryLocal(EntityType<T> entityType, Object id, String partition) {
		try {
			PersistentEntityReference ref = VdBuilder.persistentReference(entityType.getTypeSignature(), id, partition);

			return localSession.query().<T> entity(ref).find();

		} catch (GmSessionException e) {
			throw new RuntimeException("Session query failed", e);
		}
	}

	protected Id2UniqueEntity id2UniqueEntity(Id2UniqueEntityA delegateEntity) {
		Id2UniqueEntity result = localSession.create(Id2UniqueEntity.T);
		result.setId(delegateEntity.getUnique());
		result.setPartition(accessIdA);

		return result;
	}

}
