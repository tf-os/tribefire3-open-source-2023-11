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
package com.braintribe.model.access.hibernate.base.wire.space;

import static com.braintribe.model.access.hibernate.base.tools.HibernateMappings.UNMAPPED_P;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.concat;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.access.hibernate.base.model.acl.AclHaTestEntity;
import com.braintribe.model.access.hibernate.base.model.n8ive.AmbiguousEntity;
import com.braintribe.model.access.hibernate.base.model.n8ive.Player;
import com.braintribe.model.access.hibernate.base.model.simple.BasicCollectionEntity;
import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.access.hibernate.base.model.simple.GraphNodeEntity;
import com.braintribe.model.access.hibernate.base.model.simple.HierarchyBase;
import com.braintribe.model.access.hibernate.base.model.simple.HierarchySubA;
import com.braintribe.model.access.hibernate.base.model.simple.HierarchySubB;
import com.braintribe.model.access.hibernate.base.model.simple.StringIdEntity;
import com.braintribe.model.access.hibernate.base.wire.contract.HibernateModelsContract;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardIntegerIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.wire.api.annotation.Managed;

/**
 * @author peter.gazdik
 */
@Managed
public class HibernateModelsSpace implements HibernateModelsContract {

	public static final String NON_CP_ENTITY_SIG = "synthetic.NonCpEntity";

	// @formatter:off
	private static final List<EntityType<?>> simpleTypes = asList( //
			BasicEntity.T, //
			BasicScalarEntity.T, //
			BasicCollectionEntity.T, //
			StringIdEntity.T, //

			HierarchySubA.T, //
			HierarchySubB.T, //
			HierarchyBase.T //
	);

	private static final List<EntityType<?>> ALL_TYPES = concat( //
			simpleTypes //
	);

	private static final List<EntityType<?>> nativeTypes = asList( //
			Player.T, //
			
			AmbiguousEntity.T, //
			com.braintribe.model.access.hibernate.base.model.n8ive.sub.AmbiguousEntity.T //
	);
	
	private static final List<EntityType<?>> graphTypes = asList( //
			GraphNodeEntity.T //
	);
	
	private static final List<EntityType<?>> aclTypes = asList( //
			Acl.T,
			AclHaTestEntity.T //
			
	);
	// @formatter:on

	// ###################################################
	// ## . . . . . . . . Mapped models . . . . . . . . ##
	// ###################################################

	@Override
	@Managed
	public GmMetaModel basic_NoPartition() {
		GmMetaModel result = allRaw();

		BasicModelMetaDataEditor md = new BasicModelMetaDataEditor(result);
		unmapGlobalIdAndPartition(md);

		return result;
	}

	@Override
	public GmMetaModel compositeId() {
		GmMetaModel result = allRaw();

		BasicModelMetaDataEditor md = new BasicModelMetaDataEditor(result);
		unmapGlobalIdAndPartition(md);

		md.onEntityType(BasicEntity.T) //
				.addPropertyMetaData(GenericEntity.id, compositeIdMapping());

		return result;
	}

	private static MetaData compositeIdMapping() {
		String globalId = "hbm:CompositeId#id";

		JpaCompositeId result = JpaCompositeId.T.create(globalId);
		result.setColumns(asList( //
				hbmColumn(globalId, "integerValue", "integer"), //
				hbmColumn(globalId, "stringValue", "string") //
		));

		return result;
	}

	private static JpaColumn hbmColumn(String globalIdPrefix, String name, String type) {
		JpaColumn result = JpaColumn.T.create(globalIdPrefix + ":" + name);
		result.setName(name);
		result.setType(type);

		return result;
	}

	@Override
	public GmMetaModel n8ive() {
		GmMetaModel result = nativeRaw();

		BasicModelMetaDataEditor md = new BasicModelMetaDataEditor(result);
		unmapGlobalIdAndPartition(md);

		return result;
	}

	@Override
	public GmMetaModel graph() {
		GmMetaModel result = graphRaw();

		BasicModelMetaDataEditor md = new BasicModelMetaDataEditor(result);
		unmapGlobalIdAndPartition(md);

		return result;
	}

	@Override
	public GmMetaModel acl() {
		GmMetaModel result = aclRaw();

		BasicModelMetaDataEditor md = new BasicModelMetaDataEditor(result);
		unmapGlobalIdAndPartition(md);

		return result;
	}

	@Override
	public GmMetaModel nonClasspath() {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("hibernate-test:AllModel", asList(HasName.T));

		GmEntityType hasName = first(result.getTypes());
		hasName.setTypeSignature(NON_CP_ENTITY_SIG);
		hasName.setIsAbstract(false);

		result.deploy();

		return result;
	}

	// ###################################################
	// ## . . . . . . . Mapping helpers . . . . . . . . ##
	// ###################################################

	private void unmapGlobalIdAndPartition(BasicModelMetaDataEditor md) {
		md.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(GenericEntity.globalId, UNMAPPED_P) //
				.addPropertyMetaData(GenericEntity.partition, UNMAPPED_P) //
		;
	}

	// ###################################################
	// ## . . . . . . . . . Raw models . . . . . . . . .##
	// ###################################################

	/** Pretty much raw, but LocalizedString values are configured with length 1000 */
	private GmMetaModel allRaw() {
		GmMetaModel i18nModel = LocalizedString.T.getModel().getMetaModel();

		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("hibernate-test:all-model", ALL_TYPES, asList(i18nModel));
		addMd(result, this::addLocalizedStringMd);

		return result;
	}

	private GmMetaModel nativeRaw() {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("hibernate-test:native-model", nativeTypes);
		addMd(result, null);

		return result;
	}

	private GmMetaModel graphRaw() {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("hibernate-test:graph-model", graphTypes);
		addMd(result, null);

		return result;
	}

	private GmMetaModel aclRaw() {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("hibernate-test:acl-model", aclTypes);
		addMd(result, null);

		return result;
	}

	private void addMd(GmMetaModel model, Consumer<ModelMetaDataEditor> customMdConfigurer) {
		ModelOracle modelOracle = new BasicModelOracle(model);

		ModelMetaDataEditor md = new BasicModelMetaDataEditor(model);
		md.onEntityType(StandardIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(modelOracle.getGmLongType()));
		md.onEntityType(StandardStringIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(modelOracle.getGmStringType()));
		md.onEntityType(StandardIntegerIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(modelOracle.getGmIntegerType()));

		if (customMdConfigurer != null)
			customMdConfigurer.accept(md);
	}

	private void addLocalizedStringMd(ModelMetaDataEditor md) {
		md.onEntityType(LocalizedString.T) //
				.addPropertyMetaData(LocalizedString.localizedValues, length(1000L));
	}

	private PropertyMapping length(Long length) {
		PropertyMapping result = PropertyMapping.T.create();
		result.setLength(length);
		return result;
	}

	@Managed
	private TypeSpecification typeSpecification(GmType gmType) {
		TypeSpecification result = TypeSpecification.T.create();
		result.setType(gmType);

		return result;
	}

}
