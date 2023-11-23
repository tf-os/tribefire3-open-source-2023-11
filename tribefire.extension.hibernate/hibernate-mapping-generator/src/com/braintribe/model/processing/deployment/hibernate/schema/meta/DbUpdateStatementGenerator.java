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

import static com.braintribe.utils.lcd.CollectionTools2.index;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.hibernate.meta.DbUpdateStatement;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.CollectionPropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.PropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;

/**
 * A utility class to prepare the SQL statements from {@link DbUpdateStatement} metadata. The {@link DbUpdateStatement#getExpression() expression} is
 * an SQL expression with dyanic values according to {@link DbUpdateStatementGenerator#getSyntaxDescription()}.
 * 
 * These SQL statements are written into temporary files (same directory as the hbm files) in YML format.
 * 
 * This needs to be done after all {@link EntityDescriptor} are finally processed (e.g. prepared from metadata, modified with DB naming
 * strategies,...)
 * 
 * Based on these files the manipulations will be applied <b>before</b> or <b>after</b> the Hibernate mapping update.
 * 
 */
public class DbUpdateStatementGenerator {

	protected static Logger log = Logger.getLogger(DbUpdateStatementGenerator.class);

	private static final String BEFORE_YML_FILE = "sql-statements-before.json";
	private static final String AFTER_YML_FILE = "sql-statements-after.json";

	private final HbmXmlGenerationContext context;
	private final Collection<EntityDescriptor> entityDescriptors;

	private static final String TO = "to";
	private static final String FROM = "from";
	private static final String KEY = "key";
	private static final String POS = "pos";

	public static void run(HbmXmlGenerationContext context, Collection<EntityDescriptor> entityDescriptors) {
		new DbUpdateStatementGenerator(context, entityDescriptors).run();
	}

	private DbUpdateStatementGenerator(HbmXmlGenerationContext context, Collection<EntityDescriptor> entityDescriptors) {
		this.context = context;
		this.entityDescriptors = entityDescriptors;
	}

	private final List<DbUpdateStatement> befores = newList();
	private final List<DbUpdateStatement> afters = newList();

	private EntityDescriptor entityDescriptor;
	private String typeSignature;
	private String propertyName;
	private String var;
	private Map<String, PropertyDescriptor> pDescs;
	private DbUpdateStatement dbMd;

	private void run() {
		doPrepare();

		writeDbSchemaModifications(BEFORE_YML_FILE, befores);
		writeDbSchemaModifications(AFTER_YML_FILE, afters);
	}

	private void doPrepare() {
		for (EntityDescriptor ed : entityDescriptors) {
			entityDescriptor = ed;
			typeSignature = ed.getHbmEntityType().getType().getTypeSignature();
			pDescs = pDescsByName(ed);

			List<DbUpdateStatement> dbSchemaModifications = ed.getUpdateStatements();
			log.trace(() -> "Applying " + StringTools.createStringFromCollection(dbSchemaModifications, ",") + "'");

			for (DbUpdateStatement dsm : dbSchemaModifications) {
				dbMd = dsm;

				/// Actual generation:
				DbUpdateStatement statement = resolveDbUpdateStatement();

				if (dsm.getBefore())
					befores.add(statement);
				else
					afters.add(statement);
			}
		}
	}

	private Map<String, PropertyDescriptor> pDescsByName(EntityDescriptor ed) {
		return index(allPropDescs(ed)).by(PropertyDescriptor::getPropertyName).unique();
	}

	private List<PropertyDescriptor> allPropDescs(EntityDescriptor ed) {
		List<PropertyDescriptor> result = newList();

		do {
			result.addAll(ed.getProperties());
			ed = ed.parent;

		} while (ed != null);

		return result;
	}

	private DbUpdateStatement resolveDbUpdateStatement() {
		requireNonNull(dbMd.getExpression(), "'expression' of '" + dbMd + "' needs to be set");

		String sql = resolveTemplate(dbMd.getExpression());

		DbUpdateStatement result = GmReflectionTools.makeShallowCopy(dbMd);
		result.setExpression(sql);

		return result;
	}

	private String resolveTemplate(String expression) {
		Template template = Template.parse(expression);

		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(this::resolveTemplateVariable);
		String sql = template.merge(mergeContext);

		log.info(() -> "Replaced expression: '" + expression + "' to sql: '" + sql + "'");

		return sql;
	}

