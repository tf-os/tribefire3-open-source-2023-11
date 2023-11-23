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
package com.braintribe.model.processing.query.smart.test.model;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.smart.test.model.accessA.Address;
import com.braintribe.model.processing.query.smart.test.model.accessA.AllPurposeDelegateEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessA.business.CustomerA;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.accessA.discriminator.DiscriminatorEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.shared.SourceOwnerA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.BookA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderBookLink;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderBookSetLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.EnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonId2UniqueEntityLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemOrderedLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemSetLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.JdeInventoryB;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.SapInventoryB;
import com.braintribe.model.processing.query.smart.test.model.accessB.shared.SharedSourceOwnerB;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test.model.smart.AllPurposeSmartEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartEnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.business.Customer;
import com.braintribe.model.processing.query.smart.test.model.smart.business.JdeInventory;
import com.braintribe.model.processing.query.smart.test.model.smart.business.SapInventory;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorBase;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2;
import com.braintribe.model.processing.query.smart.test.model.smart.shared.SmartSourceOwnerA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;

/**
 * @author peter.gazdik
 */
public class EntityList {

	// @formatter:off
	public static final List<EntityType<?>> accessA = asList (
			PersonA.T, 
			CompanyA.T,
			Address.T,
			CarA.T,
			FlyingCarA.T,

			BookA.T,
			ManualA.T,
			ReaderA.T,
			ReaderBookLink.T,
			ReaderBookSetLink.T,
			
			ConstantPropEntitySubA.T,
			ConstantPropEntityA2.T,
			
			CustomerA.T,
			CompositeKpaEntityA.T,
			CompositeIkpaEntityA.T,
			
			DiscriminatorEntityA.T,

			AllPurposeDelegateEntity.T,
			
			SourceOwnerA.T,
			SharedFile.T
	);
			
			
	public static final List<EntityType<?>> accessB = asList (
			ItemB.T,
			PersonB.T,
			StandardIdEntity.T,
			EnumEntityB.T,

			PersonItemLink.T,
			PersonItemSetLink.T,
			PersonItemOrderedLink.T,
			
			PersonId2UniqueEntityLink.T,
			
			BookB.T,
			
			JdeInventoryB.T,
			SapInventoryB.T,

			SharedFile.T,		
			SharedSourceOwnerB.T
	);

	public static final List<EntityType<?>> accessS = asList(
			Car.T,
			Company.T,
			FlyingCar.T,
			SmartAddress.T,
			SmartItem.T,
			SmartPersonA.T,
			SmartStringIdEntity.T,
			SmartEnumEntityB.T,

			SmartManualA.T,
			SmartReaderA.T,
			SmartBookB.T,

			SmartConstantPropEntitySubA.T,
			SmartConstantPropEntityA2.T,
			
			Customer.T,
			JdeInventory.T,
			SapInventory.T,

			SmartDiscriminatorBase.T,
			SmartDiscriminatorType1.T,
			SmartDiscriminatorType2.T,

			AllPurposeSmartEntity.T,

			SharedFile.T,		
			SmartSourceOwnerA.T
	);
	// @formatter:on



}
