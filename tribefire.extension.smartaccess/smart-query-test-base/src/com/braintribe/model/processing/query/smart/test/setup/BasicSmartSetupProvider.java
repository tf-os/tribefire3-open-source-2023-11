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
package com.braintribe.model.processing.query.smart.test.setup;

import static com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1.DISC_TYPE1;
import static com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2.DISC_TYPE2;

import com.braintribe.model.accessdeployment.smart.meta.PolymorphicBaseEntityAssignment;
import com.braintribe.model.processing.query.smart.test.model.EnumConstantMappingProvider;
import com.braintribe.model.processing.query.smart.test.model.accessA.Address;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessA.VehicleA;
import com.braintribe.model.processing.query.smart.test.model.accessA.business.CustomerA;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA;
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
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemTypeB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonId2UniqueEntityLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemOrderedLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemSetLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.JdeInventoryB;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.SapInventoryB;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedEntity;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedFileDescriptor;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedSource;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.Id2UniqueEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_String;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartEnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.UnmappedSmartEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Vehicle;
import com.braintribe.model.processing.query.smart.test.model.smart.business.Customer;
import com.braintribe.model.processing.query.smart.test.model.smart.business.JdeInventory;
import com.braintribe.model.processing.query.smart.test.model.smart.business.Product;
import com.braintribe.model.processing.query.smart.test.model.smart.business.SapInventory;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorBase;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2;
import com.braintribe.model.processing.query.smart.test.model.smart.shared.SmartSourceOwnerA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartPublicationB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.query.smart.test.setup.base.AbstractSmartSetupProvider;

/**
 * @author peter.gazdik
 */
public class BasicSmartSetupProvider extends AbstractSmartSetupProvider {

	public static final BasicSmartSetupProvider INSTANCE = new BasicSmartSetupProvider();

	private BasicSmartSetupProvider() {
	}

	public static final String DATE_PATTERN = "ddMMyyyy HHmmss";