	private Object resolveTemplateVariable(String var) {
		switch (var) {
			case "INDEX_PREFIX":
				return valueOrEmpty(context.indexNamePrefix);
			case "TABLE_PREFIX":
				return valueOrEmpty(context.tablePrefix);
			case "FOREIGN_KEY_PREFIX":
				return valueOrEmpty(context.foreignKeyNamePrefix);
			case "UNIQUE_KEY_PREFIX":
				return valueOrEmpty(context.uniqueKeyNamePrefix);
			case "TABLE":
				return currentTableName();
			case "DISCRIMINATOR":
				return valueOrEmpty(discriminatorColumnName());

			default: {
				if (var.startsWith("TABLE/"))
					return resolveScalarOrEntityPropertyColumn(var);

				if (var.startsWith("TABLE:"))
					return resolveCollectionProperty(var);

				throw new IllegalArgumentException(createErrorMessage("Unrecognized variable"));
			}
		}
	}

	private String discriminatorColumnName() {
		EntityDescriptor ed = entityDescriptor;
		while (ed.parent != null)
			ed = ed.parent;

		return ed.getDiscriminatorColumnName();
	}

	private static String valueOrEmpty(String s) {
		return NullSafe.get(s, "");
	}

	// ${TABLE/property} -> Property Column
	private Object resolveScalarOrEntityPropertyColumn(String var) {
		String[] parts = var.split("/");
		if (parts.length != 2)
			throw new IllegalArgumentException(createErrorMessage("No property defined"));

		propertyName = parts[1];

		PropertyDescriptor pd = getPropertyDescriptor();
		if (!((pd.getGmType().isGmScalar()) || (pd.getGmType().isGmCustom())))
			throw new IllegalArgumentException(createErrorMessage("Property needs to be a simple property"));

		return pd.getQuotedColumnName();
	}

	// TABLE:collectionProperty[/columnSpecification]
	private Object resolveCollectionProperty(String _var) {
		var = _var;
		String[] parts = var.split(":");
		if (parts.length != 2)
			throw new IllegalArgumentException(createErrorMessage("No property defined"));

		String[] subParts = parts[1].split("/");
		propertyName = subParts[0];
		if (subParts.length == 1)
			return resolveRelationshipTable();

		if (subParts.length == 2)
			return resolveRelationshipTableColumn(subParts[1]);

		throw new IllegalArgumentException(createErrorMessage("Property wrongly defined"));
	}

	// ${TABLE:property} -> Relationship Table
	private String resolveRelationshipTable() {
		return getCollectionPropertyDescriptor().getQuotedMany2ManyTable();
	}

	// ${TABLE:property/pos} -> Relationship Table - List - Position Column
	// ${TABLE:property/key} -> Relationship Table - Map - Key Column
	// ${TABLE:property/from} -> Relationship Table - Collection - From Column
	// ${TABLE:property/to} -> Relationship Table - Collection - To Column
	private Object resolveRelationshipTableColumn(String columnSpecification) {
		CollectionPropertyDescriptor pd = getCollectionPropertyDescriptor();

		String to = pd.getElementColumn(); // to column
		String from = pd.getKeyColumn(); // from column
		String pos = pd.getIndexColumn();// pos column

		switch (pd.getGmType().typeKind()) {
			case SET: {
				if (columnSpecification.equals(TO))
					return to;
				if (columnSpecification.equals(FROM))
					return from;
				// createErrorMessage(var, typeSignature, "Property wrongly defined")
				throw new IllegalStateException("Column specification '" + columnSpecification + "' not supported for set");
			}

			case LIST: {
				if (columnSpecification.equals(TO))
					return to;
				if (columnSpecification.equals(FROM))
					return from;
				if (columnSpecification.equals(POS))
					return pos;

				throw new IllegalStateException("Column specification '" + columnSpecification + "' not supported for list.");
			}

			case MAP: {
				if (columnSpecification.equals(TO))
					return to;
				if (columnSpecification.equals(FROM))
					return from;
				if (columnSpecification.equals(KEY))
					return pd.getMapKeyColumn();

				throw new IllegalStateException("Column specification '" + columnSpecification + "' not supported for map.");
			}

			default:
				throw new IllegalStateException("Property type '" + pd.getGmType() + "' not supported. This should never happen.");
		}
	}

