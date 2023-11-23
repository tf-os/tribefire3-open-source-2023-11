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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlgeneration;

import static com.braintribe.model.processing.deployment.hibernate.mapping.utils.ResourceUtils.loadResourceToString;
import static com.braintribe.model.processing.deployment.hibernate.mapping.utils.ResourceUtils.loadResourceToStrings;
import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbeddable;
import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbedded;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.display.NameConversion;
import com.braintribe.model.meta.data.display.NameConversionStyle;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGeneratingService;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.HbmXmlGeneratingServiceException;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.utils.FileTools;

import test.models.basic.ComplexCollectionsEntity;
import test.models.basic.ComplexEntity;
import test.models.basic.ComplexMapsEntity;
import test.models.basic.SimpleCollectionsEntity;
import test.models.basic.SimpleEntity;
import test.models.basic.SimpleMapsEntity;
import test.models.basic.SimpleSubEntity;
import test.models.hierarchy.AbstractBaseReferer;
import test.models.hierarchy.Base;
import test.models.hierarchy.BaseReferer;
import test.models.hierarchy.Left;
import test.models.hierarchy.Right;
import test.models.idgen.LongIdEntity1;
import test.models.idgen.LongIdEntity2;
import test.models.idgen.LongIdEntity3;
import test.models.idgen.LongIdEntity4;
import test.models.idgen.LongIdEntity5;
import test.models.idgen.LongIdEntity6;
import test.models.idgen.LongIdEntity7;
import test.models.idgen.LongIdEntity8;
import test.models.idgen.LongIdEntity9;
import test.models.idgen.StringIdEntity1;
import test.models.idgen.StringIdEntity2;
import test.models.idgen.StringIdEntity3;
import test.models.idgen.StringIdEntity4;
import test.models.idgen.StringIdEntity5;
import test.models.idgen.StringIdEntity6;
import test.models.idgen.StringIdEntity7;
import test.models.idgen.StringIdEntity8;
import test.models.idgen.StringIdEntity9;
import test.models.naming.InvalidDbNamesEntity;

/**
 * <p>
 * Tests hibernate mapping generation based on models which are combined per use case using entities defined within this test artifact.
 * 
 */
public class MappingGenerationFromLocalModelsTest {
	static final CharacterMarshaller characterMarshaller = new JsonStreamMarshaller();

	private static boolean updateMode = false;
	private static final String updateBase = "src/test/expected";

	// @formatter:off
	static final List<EntityType<?>> basicTypes = asList(
			ComplexCollectionsEntity.T,
			ComplexEntity.T,
			ComplexMapsEntity.T,
			SimpleCollectionsEntity.T,
			SimpleEntity.T,
			SimpleMapsEntity.T
	);

	static final List<EntityType<?>> abcxTypes = asList(
			test.models.inherit.e00.A.T,
			test.models.inherit.e00.B.T,
			test.models.inherit.e00.C.T,
			test.models.inherit.e00.X.T
	);
	// @formatter:on

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testSkeleton() throws Exception {
		String tag = "testSkeleton";

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, basicTypes);

		renderMappings(metaModel);