	@Override
	protected void configureMappings() {
		// @formatter:off
		editor.onEntityType(SmartPersonA.T)
			.addMetaData(qualifiedEntityAssignment(PersonA.T))

			.addPropertyMetaData(asIsForPropertyNameSuffix("A"))

			.addPropertyMetaData("id", asIsProperty())
			.addPropertyMetaData("nickName", qpa(PersonA.T, "nickNameX"))
			.addPropertyMetaData("smartParentB", kpa(cqp(PersonA.T, "parentB"), qp(PersonB.T, "nameB")))
			.addPropertyMetaData("keyCompanyA", kpa(cqp(PersonA.T, "companyNameA", true), qp(CompanyA.T, "nameA", true)))
			.addPropertyMetaData("keyCompanyExternalDqj", external(kpa(cqp(PersonA.T, "companyNameA", true), qp(CompanyA.T, "nameA", true))))
			.addPropertyMetaData("compositeKpaEntity",
					compositeKpa(PersonA.T, CompositeKpaEntityA.T, "compositeId", "personId", "compositeName", "personName", "compositeCompanyName", "personCompanyName"))
			.addPropertyMetaData("compositeKpaEntityExternalDqj",
					external(compositeKpa(PersonA.T, CompositeKpaEntityA.T, "compositeId", "personId", "compositeName", "personName", "compositeCompanyName", "personCompanyName")))
			.addPropertyMetaData("keyCompanySetA", kpa(cqp(PersonA.T, "companyNameSetA"), qp(CompanyA.T, "nameA")))
			.addPropertyMetaData("keyCompanySetExternalDqj", external(kpa(cqp(PersonA.T, "companyNameSetA"), qp(CompanyA.T, "nameA"))))
			.addPropertyMetaData("keyCompanyListA", kpa(cqp(PersonA.T, "companyNameListA"), qp(CompanyA.T, "nameA")))
			.addPropertyMetaData("keyCompanyListExternalDqj", external(kpa(cqp(PersonA.T, "companyNameListA"), qp(CompanyA.T, "nameA"))))
			.addPropertyMetaData("keyFriendEmployerA", kpa(cqp(PersonA.T, "keyFriendEmployerNameA"), qp(CompanyA.T, "nameA")))
			.addPropertyMetaData("keyFriendEmployerExternalDqj", external(kpa(cqp(PersonA.T, "keyFriendEmployerNameA"), qp(CompanyA.T, "nameA"))))
			.addPropertyMetaData("inverseIdKeyCompanyA", external(kpa(cqp(PersonA.T, "id"), qp(CompanyA.T, "ownerIdA"))))
			.addPropertyMetaData("inverseKeyItem", ikpa(cqp(ItemB.T, "singleOwnerName", true), qp(PersonA.T, "nickNameX", true)))
			.addPropertyMetaData("inverseKeySharedItem", ikpa(cqp(ItemB.T, "sharedOwnerNames"), PersonA.T, "nameA"))
			.addPropertyMetaData("compositeIkpaEntity",
					inverseCompositeKpa(PersonA.T, CompositeIkpaEntityA.T, "id", "personId", "nameA", "personName"))
			.addPropertyMetaData("compositeIkpaEntityExternalDqj",
					external(inverseCompositeKpa(PersonA.T, CompositeIkpaEntityA.T, "id", "personId", "nameA", "personName")))
			.addPropertyMetaData("inverseKeyItemSet", ikpa(cqp(ItemB.T, "multiOwnerName"), PersonA.T, "nameA"))
			.addPropertyMetaData("inverseKeyMultiSharedItemSet", ikpa(cqp(ItemB.T, "multiSharedOwnerNames"), PersonA.T, "nameA"))
			.addPropertyMetaData("compositeIkpaEntitySet", inverseCompositeKpa(PersonA.T, CompositeIkpaEntityA.T, "id", "personId_Set", "nameA", "personName_Set"))
			.addPropertyMetaData("compositeIkpaEntitySetExternalDqj",
					external(inverseCompositeKpa(PersonA.T, CompositeIkpaEntityA.T, "id", "personId_Set", "nameA", "personName_Set")))
			.addPropertyMetaData("linkItem", linkPropertyAssignment(PersonA.T, "nameA", ItemB.T, "nameB", PersonItemLink.T, "personName", "itemName"))
			.addPropertyMetaData("linkItems", linkPropertyAssignment(PersonA.T, "nameA", ItemB.T, "nameB", PersonItemSetLink.T, "personName", "itemName"))
			.addPropertyMetaData("orderedLinkItems", orderedLinkPropertyAssignment(PersonA.T, "nameA", ItemB.T, "nameB", PersonItemOrderedLink.T, "personName", "itemName", "itemIndex"))
			.addPropertyMetaData("linkId2UniqueEntityA", linkPropertyAssignment(PersonA.T, "nameA", Id2UniqueEntityA.T, "unique", PersonId2UniqueEntityLink.T, "personName", "linkUnique"))
			.addPropertyMetaData("unmappedParent", unmapped())
			.addPropertyMetaData("unmappedEntity", unmapped())
			;

			editor.onEntityType(SmartPersonB.T)
				.addMetaData(qualifiedEntityAssignment(PersonB.T))

				.addPropertyMetaData("id", asIsProperty())
				.addPropertyMetaData("smartParentA", kpa(cqp(PersonB.T, "parentA"), qp(PersonA.T, "nameA")))
				.addPropertyMetaData("convertedSmartParentA", kpa(cqp(PersonB.T, "parentA", longToString(true)), qp(PersonA.T, "id")))
				.addPropertyMetaData("companyB", kpa(cqp(PersonB.T, "companyNameB"), qp(CompanyA.T, "nameA")))
				.addPropertyMetaData("convertedBirthDate", qpa(PersonB.T, "birthDate", dateToString(DATE_PATTERN, true)))
				.addPropertyMetaData("convertedDates", qpa(PersonB.T, "dates", dateToString(DATE_PATTERN, true)))
				.addPropertyMetaData(asIsForPropertyNameSuffix("B"));

			editor.onEntityType(Company.T)
				.addMetaData(qualifiedEntityAssignment(CompanyA.T))
				.addPropertyMetaData("id", asIsProperty())
				.addPropertyMetaData(asIsForPropertyNameSuffix("A"));

			editor.onEntityType(CompositeKpaEntity.T)
				.addMetaData(qualifiedEntityAssignment(CompositeKpaEntityA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(CompositeIkpaEntity.T)
				.addMetaData(qualifiedEntityAssignment(CompositeIkpaEntityA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(SmartAddress.T)
				.addMetaData(qualifiedEntityAssignment(Address.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(Vehicle.T)
				.addMetaData(qualifiedEntityAssignment(VehicleA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(Car.T)
				.addMetaData(qualifiedEntityAssignment(CarA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(FlyingCar.T)
				.addMetaData(qualifiedEntityAssignment(FlyingCarA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(SmartItem.T)
				.addMetaData(qualifiedEntityAssignment(ItemB.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(SmartStringIdEntity.T)
				.addMetaData(qualifiedEntityAssignment(StandardIdEntity.T))
				.addPropertyMetaData(asIsProperty())
				.addPropertyMetaData("id", qpa(StandardIdEntity.T, "id", longToString(false)))
				.addPropertyMetaData("kpaParent", kpa(cqp(StandardIdEntity.T, "id"), qp(StandardIdEntity.T, "kpaParentId")));

			editor.onEntityType(Id2UniqueEntity.T)
				.addMetaData(qualifiedEntityAssignment(Id2UniqueEntityA.T))
				.addPropertyMetaData("id", qpa(Id2UniqueEntityA.T, "unique"))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(UnmappedSmartEntity.T)
				.addMetaData(unmapped());
			
			// Enums
			editor.onEntityType(SmartEnumEntityB.T)
				.addMetaData(qualifiedEntityAssignment(EnumEntityB.T))
				.addPropertyMetaData(asIsProperty())
				.addPropertyMetaData("enumCustomConverted", asIsProperty(enumToOrdinalConversion(ItemType.class, oracleS)));

			addEnumConstantMapping(ItemType_String.class, EnumConstantMappingProvider.STRING_CONVERSION("_B"));
			addEnumConstantMapping(ItemType.class, EnumConstantMappingProvider.ENUM_CONVERSION(oracleB.getEnumTypeOracle(ItemTypeB.class).asGmEnumType(), "_B"));

			// ###################################
			// ## . . . . . . Constant . . . . .##
			// ###################################

			editor.onEntityType(SmartConstantPropEntityA.T)
				.addMetaData(qualifiedEntityAssignment(ConstantPropEntityA.T))
				.addPropertyMetaData(asIsProperty())
				.addPropertyMetaData("constantValue", constantProperty(SmartConstantPropEntityA.CONSTANT_VALUE));

			editor.onEntityType(SmartConstantPropEntitySubA.T)
				.addMetaData(qualifiedEntityAssignment(ConstantPropEntitySubA.T))
				.addPropertyMetaData("constantValue", constantProperty(SmartConstantPropEntitySubA.CONSTANT_VALUE_SUB));

			editor.onEntityType(SmartConstantPropEntityA2.T)
				.addMetaData(qualifiedEntityAssignment(ConstantPropEntityA2.T))
				.addPropertyMetaData(asIsProperty())
				.addPropertyMetaData("constantValue", constantProperty(SmartConstantPropEntityA2.CONSTANT_VALUE));

			// ###################################
			// ## . . . . . . Special . . . . . ##
			// ###################################

			editor.onEntityType(SmartPublicationB.T) // unmapped type
				.addPropertyMetaData("favoriteReader", important(priority(ikpa(cqp(ReaderA.T, "ikpaPublicationTitle"), SmartPublicationB.T, "title"), 10)));

			editor.onEntityType(SmartBookA.T)
				.addMetaData(qualifiedEntityAssignment(BookA.T))
				.addPropertyMetaData(asIsProperty());

			editor.onEntityType(SmartManualA.T)
				.addMetaData(qualifiedEntityAssignment(ManualA.T))
				.addPropertyMetaData("smartManualString", qpa(ManualA.T, "manualString"));

			editor.onEntityType(SmartBookB.T)
				.addMetaData(qualifiedEntityAssignment(BookB.T))
				.addPropertyMetaData(asIsProperty())
				.addPropertyMetaData("title", qpa(BookB.T, "titleB"));

			editor.onEntityType(SmartReaderA.T)
				.addMetaData(qualifiedEntityAssignment(ReaderA.T))

				.addPropertyMetaData(asIsProperty())

				.addPropertyMetaData("favoritePublication", kpa(cqp(ReaderA.T, "favoritePublicationTitle"), qp(SmartPublicationB.T, "title")))
				.addPropertyMetaData("favoritePublications", kpa(cqp(ReaderA.T, "favoritePublicationTitles"), qp(SmartPublicationB.T, "title")))
				.addPropertyMetaData("favoritePublicationLink", linkPropertyAssignment(ReaderA.T, "name", SmartPublicationB.T, "title", ReaderBookLink.T, "readerName", "publicationTitle"))
				.addPropertyMetaData("favoritePublicationLinks", linkPropertyAssignment(ReaderA.T, "name", SmartPublicationB.T, "title", ReaderBookSetLink.T, "readerName", "publicationTitle"))
				// Weak-type Properties
				.addPropertyMetaData("weakFavoriteManual", kpa(cqp(ReaderA.T, "favoriteManualTitle"), qp(SmartManualA.T, "title")))
				.addPropertyMetaData("weakFavoriteManuals", kpa(cqp(ReaderA.T, "favoriteManualTitles"), qp(SmartManualA.T, "title")))
				.addPropertyMetaData("weakInverseFavoriteManuals", ikpa(cqp(SmartManualA.T, "smartManualString"), ReaderA.T, "name"))
				.addPropertyMetaData("weakFavoriteManualLink", linkPropertyAssignment(ReaderA.T, "name", SmartManualA.T, "title", ReaderBookLink.T, "readerName", "publicationTitle"));

		// ###################################
		// ## . . . . . Business . . . . . .##
		// ###################################

		editor.onEntityType(Customer.T)
			.addMetaData(qualifiedEntityAssignment(CustomerA.T))
			.addPropertyMetaData(asIsProperty());

		editor.onEntityType(Product.T)
			.addPropertyMetaData(asIsProperty());

		editor.onEntityType(JdeInventory.T)
			.addMetaData(qualifiedEntityAssignment(JdeInventoryB.T))
			.addPropertyMetaData("customer", kpa(cqp(JdeInventoryB.T, "ucn"), qp(CustomerA.T, "ucn")));

		editor.onEntityType(SapInventory.T)
			.addMetaData(qualifiedEntityAssignment(SapInventoryB.T))
			.addPropertyMetaData("customer", kpa(cqp(SapInventoryB.T, "ucn"), qp(CustomerA.T, "ucn")));

		// ##########################################
		// ## . . . . . . Discriminator . . . . . .##
		// ##########################################

		PolymorphicBaseEntityAssignment base = polymorphicBase(DiscriminatorEntityA.T, "discriminator");

		editor.onEntityType(SmartDiscriminatorBase.T)
			.addMetaData(base)
			.addPropertyMetaData(asIsProperty());

		editor.onEntityType(SmartDiscriminatorType1.T)
			.addMetaData(polymorphicDerivation(base, setup.accessA, DISC_TYPE1));

		editor.onEntityType(SmartDiscriminatorType2.T)
			.addMetaData(polymorphicDerivation(base, setup.accessA, DISC_TYPE2));

		// ###################################
		// ## . . . . . . Shared . . . . . .##
		// ###################################

		// SmartSharedSourceOwnerA -> all properties mapped AsIs
		editor.onEntityType(SmartSourceOwnerA.T)
			.addMetaData(qualifiedEntityAssignment(SourceOwnerA.T))
			.addPropertyMetaData(asIsProperty())
			.addPropertyMetaData("kpaSharedSource", kpa(cqp(SourceOwnerA.T, "kpaSharedSourceUuid"), qp(SharedSource.T, "uuid")))
			.addPropertyMetaData("kpaSharedSourceSet", kpa(cqp(SourceOwnerA.T, "kpaSharedSourceUuidSet"), qp(SharedSource.T, "uuid")));

		editor.onEntityType(SharedEntity.T)
			.addMetaData(asIsEntity(setup.accessA))
			.addMetaData(asIsEntity(setup.accessB))
			.addPropertyMetaData(asIsProperty());

		editor.onEntityType(SharedFile.T)
			.addMetaData(defaultDelegate(setup.accessB));

		editor.onEntityType(SharedFileDescriptor.T)
			.addMetaData(defaultDelegate(setup.accessA));
		// @formatter:on
	}

}
