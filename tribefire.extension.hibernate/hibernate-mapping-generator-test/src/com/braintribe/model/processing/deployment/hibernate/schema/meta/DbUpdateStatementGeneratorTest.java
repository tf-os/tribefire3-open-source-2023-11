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
package com.braintribe.model.processing.deployment.hibernate.schema.meta;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.model.accessdeployment.hibernate.meta.DbUpdateStatement;
import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmEntityTypeMapBuilder;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.db.NamingStrategyProvider;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.HbmXmlGeneratorException;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptorFactory;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.utils.template.TemplateException;

import test.models.basic.ComplexCollectionsEntity;
import test.models.basic.ComplexEntity;
import test.models.basic.ComplexMapsEntity;
import test.models.basic.SimpleEntity;
import test.models.basic.SimpleSubEntity;

/** Tests for {@link DbUpdateStatementGenerator} */
public class DbUpdateStatementGeneratorTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private File tempDir;

	private HbmXmlGenerationContext context;
	private GmMetaModel metaModel;
	private Collection<EntityDescriptor> entityDescriptors;
	private boolean before = false;

	private static final String EMPTY = "";
	private static final String NULL = null;
	private static final String SIMPLE = "SELECT HELLO";
	private static final String WRONG_SYNTAX = "SELECT ${NOT_EXISTING}";
	private static final String INDEX_PREFIX = "CREATE INDEX ${INDEX_PREFIX}";
	private static final String TABLE = "CREATE INDEX ${TABLE}";

	private static final String TAB_SCALAR_LONG = "CREATE INDEX ${TABLE/longProperty}";
	private static final String TAB_SCALAR_INTEGER = "CREATE INDEX ${TABLE/integerProperty}";
	private static final String TAB_SCALAR_BOOLEAN = "CREATE INDEX ${TABLE/booleanProperty}";
	private static final String TAB_SCALAR_STRING = "CREATE INDEX ${TABLE/stringProperty}";
	private static final String TAB_SCALAR_DATE = "CREATE INDEX ${TABLE/dateProperty}";
	private static final String TAB_SCALAR_ENUM = "CREATE INDEX ${TABLE/enumProperty}";
	private static final String TAB_SCALAR_MULTI_INHERITANCE = "CREATE INDEX ${TABLE/longProperty}, ${TABLE/subProperty}, ${TABLE/siblingProperty}";
	private static final String TAB_SCALAR_NOT_EXISTING = "CREATE INDEX ${TABLE/definitelyNotExisting}";
	private static final String TAB_SCALAR_BUT_COMPLEX = "CREATE INDEX ${TABLE/entityProperty}";
	private static final String TAB_SCALAR_MULTIPLE_1 = "CREATE INDEX ${TABLE/longProperty}";
	private static final String TAB_SCALAR_MULTIPLE_2 = "CREATE INDEX ${TABLE/integerProperty}";

	private static final String TABLE_PROPERTY_COLLECTION_NOT_EXISTING = "CREATE INDEX ${TABLE:definitelyNotExisting}";
	private static final String TABLE_PROPERTY_COLLECTION_BUT_SIMPLE = "CREATE INDEX ${TABLE:longProperty}";

	private static final String TABLE_PROPERTY_COLLECTION_SET_NOT_EXISTING = "CREATE INDEX ${TABLE:entitySet/definitelyNotExisting}";
	private static final String TABLE_PROPERTY_COLLECTION_SET_TO = "CREATE INDEX ${TABLE:entitySet/to}";
	private static final String TABLE_PROPERTY_COLLECTION_SET_FROM = "CREATE INDEX ${TABLE:entitySet/from}";
	private static final String TABLE_PROPERTY_COLLECTION_SET_POS_ERROR = "CREATE INDEX ${TABLE:entitySet/pos}";
	private static final String TABLE_PROPERTY_COLLECTION_SET_KEY_ERROR = "CREATE INDEX ${TABLE:entitySet/key}";

	private static final String TABLE_PROPERTY_COLLECTION_LIST_NOT_EXISTING = "CREATE INDEX ${TABLE:entityList/definitelyNotExisting}";
	private static final String TABLE_PROPERTY_COLLECTION_LIST_TO = "CREATE INDEX ${TABLE:entityList/to}";
	private static final String TABLE_PROPERTY_COLLECTION_LIST_FROM = "CREATE INDEX ${TABLE:entityList/from}";
	private static final String TABLE_PROPERTY_COLLECTION_LIST_POS = "CREATE INDEX ${TABLE:entityList/pos}";
	private static final String TABLE_PROPERTY_COLLECTION_LIST_KEY_ERROR = "CREATE INDEX ${TABLE:entityList/key}";

	private static final String TABLE_PROPERTY_COLLECTION_MAP_STING_TO_ENTITY_NOT_EXISTING = "CREATE INDEX ${TABLE:stringToEntityMap/definitelyNotExisting}";

	// -----------------------------------------------------------------------
	// SETUP AND TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void setu() {
		context = new HbmXmlGenerationContext();

		entityDescriptors = new ArrayList<>();

		tempDir = tempFolder.getRoot();
		context.outputFolder = tempDir;
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testSimpleAfterEmpty() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), EMPTY);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(EMPTY, afterFile());
	}

	@Test(expected = NullPointerException.class)
	public void testSimpleAfterNull() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), NULL);
	}

	@Test
	public void testSimpleAfterSimple() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), SIMPLE);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(SIMPLE, afterFile());
	}

	@Test
	public void testSimpleBeforeSimple() {
		before = true;
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), SIMPLE);

		Assert.assertEquals(SIMPLE, beforeFile());
		Assert.assertEquals(EMPTY, afterFile());
	}

	@Test(expected = TemplateException.class)
	public void testAfterWrongSyntax() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), WRONG_SYNTAX);
	}

	@Test
	public void testAfterIndexPrefixEmpty() {
		String indexNamePrefix = "";
		context.indexNamePrefix = indexNamePrefix;
		before = true;
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), INDEX_PREFIX);

		Assert.assertEquals(INDEX_PREFIX.replace("${INDEX_PREFIX}", indexNamePrefix), beforeFile());
		Assert.assertEquals(EMPTY, afterFile());
	}

	@Test
	public void testAfterIndexPrefixNull() {
		String indexNamePrefix = null;
		context.indexNamePrefix = indexNamePrefix;
		before = true;
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), INDEX_PREFIX);

		Assert.assertEquals(INDEX_PREFIX.replace("${INDEX_PREFIX}", ""), beforeFile());
		Assert.assertEquals(EMPTY, afterFile());
	}

	@Test
	public void testAfterIndexPrefix() {
		String indexNamePrefix = "MyPrefix_";
		context.indexNamePrefix = indexNamePrefix;
		before = true;
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), INDEX_PREFIX);

		Assert.assertEquals(INDEX_PREFIX.replace("${INDEX_PREFIX}", indexNamePrefix), beforeFile());
		Assert.assertEquals(EMPTY, afterFile());
	}

	@Test
	public void testAfterTable() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TABLE);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE.replace("${TABLE}", SimpleEntity.T.getShortName() + "_"), afterFile());
	}

	@Test
	public void testAfterTableSimpleLong() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_LONG);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_LONG.replace("${TABLE/longProperty}", SimpleEntity.longProperty), afterFile());
	}

	@Test
	public void testAfterTableSimpleInteger() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_INTEGER);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_INTEGER.replace("${TABLE/integerProperty}", SimpleEntity.integerProperty), afterFile());
	}

	@Test
	public void testAfterTableSimpleBoolean() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_BOOLEAN);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_BOOLEAN.replace("${TABLE/booleanProperty}", SimpleEntity.booleanProperty), afterFile());
	}

	@Test
	public void testAfterTableSimpleString() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_STRING);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_STRING.replace("${TABLE/stringProperty}", SimpleEntity.stringProperty), afterFile());
	}

	@Test
	public void testAfterTableSimpleDate() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_DATE);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_DATE.replace("${TABLE/dateProperty}", SimpleEntity.dateProperty), afterFile());
	}

	@Test
	public void testAfterTableSimpleEnum() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_ENUM);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_ENUM.replace("${TABLE/enumProperty}", SimpleEntity.enumProperty), afterFile());
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableSimpleNotExisting() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_NOT_EXISTING);
	}

	@Test
	public void testAfterTableSimpleButComplex() {
		runGenerator(asList(ComplexEntity.T, SimpleEntity.T), asSet(ComplexEntity.T), TAB_SCALAR_BUT_COMPLEX);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TAB_SCALAR_BUT_COMPLEX.replace("${TABLE/entityProperty}", "entityProperty"), afterFile());
	}

	@Test
	public void testAfterTable_MultiInheritance() {
		runGenerator(asList(SimpleSubEntity.T), asSet(SimpleSubEntity.T), TAB_SCALAR_MULTI_INHERITANCE);

		Assert.assertEquals(EMPTY, beforeFile());
		String expected = TAB_SCALAR_MULTI_INHERITANCE //
				.replace("${TABLE/longProperty}", SimpleSubEntity.longProperty) //
				.replace("${TABLE/subProperty}", SimpleSubEntity.subProperty) //
				.replace("${TABLE/siblingProperty}", SimpleSubEntity.siblingProperty);
		Assert.assertEquals(expected, afterFile());
	}

	@Test
	public void testAfterTableSimpleMultiple() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TAB_SCALAR_MULTIPLE_1, TAB_SCALAR_MULTIPLE_2);

		String expected = TAB_SCALAR_LONG.replace("${TABLE/longProperty}", SimpleEntity.longProperty) + "\n" + //
				TAB_SCALAR_INTEGER.replace("${TABLE/integerProperty}", SimpleEntity.integerProperty);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(expected, afterFile());
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableComplexPropertyNotExisting() {
		runGenerator(asList(ComplexEntity.T, SimpleEntity.T), asSet(ComplexEntity.T), TABLE_PROPERTY_COLLECTION_NOT_EXISTING);
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableComplexButSimple() {
		runGenerator(asList(SimpleEntity.T), asSet(SimpleEntity.T), TABLE_PROPERTY_COLLECTION_BUT_SIMPLE);
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionSetNotExisting() {
		runGenerator(asList(ComplexCollectionsEntity.T, ComplexCollectionsEntity.T), asSet(ComplexCollectionsEntity.T),
				TABLE_PROPERTY_COLLECTION_SET_NOT_EXISTING);
	}

	@Test
	public void testAfterTableCollectionSetTo() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_SET_TO);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE_PROPERTY_COLLECTION_SET_TO.replace("${TABLE:entitySet/to}", "SimpleEntityId"), afterFile());
	}

	@Test
	public void testAfterTableCollectionSetFrom() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_SET_FROM);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE_PROPERTY_COLLECTION_SET_FROM.replace("${TABLE:entitySet/from}", "ComplexCollectionsEntityId"), afterFile());
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionSetPosError() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_SET_POS_ERROR);
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionSetKeyError() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_SET_KEY_ERROR);
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionListNotExisting() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T),
				TABLE_PROPERTY_COLLECTION_LIST_NOT_EXISTING);
	}

	@Test
	public void testAfterTableCollectionListTo() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_LIST_TO);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE_PROPERTY_COLLECTION_LIST_TO.replace("${TABLE:entityList/to}", "SimpleEntityId"), afterFile());
	}

	@Test
	public void testAfterTableCollectionListFrom() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_LIST_FROM);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE_PROPERTY_COLLECTION_LIST_FROM.replace("${TABLE:entityList/from}", "ComplexCollectionsEntityId"), afterFile());
	}

	@Test
	public void testAfterTableCollectionListPos() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_LIST_POS);

		Assert.assertEquals(EMPTY, beforeFile());
		Assert.assertEquals(TABLE_PROPERTY_COLLECTION_LIST_POS.replace("${TABLE:entityList/pos}", "ComplexCollEntityEntityListIdx"), afterFile());
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionListKeyError() {
		runGenerator(asList(ComplexCollectionsEntity.T, SimpleEntity.T), asSet(ComplexCollectionsEntity.T), TABLE_PROPERTY_COLLECTION_LIST_KEY_ERROR);
	}

	@Test(expected = TemplateException.class)
	public void testAfterTableCollectionMapStringToEntityNotExisting() {
		runGenerator(asList(ComplexMapsEntity.T, SimpleEntity.T), asSet(ComplexMapsEntity.T),
				TABLE_PROPERTY_COLLECTION_MAP_STING_TO_ENTITY_NOT_EXISTING);
	}
	// TODO: test for supertype
	// TODO: test for subtype
	// TODO: test for loglevel
	// TODO: test for stacktrace
	// TODO: test for dialects
	// TODO: test for table name prefix

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private String beforeFile() {
		return file(true);
	}

	private String afterFile() {
		return file(false);
	}

	private String file(boolean before) {
		return DbUpdateStatementGenerator.readDbUpdateStatements(tempDir, before).stream() //
				.map(DbUpdateStatement::getExpression) //
				.collect(Collectors.joining("\n"));
	}

	/**
	 * Execute a {@link DbUpdateStatementGenerator} for a test model with types to be used for testing
	 * 
	 * @param modelTypes
	 *            types to be added to the test model
	 * @param entityMappingTypes
	 *            types of interest for {@link DbUpdateStatement}
	 * @param expressions
	 *            multiple {@link DbUpdateStatement#getExpression()} @ in case of an error
	 */
	private void runGenerator(List<EntityType<?>> modelTypes, Set<EntityType<?>> entityMappingTypes, String... expressions) {
		// prepare a model based on
		List<DbUpdateStatement> dbSchemaModifications = createDbUpdateStatements(expressions);

		metaModel = provideModel("test:db-update-statement-handler-model", modelTypes);

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		for (EntityType<?> type : entityMappingTypes) {
			EntityMapping entityMapping = EntityMapping.T.create();
			entityMapping.setTableName(type.getShortName() + "_");// to have a different name

			editor.onEntityType(type) //
					.addMetaData(entityMapping) //
					.addMetaData(dbSchemaModifications);
		}

		context.setGmMetaModel(metaModel);

		prepareEntityDescriptors();

		DbUpdateStatementGenerator.run(context, entityDescriptors);
	}

	private void prepareEntityDescriptors() {
		try {
			// simulate the steps from HbmXmlGenerator
			Map<String, HbmEntityType> hbmEntityTypeMap = new HbmEntityTypeMapBuilder(context).generateHbmEntityTypeMap();
			entityDescriptors = new EntityDescriptorFactory(context).createEntityDescriptors(hbmEntityTypeMap);
			entityDescriptors = new NamingStrategyProvider(context, entityDescriptors).apply();

		} catch (HbmXmlGeneratorException e) {
			throw new RuntimeException("Hbm generation failed.", e);
		}
	}

	private List<DbUpdateStatement> createDbUpdateStatements(String... expressions) {
		return Stream.of(expressions) //
				.map(this::newDbUpdateStatement) //
				.collect(Collectors.toList());
	}

	private DbUpdateStatement newDbUpdateStatement(String expression) {
		DbUpdateStatement result = DbUpdateStatement.T.create();
		result.setExpression(expression);
		result.setBefore(before);
		return result;
	}

	/**
	 * Provides the GmMetaModel with some essential metadata, e.g.: TypeSpecification
	 */
	private GmMetaModel provideModel(String name, List<EntityType<?>> types) {
		GmMetaModel metaModel = MetaModelTools.provideRawModel(name, types);

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

}
