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
package com.braintribe.model.processing.deployment.hibernate.test.metamodel;

import java.util.Collections;
import java.util.stream.Collectors;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.deployment.hibernate.testmodel.collections.Person;
import com.braintribe.model.processing.deployment.hibernate.testmodel.enriching.EnrichedEntity;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Card;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.CardCompany;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Company;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.DebitCard;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Employee;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;

/**
 * 
 */
public class MetaModelProvider {

	public static GmMetaModel provideModel() {
		GmMetaModel gmMetaModel = CardCompany.T.getModel().getMetaModel();
		return gmMetaModel.clone(new StandardCloningContext());
	}

	public static GmMetaModel provideShuffledModel() {
		return shuffle(provideModel());
	}

	public static GmMetaModel provideEnrichedModel() {
		return enrichMetaModel(provideModel());
	}

	public static GmMetaModel provideShuffledEnrichedModel() {
		return shuffle(provideEnrichedModel());
	}

	private static GmMetaModel shuffle(GmMetaModel in) {
		for (GmEntityType entityType : in.entityTypes().collect(Collectors.toList())) {
			Collections.shuffle(entityType.getProperties());
		}
		return in;
	}

	private static GmMetaModel enrichMetaModel(GmMetaModel gmMetaModel) {

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(gmMetaModel);

		/************************
		 * MaxLength
		 ************************/

		MaxLength length = MaxLength.T.create();
		length.setLength(1000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthString", length);

		length = MaxLength.T.create();
		length.setLength(2000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthAndTextHintString", length);

		length = MaxLength.T.create();
		length.setLength(3000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthAndClobHintString", length);

		length = MaxLength.T.create();
		length.setLength(4000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthSet", length);

		length = MaxLength.T.create();
		length.setLength(3000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthList", length);

		length = MaxLength.T.create();
		length.setLength(2000L);
		editor.onEntityType(EnrichedEntity.T).addPropertyMetaData("maxLengthMap", length);

		/************************
		 * EntityMapping
		 ************************/

		// full xml to Employee

		EntityMapping entityMapping = EntityMapping.T.create();
		entityMapping.setXml(employeeXmlSnipped);
		editor.onEntityType(Employee.T).addMetaData(entityMapping);

		// mapToDb=false to DebitCard, which will be cascaded to PrePaidDebitCard

		entityMapping = EntityMapping.T.create();
		entityMapping.setMapToDb(false);
		editor.onEntityType(DebitCard.T).addMetaData(entityMapping);

		/************************
		 * PropertyMapping
		 ************************/

		// Company.employeeList = mapToDb=false, which inherited by CardCompany.employeeList will result in employeeList
		// not being mapped in CardCompany and Bank
		PropertyMapping propertyMapping = PropertyMapping.T.create();
		propertyMapping.setMapToDb(false);
		editor.onEntityType(Company.T).addPropertyMetaData("employeeList", propertyMapping);

		// Company.name column = COMPANY_NAME affecting CardCompany and Bank
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setColumnName("COMPANY_NAME");
		editor.onEntityType(Company.T).addPropertyMetaData("name", propertyMapping);

		// Card.number length=90 and columnName = CC_NUM
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setLength(90L);
		propertyMapping.setColumnName("CC_NUM");
		editor.onEntityType(Card.T).addPropertyMetaData("number", propertyMapping);

		// Card.brand lazy=false, foreign-key = CARD_COMPANY_FK
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setLazy("false");
		propertyMapping.setForeignKey("CARD_COMPANY_FK");
		editor.onEntityType(Card.T).addPropertyMetaData("brand", propertyMapping);

		// Person.carPlateCarMarkMap may key: foreign-key="CAR_MARK_FK"
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setMapKeyForeignKey("CAR_MARK_FK");
		editor.onEntityType(Person.T).addPropertyMetaData("carPlateCarMarkMap", propertyMapping);

		// Person.carSet key foreign key = CAR_SET_OWNER_FK, element foreign key: CAR_SET_ELEMENT_FK
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setForeignKey("CAR_SET_OWNER_FK");
		propertyMapping.setCollectionElementForeignKey("CAR_SET_ELEMENT_FK");
		editor.onEntityType(Person.T).addPropertyMetaData("carSet", propertyMapping);

		// Person.carList key foreign key = CAR_SET_OWNER_FK, element foreign key: CAR_SET_ELEMENT_FK
		propertyMapping = PropertyMapping.T.create();
		propertyMapping.setForeignKey("CAR_LIST_OWNER_FK");
		propertyMapping.setCollectionElementForeignKey("CAR_LIST_ELEMENT_FK");
		editor.onEntityType(Person.T).addPropertyMetaData("carList", propertyMapping);

		return gmMetaModel;
	}

	private static final String employeeXmlSnipped = "" + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<!DOCTYPE hibernate-mapping PUBLIC  \n"
			+ "	\"-//Hibernate/Hibernate Mapping DTD//EN\" \n" + "	\"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">\n"
			+ "<hibernate-mapping auto-import=\"true\">\n"
			+ "  <class name=\"com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Employee\" table=\"TEST_EMPLOYEE\" abstract=\"false\">\n"
			+ "    <id name=\"employeeNumber\" column=\"EMPLOYEE_REGISTRATION_NUMBER\">\n" + "      <generator class=\"native\" />\n" + "    </id>\n"
			+ "    <property name=\"fullName\" column=\"EMPLOYEE_FULL_NAME\" />\n" + "  </class>\n" + "</hibernate-mapping>";

}