	private CollectionPropertyDescriptor getCollectionPropertyDescriptor() {
		PropertyDescriptor pd = getPropertyDescriptor();

		if (!(pd instanceof CollectionPropertyDescriptor))
			throw new IllegalArgumentException(createErrorMessage("Property is not a collection: " + propertyName));

		return (CollectionPropertyDescriptor) pd;
	}

	private PropertyDescriptor getPropertyDescriptor() {
		PropertyDescriptor pd = pDescs.get(propertyName);
		if (pd == null)
			throw new IllegalArgumentException(createErrorMessage("Column name could not get calculated from property name: '" + propertyName
					+ "'. Does the property exists in entity: '" + typeSignature + "'"));
		return pd;
	}

	private Object currentTableName() {
		if (entityDescriptor.getIsTopLevel())
			return entityDescriptor.getQuotedTableName();
		else
			return calculateSuperType(entityDescriptor).getQuotedTableName();
	}

	public static List<DbUpdateStatement> readDbUpdateStatements(File folder, boolean before) {
		return readStatements(folder, before ? BEFORE_YML_FILE : AFTER_YML_FILE);
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private EntityDescriptor calculateSuperType(EntityDescriptor entityDescriptor) {
		HbmEntityType superType = entityDescriptor.getHbmEntityType().getSuperType();
		if (superType == null)
			return entityDescriptor;

		for (EntityDescriptor ed : entityDescriptors)
			if (ed.getHbmEntityType() == superType)
				return calculateSuperType(ed);

		return entityDescriptor;
	}

	private void writeDbSchemaModifications(String fileName, List<DbUpdateStatement> entries) {
		if (entries.isEmpty())
			return;

		File file = new File(context.outputFolder, fileName);

		FileTools.write(file).usingOutputStream(os -> new JsonStreamMarshaller().marshall(os, entries));

		log.debug(() -> "Successfully wrote file: '" + file.getAbsolutePath() + "' with '" + entries.size() + "' entries");
	}

	private static List<DbUpdateStatement> readStatements(File folder, String fileName) {
		File file = new File(folder, fileName);

		if (!file.exists())
			return emptyList();

		List<DbUpdateStatement> result = (List<DbUpdateStatement>) FileTools.read(file).fromInputStream(new JsonStreamMarshaller()::unmarshall);
		log.debug(() -> "Successfully read file '" + file.getAbsolutePath() + "' with '" + result.size() + "' entries");

		return result;

	}

	private String createErrorMessage(String details) {
		return "Could not resolve variable " + "'" + var + "' of template '" + dbMd.getExpression() + "' on entity '" + typeSignature + "'. Details: "
				+ details + ". Proper syntax: " + getSyntaxDescription();
	}

	private String getSyntaxDescription() {
		//@formatter:off
		return  "\n-------------------------------------------------------------------------------"+
				"\nAvailable Syntax:\n" +
				"\t${TABLE_PREFIX} -> HibernateAccess.tableNamePrefix\n" +
				"\t${INDEX_PREFIX} -> HibernateAccess.indexNamePrefix\n" +
				"\t${FOREIGN_KEY_PREFIX} -> HibernateAccess.foreignKeyNamePrefix\n" +
				"\t${UNIQUE_KEY_PREFIX} -> HibernateAccess.uniqueKeyNamePrefix\n" +
				"\t${DISCRIMINATOR} -> Discriminator column of main table\n" +
				"\t${TABLE} -> Main Table - refer to HibernateAccess\n" +
				"\t${TABLE/property} -> Property Column\n" +
				"\t${TABLE:collectionProperty} -> Relationship Table\n" +
				"\t${TABLE:collectionProperty/pos} -> Relationship Table - List - Position Column\n" +
				"\t${TABLE:collectionProperty/key} -> Relationship Table - Map - Key Column\n" +
				"\t${TABLE:collectionProperty/from} -> Relationship Table - Collection - From Column\n" +
				"\t${TABLE:collectionProperty/to} -> Relationship Table - Collection - To Column\n" +
		        "-------------------------------------------------------------------------------";
		//@formatter:on
	}

}