		assertMapping(tag, basicTypes);
	}

	@Test
	public void testEntityCustomXmlAsString() throws Exception {
		String tag = "testEntityCustomXml";

		List<EntityType<?>> types = asList(/* SimpleEntity.T, */ SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		// EntityMapping mapping = EntityMapping.T.create();
		// mapping.setXml(loadResourceToString("classpath:test/models/basic/SimpleEntity.xml"));

		// ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		// editor.onEntityType(SimpleEntity.T).addMetaData(mapping);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testEntityCustomXmlAsStringUsingHints() throws Exception {
		String tag = "testEntityCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setXml(loadResourceToString("classpath:test/models/basic/SimpleEntity.xml"));

		String hints = hints().add(SimpleEntity.T, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, types);
	}

	@Test
	public void testEntityCustomXmlFromUrl() throws Exception {
		String tag = "testEntityCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setXmlFileUrl("classpath:test/models/basic/SimpleEntity.xml");

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addMetaData(mapping);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testEntityCustomXmlFromUrlUsingHints() throws Exception {
		String tag = "testEntityCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setXmlFileUrl("classpath:test/models/basic/SimpleEntity.xml");

		String hints = hints().add(SimpleEntity.T, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, types);
	}

	@Test
	public void testPropertyCustomXmlAsString() throws Exception {
		String tag = "testPropertyCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setXml(loadResourceToString("classpath:test/models/basic/SimpleEntity.enumProperty.xml"));

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addPropertyMetaData(SimpleEntity.enumProperty, mapping);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testPropertyCustomXmlAsStringUsingHints() throws Exception {
		String tag = "testPropertyCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setXml(loadResourceToString("classpath:test/models/basic/SimpleEntity.enumProperty.xml"));

		String hints = hints().add(SimpleEntity.T, SimpleEntity.enumProperty, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, types);
	}

	@Test
	public void testPropertyCustomXmlFromUrl() throws Exception {
		String tag = "testPropertyCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setXmlFileUrl("classpath:test/models/basic/SimpleEntity.enumProperty.xml");

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addPropertyMetaData(SimpleEntity.enumProperty, mapping);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testPropertyCustomXmlFromUrlUsingHints() throws Exception {
		String tag = "testPropertyCustomXml";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setXmlFileUrl("classpath:test/models/basic/SimpleEntity.enumProperty.xml");

		String hints = hints().add(SimpleEntity.T, SimpleEntity.enumProperty, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, types);
	}

	@Test
	public void testEntityNotMapped() throws Exception {
		String tag = "testEntityNotMapped";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setMapToDb(false);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleCollectionsEntity.T).addMetaData(mapping);

		renderMappings(metaModel);

		assertMapping(tag, SimpleEntity.T);
	}

	@Test
	public void testEntityNotMappedUsingHints() throws Exception {
		String tag = "testEntityNotMapped";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setMapToDb(false);

		String hints = hints().add(SimpleCollectionsEntity.T, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, SimpleEntity.T);
	}

	@Test(expected = HbmXmlGeneratingServiceException.class)
	public void testReferencedEntityNotMapped() throws Exception {
		String tag = "testReferencedEntityNotMapped";

		List<EntityType<?>> types = asList(SimpleEntity.T, ComplexEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setMapToDb(false);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addMetaData(mapping);

		renderMappings(metaModel);

		// Must throw HbmXmlGeneratingServiceException :
		// ComplexEntity.entityProperty references SimpleEntity, which was marked not to be mapped.
		Assert.fail("Should have failed with " + HbmXmlGeneratingServiceException.class.getName());
	}

	@Test
	public void testPropertyNotMapped() throws Exception {
		String tag = "testPropertyNotMapped";

		List<EntityType<?>> types = asList(SimpleEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setMapToDb(false);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addPropertyMetaData(SimpleEntity.enumProperty, mapping);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testPropertyNotMappedUsingHints() throws Exception {
		String tag = "testPropertyNotMapped";

		List<EntityType<?>> types = asList(SimpleEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setMapToDb(false);

		String hints = hints().add(SimpleEntity.T, SimpleEntity.enumProperty, mapping).done();

		renderMappings(metaModel, hints);

		assertMapping(tag, types);
	}

	@Test
	public void testConstraints() throws Exception {
		String tag = "testConstraints";

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, basicTypes);

		enrichBasicModel(metaModel, true, false, false);

		renderMappings(metaModel);

		assertMapping(tag, basicTypes);
	}

	@Test
	public void testDatabaseNames() throws Exception {
		String tag = "testDatabaseNames";

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, basicTypes);

		enrichBasicModel(metaModel, false, true, false);

		renderMappings(metaModel);

		assertMapping(tag, basicTypes);
	}

	@Test
	public void testDatabaseNamesWithSpaces() throws Exception {
		String tag = "testDatabaseNamesWithSpaces";

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, basicTypes);

		enrichBasicModel(metaModel, false, true, true);

		renderMappings(metaModel);

		assertMapping(tag, basicTypes);
	}

	@Test
	public void testInvalidDatabaseNames() throws Exception {
		String tag = "testInvalidDatabaseNames";

		List<EntityType<?>> types = asList(InvalidDbNamesEntity.T);

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, types);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testConstraintsAndDatabaseNames() throws Exception {
		String tag = "testConstraintsAndDatabaseNames";

		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, basicTypes);

		enrichBasicModel(metaModel, true, true, false);

		renderMappings(metaModel);

		assertMapping(tag, basicTypes);
	}

	@Test
	public void measure() throws Exception {
		for (int i = 0; i < 3; i++)
			testIdGeneration();
	}

	@Test
	public void testIdGeneration() throws Exception {
		String tag = "testIdGeneration";

		// @formatter:off
		List<EntityType<?>> types = asList(
			LongIdEntity1.T,
			LongIdEntity2.T,
			LongIdEntity3.T,
			LongIdEntity4.T,
			LongIdEntity5.T,
			LongIdEntity6.T,
			LongIdEntity7.T,
			LongIdEntity8.T,
			LongIdEntity9.T,
			StringIdEntity1.T,
			StringIdEntity2.T,
			StringIdEntity3.T,
			StringIdEntity4.T,
			StringIdEntity5.T,
			StringIdEntity6.T,
			StringIdEntity7.T,
			StringIdEntity8.T,
			StringIdEntity9.T
		);
		// @formatter:on

		GmMetaModel metaModel = provideModel(tag, types);

		// @formatter:off
		// id type | auto  | generation | expected
		// long    | null  | null       | native
		// long    | true  | null       | native
		// long    | false | null       | native
		// long    | null  | native     | native
		// long    | true  | native     | native
		// long    | false | native     | native
		// long    | null  | assigned   | assigned
		// long    | true  | assigned   | assigned
		// long    | false | assigned   | assigned
		// string  | null  | null       | assigned
		// string  | true  | null       | native
		// string  | false | null       | assigned
		// string  | null  | native     | native
		// string  | true  | native     | native
		// string  | false | native     | native
		// string  | null  | assigned   | assigned
		// string  | true  | assigned   | assigned
		// string  | false | assigned   | assigned
		// @formatter:on

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);

		editor.onEntityType(LongIdEntity1.T).addPropertyMetaData("id", idGenerationMapping(null, null));
		editor.onEntityType(LongIdEntity2.T).addPropertyMetaData("id", idGenerationMapping(true, null));
		editor.onEntityType(LongIdEntity3.T).addPropertyMetaData("id", idGenerationMapping(false, null));
		editor.onEntityType(LongIdEntity4.T).addPropertyMetaData("id", idGenerationMapping(null, "native"));
		editor.onEntityType(LongIdEntity5.T).addPropertyMetaData("id", idGenerationMapping(true, "native"));
		editor.onEntityType(LongIdEntity6.T).addPropertyMetaData("id", idGenerationMapping(false, "native"));
		editor.onEntityType(LongIdEntity7.T).addPropertyMetaData("id", idGenerationMapping(null, "assigned"));
		editor.onEntityType(LongIdEntity8.T).addPropertyMetaData("id", idGenerationMapping(true, "assigned"));
		editor.onEntityType(LongIdEntity9.T).addPropertyMetaData("id", idGenerationMapping(false, "assigned"));
		editor.onEntityType(StringIdEntity1.T).addPropertyMetaData("id", idGenerationMapping(null, null));
		editor.onEntityType(StringIdEntity2.T).addPropertyMetaData("id", idGenerationMapping(true, null));
		editor.onEntityType(StringIdEntity3.T).addPropertyMetaData("id", idGenerationMapping(false, null));
		editor.onEntityType(StringIdEntity4.T).addPropertyMetaData("id", idGenerationMapping(null, "native"));
		editor.onEntityType(StringIdEntity5.T).addPropertyMetaData("id", idGenerationMapping(true, "native"));
		editor.onEntityType(StringIdEntity6.T).addPropertyMetaData("id", idGenerationMapping(false, "native"));
		editor.onEntityType(StringIdEntity7.T).addPropertyMetaData("id", idGenerationMapping(null, "assigned"));
		editor.onEntityType(StringIdEntity8.T).addPropertyMetaData("id", idGenerationMapping(true, "assigned"));
		editor.onEntityType(StringIdEntity9.T).addPropertyMetaData("id", idGenerationMapping(false, "assigned"));

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testEntityMappedByReference() throws Exception {
		String tag = "testEntityMappedByReference";

		GmMetaModel metaModel = provideModel(tag, abcxTypes);

		renderMappings(metaModel);

		assertMapping(tag, abcxTypes);
	}

	@Test
	public void testEntityNotMappedDueToPropertyNotMapped() throws Exception {
		String tag = "testEntityNotMappedDueToPropertyNotMapped";

		GmMetaModel metaModel = provideModel(tag, abcxTypes);

		PropertyMapping mapping = PropertyMapping.T.create();
		mapping.setMapToDb(false);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(test.models.inherit.e00.X.T).addPropertyMetaData("propertyX", mapping);

		renderMappings(metaModel);

		// @formatter:off
		assertMapping(
			tag, 
			test.models.inherit.e00.B.T,
			test.models.inherit.e00.C.T,
			test.models.inherit.e00.X.T
		);
		// @formatter:on
	}

	@Test
	public void testEntityForcedMapping() throws Exception {
		String tag = "testEntityForcedMapping";

		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleCollectionsEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setForceMapping(true);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(StandardIdentifiable.T).addMetaData(mapping);

		renderMappings(metaModel);

		// @formatter:off
		assertMapping(
			tag, 
			StandardIdentifiable.T,
			SimpleEntity.T,
			SimpleCollectionsEntity.T
		);
		// @formatter:on
	}

	@Test
	public void testEntityForcedMappingOnTransient() throws Exception {
		String tag = "testEntityForcedMappingOnTransient";

		List<EntityType<?>> types = asList(LongIdEntity1.T, StringIdEntity1.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping mapping = EntityMapping.T.create();
		mapping.setMapToDb(false);
		mapping.setForceMapping(true);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(StandardIdentifiable.T).addMetaData(mapping);

		renderMappings(metaModel);

		assertMapping(tag, StringIdEntity1.T);
	}

	/**@see AbstractBaseReferer*/
	@Test
	public void testElectsTopLevelBasedOnPropertyInheritedFromUnmappedType() throws Exception {
		String tag = "testElectsTopLevelBasedOnPropertyInheritedFromUnmappedType";

		List<EntityType<?>> types = asList(Left.T, Right.T, BaseReferer.T);

		GmMetaModel metaModel = provideModel(tag, types);

		EntityMapping unmapped = EntityMapping.T.create();
		unmapped.setMapToDb(false);
		unmapped.setInherited(false);

		
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(AbstractBaseReferer.T).addMetaData(unmapped);

		renderMappings(metaModel);

		assertMapping(tag,  BaseReferer.T, Base.T, Left.T, Right.T);
	}

	
	@Test
	public void testDiscriminator() throws Exception {
		String tag = "testDiscriminator";

		GmMetaModel metaModel = provideModel(tag, abcxTypes);

		EntityMapping mappingA = EntityMapping.T.create();
		mappingA.setDiscriminatorColumnName("myCustom discriminatorCol");
		mappingA.setDiscriminatorType("char");

		EntityMapping mappingB = EntityMapping.T.create();
		mappingB.setDiscriminatorValue("B");

		EntityMapping mappingC = EntityMapping.T.create();
		mappingC.setDiscriminatorValue("C");

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(test.models.inherit.e00.A.T).addMetaData(mappingA);
		editor.onEntityType(test.models.inherit.e00.B.T).addMetaData(mappingB);
		editor.onEntityType(test.models.inherit.e00.C.T).addMetaData(mappingC);

		renderMappings(metaModel);

		assertMapping(tag, abcxTypes);
	}

	@Test
	public void testDiscriminatorFormula() throws Exception {
		String tag = "testDiscriminatorFormula";

		GmMetaModel metaModel = provideModel(tag, abcxTypes);

		EntityMapping mappingA = EntityMapping.T.create();
		mappingA.setDiscriminatorFormula("(CASE WHEN propertyA IS NULL THEN 'A' ELSE propertyA END)");

		EntityMapping mappingB = EntityMapping.T.create();
		mappingB.setDiscriminatorValue("B");

		EntityMapping mappingC = EntityMapping.T.create();
		mappingC.setDiscriminatorValue("C");

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(test.models.inherit.e00.A.T).addMetaData(mappingA);
		editor.onEntityType(test.models.inherit.e00.B.T).addMetaData(mappingB);
		editor.onEntityType(test.models.inherit.e00.C.T).addMetaData(mappingC);

		renderMappings(metaModel);

		assertMapping(tag, abcxTypes);
	}

	@Test
	public void testMaxLengthPropertyMappingConflict() throws Exception {
		String tag = "testMaxLengthPropertyMappingConflict";

		List<EntityType<?>> types = asList(SimpleEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		PropertyMapping propertyMappingText = PropertyMapping.T.create();
		propertyMappingText.setType("text");
		propertyMappingText.setLength(2000L); // Will be overridden by MaxLength.length

		MaxLength maxLength = MaxLength.T.create();
		maxLength.setLength(1000L); // Will override the PropertyMapping.length

		new BasicModelMetaDataEditor(metaModel).onEntityType(SimpleEntity.T) //
				.addPropertyMetaData(SimpleEntity.stringProperty, propertyMappingText) //
				.addPropertyMetaData(SimpleEntity.stringProperty, maxLength);

		renderMappings(metaModel);

		assertMapping(tag, types);
	}

	@Test
	public void testNameConversion() throws Exception {
		String tag = "testNameConversion";
		
		List<EntityType<?>> types = asList(SimpleEntity.T, SimpleSubEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		NameConversion nc = NameConversion.T.create();
		nc.setStyle(NameConversionStyle.screamingSnakeCase);
		
		new BasicModelMetaDataEditor(metaModel) //
				.onEntityType(SimpleEntity.T) //
				.addMetaData(nc) //
				.addPropertyMetaData(nc);

		renderMappings(metaModel);

		assertMapping(tag, SimpleEntity.T, SimpleSubEntity.T);
	}

	@Test
	public void testCompositeId() throws Exception {
		String tag = "testCompositeId";

		List<EntityType<?>> types = asList(SimpleEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		JpaCompositeId compositeId = JpaCompositeId.T.create();
		compositeId.setColumns(asList( //
				jpaColumn(SimpleEntity.longProperty, "long"), jpaColumn(SimpleEntity.stringProperty, "string")));

		new BasicModelMetaDataEditor(metaModel) //
				.onEntityType(SimpleEntity.T).addPropertyMetaData(GenericEntity.id, compositeId);

		renderMappings(metaModel);

		assertMapping(tag, SimpleEntity.T);
	}

	private JpaColumn jpaColumn(String name, String type) {
		JpaColumn result = JpaColumn.T.create();
		result.setName(name);
		result.setType(type);

		return result;
	}

	@Test
	public void testEmbedded() throws Exception {
		String tag = "testEmbedded";

		List<EntityType<?>> types = asList(ComplexEntity.T);

		GmMetaModel metaModel = provideModel(tag, types);

		JpaEmbedded embedded = JpaEmbedded.T.create();
		embedded.setEmbeddedPropertyMappings(asMap(//
				SimpleEntity.longProperty, columnMapping(SimpleEntity.longProperty, "long"), //
				SimpleEntity.stringProperty, columnMapping(SimpleEntity.stringProperty, "string") //
		));

		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(SimpleEntity.T).addMetaData(JpaEmbeddable.T.create());
		editor.onEntityType(ComplexEntity.T).addPropertyMetaData(ComplexEntity.entityProperty, embedded);

		renderMappings(metaModel);

		assertMapping(tag, ComplexEntity.T);
	}

	private PropertyMapping columnMapping(String columnName, String type) {
		PropertyMapping result = PropertyMapping.T.create();
		result.setColumnName(columnName);
		result.setType(type);

		return result;
	}

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	/**
	 * Provides the GmMetaModel with some essential metadata, e.g.: TypeSpecification
	 */
	private GmMetaModel provideModel(String tag, List<EntityType<?>> types) {
		GmMetaModel metaModel = MetaModelTools.provideRawModel(tag, types);

		ModelOracle modelOracle = new BasicModelOracle(metaModel);
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);

		editor.onEntityType(StandardIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(modelOracle.getGmLongType()));
		editor.onEntityType(StandardStringIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(modelOracle.getGmStringType()));

		return metaModel;
	}

	private TypeSpecification typeSpecification(GmType gmType) {
		TypeSpecification sts = TypeSpecification.T.create();
		sts.setType(gmType);
		return sts;
	}

	private void enrichBasicModel(GmMetaModel metaModel, boolean addConstraints, boolean addDatabaseNames, boolean allowSpaces) {
		PropertyMapping partition = PropertyMapping.T.create();
		if (addDatabaseNames)
			partition.setColumnName("myCustom partitionCol");

		if (addConstraints)
			partition.setIndex("myCustom partitionIdx");

		PropertyMapping id = PropertyMapping.T.create();
		if (addConstraints)
			id.setScale(8L);

		PropertyMapping longProperty = PropertyMapping.T.create();
		if (addDatabaseNames)
			longProperty.setColumnName(dbName("myCustom l0ngProp3rtyCO1", !allowSpaces));

		PropertyMapping integerProperty = PropertyMapping.T.create();
		if (addDatabaseNames)
			integerProperty.setColumnName(dbName("myCustom integerPropertyCol", !allowSpaces));
		if (addConstraints) {
			integerProperty.setType("custom_integer");
			integerProperty.setPrecision(2L);
			integerProperty.setScale(6L);
			integerProperty.setUnique(true);
			integerProperty.setNotNull(false);
			integerProperty.setUniqueKey("uniqueKey");
			integerProperty.setIndex("index");
			integerProperty.setLazy("true");
		}

		PropertyMapping stringProperty = PropertyMapping.T.create();
		if (addConstraints) {
			stringProperty.setType("string");
			stringProperty.setLength(10L);
			stringProperty.setUnique(false);
			stringProperty.setNotNull(true);
			stringProperty.setIndex("index");
			stringProperty.setLazy("false");
		}

		PropertyMapping booleanProperty = PropertyMapping.T.create();
		if (addConstraints)
			booleanProperty.setReadOnly(true);

		PropertyMapping stringList = PropertyMapping.T.create();
		if (addDatabaseNames) {
			stringList.setCollectionTableName(dbName("myCustom stringList table", !allowSpaces)); // many2mane table
			stringList.setListIndexColumn(dbName("myCustom stringListIndexCol", !allowSpaces));
			stringList.setColumnName(dbName("myCustom stringListCol", !allowSpaces));
		}
		if (addConstraints) {
			stringList.setLazy("extra");
			stringList.setFetch("join");
		}

		PropertyMapping entityProperty = PropertyMapping.T.create();
		if (addConstraints) {
			entityProperty.setInvalidReferencesIgnored(true);
			entityProperty.setReferencedProperty(GenericEntity.globalId);
		}

		PropertyMapping entityList = PropertyMapping.T.create();
		if (addDatabaseNames) {
			entityList.setCollectionTableName(dbName("myCustom entityList table", !allowSpaces)); // many2mane table
			entityList.setListIndexColumn(dbName("myCustom entityListIndexCol", !allowSpaces));
			entityList.setColumnName(dbName("myCustom entityListCol", !allowSpaces));
			entityList.setCollectionElementForeignKey(dbName("myCustom entityList fkey", !allowSpaces));
		}
		if (addConstraints) {
			entityList.setCollectionElementFetch("join");
			entityList.setFetch("subselect");
		}

		PropertyMapping entityToEntityMap = PropertyMapping.T.create();
		if (addDatabaseNames) {
			entityToEntityMap.setCollectionKeyColumn(dbName("myCustom collectionKeyCol", !allowSpaces)); // keyColumn
			entityToEntityMap.setCollectionTableName(dbName("myCustom entToEnt table", !allowSpaces)); // many2mane
																										// table
			entityToEntityMap.setCollectionElementColumn(dbName("myCustom collectionElementCol", !allowSpaces)); // elementColumn
			entityToEntityMap.setMapKeyColumn(dbName("myCustom mapKeyCol", !allowSpaces));
		}
		if (addConstraints) {
			entityToEntityMap.setMapKeySimpleType("myCustom_mapKeySimpleType");
			entityToEntityMap.setMapKeyLength(12L);
			entityToEntityMap.setCollectionKeyPropertyRef("myCustom_collectionKeyPropertyRef");
			entityToEntityMap.setLazy("false");
		}

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);

		// @formatter:off
		editor.onEntityType(GenericEntity.T)
				.addPropertyMetaData(GenericEntity.partition, partition);
		
		editor.onEntityType(SimpleEntity.T)
				.addPropertyMetaData(StandardIdentifiable.id, id)
				.addPropertyMetaData(SimpleEntity.longProperty, longProperty)
				.addPropertyMetaData(SimpleEntity.integerProperty, integerProperty)
				.addPropertyMetaData(SimpleEntity.booleanProperty, booleanProperty);
		
		editor.onEntityType(SimpleCollectionsEntity.T)
				.addPropertyMetaData(SimpleCollectionsEntity.stringList, stringList);

		editor.onEntityType(ComplexEntity.T)
				.addPropertyMetaData(ComplexEntity.entityProperty, entityProperty);

		editor.onEntityType(ComplexCollectionsEntity.T)
				.addPropertyMetaData(ComplexCollectionsEntity.entityList, entityList);
		
		editor.onEntityType(ComplexMapsEntity.T)
				.addPropertyMetaData("entityToEntityMap", entityToEntityMap);
		// @formatter:on
	}

	private String dbName(String name, boolean removeSpaces) {
		return removeSpaces ? name.replace(" ", "_") : name;
	}

	private static PropertyMapping idGenerationMapping(Boolean auto, String strategy) {
		PropertyMapping pm = PropertyMapping.T.create();
		pm.setAutoAssignable(auto);
		pm.setIdGeneration(strategy);
		return pm;
	}

	private void assertMapping(String testName, EntityType<?>... entityTypes) throws Exception {
		assertMapping(testName, asList(entityTypes));
	}

	private void assertMapping(String testName, List<EntityType<?>> entityTypes) throws Exception {
		int totalMappings = tempFolder.getRoot().list().length;
		int expectedMappings = entityTypes.size();

		Assert.assertEquals(totalMappings + " mapping files were generated, but " + expectedMappings + " were expected", expectedMappings,
				totalMappings);

		if (updateMode) {
			Path testFolderPath = Paths.get(updateBase + "/" + testName);
			FileTools.ensureDirectoryExists(testFolderPath.toFile());
			
			for (EntityType<?> entityType : entityTypes) {
				String fileName = entityType.getTypeSignature() + ".hbm.xml";
				String targetPath = tempFolder.getRoot().getCanonicalPath() + File.separator + fileName;
				Path updatePath = testFolderPath.resolve(fileName);
				Files.copy(Paths.get(targetPath), updatePath, StandardCopyOption.REPLACE_EXISTING);
			}

			return;
		}

		for (EntityType<?> entityType : entityTypes) {
			String fileName = entityType.getTypeSignature() + ".hbm.xml";
			assertMapping(testName, fileName);
		}
	}

	private void assertMapping(String testName, String fileName) throws Exception {
		String sourceFile = testName + "/" + fileName;
		String targetPath = tempFolder.getRoot().getCanonicalPath() + File.separator + fileName;
		assertContentEqual("classpath:test/expected/" + sourceFile, targetPath);
	}

	private void assertContentEqual(String expectedUrl, String actualUrl) {
		List<String> expectedLines = loadResourceToStrings(expectedUrl);
		List<String> actualLines = loadResourceToStrings(actualUrl);

		// printContent(expectedUrl);
		// printContent(actualUrl);

		String mappingFileName = expectedUrl.substring(expectedUrl.lastIndexOf("/") + 1);

		int size = Math.min(expectedLines.size(), actualLines.size());
		for (int i = 0; i < size; i++) {
			String expected = expectedLines.get(i);
			String actual = actualLines.get(i);

			// Temp hack for some temp hacking in the template - once we get rid of velocity we should change the expected mapping too
			if (expected.startsWith("<!---->"))
				expected = expected.replace("<!---->", "\t");

			if (!expected.equals(actual))
				throw new AssertionError(
						"\nUNEXPECTED MAPPING\nFile: " + mappingFileName + "\nLine: " + (i + 1) + "\nExpected: " + expected + "\nActual: " + actual);
		}

		assertThat(actualLines).as("Incorrect number of lines in: " + mappingFileName).hasSameSizeAs(actualLines);
	}

	/* package */ static void printContent(String url) {
		String actualS = loadResourceToString(url);
		spOut("\n" + actualS);
	}

	private static HintsBuilder hints() {
		return new HintsBuilder();
	}

	private static class HintsBuilder {
		Map<String, MetaData> metaDataHints = new HashMap<>();

		private HintsBuilder() {
			metaDataHints = new HashMap<>();
		}

		public HintsBuilder add(EntityType<?> type, MetaData metaData) {
			metaDataHints.put(type.getTypeSignature(), metaData);
			return this;
		}

		public HintsBuilder add(EntityType<?> type, String propertyName, MetaData metaData) {
			metaDataHints.put(type.getTypeSignature() + "#" + propertyName, metaData);
			return this;
		}

		public String done() {
			StringWriter writer = new StringWriter();
			try {
				characterMarshaller.marshall(writer, metaDataHints, GmSerializationOptions.defaultOptions);
			} catch (MarshallException e) {
				throw new RuntimeException(e);
			}
			return writer.toString();
		}
	}

	private void renderMappings(GmMetaModel metaModel) {
		renderMappings(metaModel, null);
	}

	private void renderMappings(GmMetaModel metaModel, String hints) {
		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(metaModel);
		generatorService.setOutputFolder(tempFolder.getRoot());
		generatorService.setTypeHints(hints);
		generatorService.renderMappings();

		System.out.println("Mappings generated to: " + tempFolder.getRoot().getAbsolutePath());
	}
}
