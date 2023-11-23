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
package com.braintribe.model.processing.deployment.hibernate.mapping.render;

import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.processing.deployment.hibernate.mapping.SourceDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.CollectionPropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.ComponentDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityMappingContextTools;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EnumDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.PropertyDescriptor;

/**
 * @author peter.gazdik
 */
public class HbmXmlFastRenderer extends AbstractStringifier {

	public static SourceDescriptor renderEntityType(EntityDescriptor entityDescriptor) {
		SourceDescriptor result = new SourceDescriptor();
		result.sourceRelativePath = entityDescriptor.getFullName() + ".hbm.xml";
		result.sourceCode = renderXml(entityDescriptor);

		return result;
	}

	private static String renderXml(EntityDescriptor ed) {
		if (ed.xml != null)
			return ed.xml;
		else
			return new HbmXmlFastRenderer(ed).renderHbmXml();
	}

	private static final String Q = "\"";

	private final EntityDescriptor ed;

	private HbmXmlFastRenderer(EntityDescriptor ed) {
		this.ed = ed;

	}

	private String renderHbmXml() {
		println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		println();
		print("<hibernate-mapping auto-import=\"true\"");
		optAttr("schema", ed.schema);
		optAttr("catalog", ed.catalog);
		endOpenTag();

		levelUp();
		{
			openTag(ed.tag);
			attr("name", ed.fullName);
			attr("entity-name", ed.fullName);
			if (ed.isTopLevel)
				attr("table", ed.getQuotedTableName());
			attr("abstract", ed.abstractFlag());
			if (!ed.isTopLevel)
				attr("extends", ed.hbmSuperType);
			if (!ed.isTopLevel && !empty(ed.discriminatorValue))
				attr("discriminator-value", ed.discriminatorValue);
			endOpenTag();

			levelUp();
			{
				renderProperties();
			}
			levelDown();

			closeTag(ed.tag);
		}
		levelDown();

		println("</hibernate-mapping>");

		return builder.toString();
	}

	private void renderProperties() {
		for (PropertyDescriptor pd : ed.properties)
			render(pd);
	}

	private void render(PropertyDescriptor pd) {
		if (pd.xml != null)
			println(pd.xml.trim());
		else if (pd.getIsCollectionType())
			renderCollectionProperty((CollectionPropertyDescriptor) pd);

		else if (pd.getIsEnumType())
			renderEnumProperty((EnumDescriptor) pd);

		else if (pd.getIsCompositeIdProperty())
			renderCompositeId(pd);

		else if (pd.getIsEmbedded())
			renderEmbedded((ComponentDescriptor) pd);

		else if (pd.fkClass != null)
			renderManyToOne(pd);

		else if (!pd.isIdProperty && !pd.getHasColumnAttributes())
			renderNonIdNoColumnAttrs(pd);

		else {
			if (pd.isIdProperty && !pd.getHasColumnAttributes())
				renderIdNoColumnAttrs(pd);
			else
				renderWithColumnAttrs(pd);

			if (pd.isIdProperty) {
				levelUp();
				{
					openTag("generator");
					attr("class", pd.idGenerator);
					closeTag();
				}
				levelDown();
			}

			closeTag(pd.tag); // can be 'id' or 'property'

			renderDiscriminatorMaybe(pd);
		}

	}

	private void renderCollectionProperty(CollectionPropertyDescriptor pd) {
		openTag(pd.tag);
		attr("name", pd.name);
		if (!pd.isOneToMany)
			attr("table", pd.getQuotedMany2ManyTable());

		optAttr("lazy", pd.lazy);
		optAttr("cascade", pd.cascade);
		optAttr("fetch", pd.fetch);
		endOpenTag();

		levelUp();
		{
			openTag("key");
			attr("column", pd.getQuotedKeyColumn());
			optAttr("not-null", pd.keyColumnNotNull);
			optAttr("property-ref", pd.keyPropertyRef);
			optAttr("foreign-key", pd.foreignKey);
			closeTag();

			if (pd.getIsList()) {
				openTag("list-index");
				attr("column", pd.getQuotedIndexColumn());
				closeTag();

			} else if (pd.getIsMap()) {
				if (pd.isSimpleMapKey) {
					openTag("map-key");
					attr("column", pd.getQuotedMapKeyColumn());
					attr("type", pd.mapKeySimpleType);
					optAttr("length", pd.mapKeyLength);
					closeTag();
				} else {
					openTag("map-key-many-to-many");
					attr("column", pd.getQuotedMapKeyColumn());
					attr("class", pd.mapKeySignature);
					optAttr("foreign-key", pd.mapKeyForeignKey);
					closeTag();
				}
			}

			if (pd.isSimpleCollection) {
				openTag("element");
				attr("column", pd.getQuotedElementColumn());
				attr("type", pd.elementSimpleType);

				optAttr("length", pd.length);
				optAttr("precision", pd.precision);
				optAttr("scale", pd.scale);

				optAttr("unique", pd.isUnique);
				optAttr("not-null", pd.isNotNull);

				closeTag();

			} else if (pd.isOneToMany) {
				openTag("many-to-many");
				attr("class", pd.elementSignature);
				closeTag();

			} else {
				openTag("many-to-many");
				attr("class", pd.elementSignature);
				attr("column", pd.getQuotedElementColumn());

				optAttr("unique", pd.isUnique);
				optAttr("fetch", pd.manyToManyFetch);
				optAttr("foreign-key", pd.elementForeignKey);

				closeTag();
			}
		}

		levelDown();
		closeTag(pd.tag);
	}

	private void renderEnumProperty(EnumDescriptor pd) {
		openTag("property");
		attr("name", pd.name);
		attr("column", pd.getQuotedColumnName());
		optAttr("unique", pd.isUnique);
		optAttr("not-null", pd.isNotNull);
		optAttr("index", pd.index);
		endOpenTag();

		levelUp();
		{
			openTag("type");
			attr("name", "org.hibernate.type.EnumType");
			endOpenTag();

			levelUp();
			{
				println("<param name=\"enumClass\">" + pd.enumClass + "</param>");
				println("<param name=\"type\">" + pd.enumSqlType + "</param>");
			}
			levelDown();

			closeTag("type");
		}
		levelDown();

		closeTag("property");
	}

	private void renderCompositeId(PropertyDescriptor pd) {
		println("<composite-id name=\"id\" class=\"com.braintribe.model.access.hibernate.gm.CompositeIdValues\">");
		levelUp();

		int i = 0;
		for (JpaColumn c : pd.getCompositeId().getColumns()) {
			openTag("key-property");
			attr("name", "value" + i++);
			attr("column", EntityMappingContextTools.quoteIdentifier(c.getName()));
			attr("type", c.getType());
			closeTag();
		}

		levelDown();
		closeTag("composite-id");
	}

	private void renderEmbedded(ComponentDescriptor pd) {
		openTag("component");
		attr("name", pd.name);
		attr("class", pd.fkClass);
		endOpenTag();

		levelUp();
		{
			println("<tuplizer entity-mode=\"pojo\" class=\"com.braintribe.model.access.hibernate.gm.GmPojoComponentTuplizer\"/>");
			println("<property name=\"id\" formula=\"(select concat('" + pd.getIdPrefix() + "', " + pd.getOwnerIdQuotedColumnName()
					+ "))\" type=\"string\" />");

			for (PropertyDescriptor embeddedPd : pd.getEmbeddedProperties())
				render(embeddedPd);

		}
		levelDown();

		closeTag("component");
	}

	private void renderManyToOne(PropertyDescriptor pd) {
		openTag("many-to-one");
		attr("name", pd.name);
		attr("class", pd.fkClass);
		attr("column", pd.getQuotedColumnName());

		optAttr("unique", pd.isUnique);
		optAttr("not-null", pd.isNotNull);
		optAttr("index", pd.index);

		optAttr("lazy", pd.lazy);
		optAttr("cascade", pd.cascade);
		optAttr("fetch", pd.fetch);

		if (Boolean.TRUE.equals(pd.isInvalidReferencesIgnored))
			attr("not-found", "ignore");

		optAttr("property-ref", pd.referencedProperty);
		optAttr("foreign-key", pd.foreignKey);

		if (Boolean.TRUE.equals(pd.isReadOnly)) {
			attr("insert", "false");
			attr("update", "false");
		}

		closeTag();
	}

	private void renderNonIdNoColumnAttrs(PropertyDescriptor pd) {
		openTag(pd.tag);
		attr("name", pd.name);
		attr("column", pd.getQuotedColumnName());

		optAttr("type", pd.explicitType);
		optAttr("lazy", pd.lazy);
		if (Boolean.TRUE.equals(pd.isReadOnly)) {
			attr("insert", "false");
			attr("update", "false");
		}

		optAttr("length", pd.length);
		optAttr("precision", pd.precision);
		optAttr("scale", pd.scale);

		optAttr("unique", pd.isUnique);
		optAttr("not-null", pd.isNotNull);

		optAttr("index", pd.index);
		optAttr("unique-key", pd.uniqueKey);
		optAttr("access", pd.access);
		optAttr("node", pd.node);
		optAttr("formula", pd.formula);
		optAttr("optimistic-lock", pd.isOptimisticLock);

		closeTag();
	}

	private void renderIdNoColumnAttrs(PropertyDescriptor pd) {
		openTag(pd.tag); // id
		attr("name", pd.name);
		attr("column", pd.getQuotedColumnName());

		optAttr("type", pd.explicitType);

		optAttr("length", pd.length);
		optAttr("precision", pd.precision);
		optAttr("scale", pd.scale);

		optAttr("index", pd.index);
		optAttr("unique-key", pd.uniqueKey);
		optAttr("access", pd.access);
		optAttr("node", pd.node);
		optAttr("formula", pd.formula);
		optAttr("optimistic-lock", pd.isOptimisticLock);

		endOpenTag();
	}

	private void renderWithColumnAttrs(PropertyDescriptor pd) {
		openTag(pd.tag);
		attr("name", pd.name);
		attr("column", pd.getQuotedColumnName());
		optAttr("type", pd.explicitType);

		optAttr("lazy", pd.lazy);
		if (Boolean.TRUE.equals(pd.isReadOnly)) {
			attr("insert", "false");
			attr("update", "false");
		}

		optAttr("length", pd.length);
		optAttr("precision", pd.precision);
		optAttr("scale", pd.scale);

		optAttr("unique", pd.isUnique);
		optAttr("not-null", pd.isNotNull);

		optAttr("index", pd.index);
		optAttr("unique-key", pd.uniqueKey);
		optAttr("access", pd.access);
		optAttr("node", pd.node);
		optAttr("formula", pd.formula);
		optAttr("optimistic-lock", pd.isOptimisticLock);

		endOpenTag();

		levelUp();
		{
			openTag("column");
			optAttr("sql-type", pd.sqlType);
			optAttr("check", pd.check);
			optAttr("default", pd.defaultValue);
			optAttr("write", pd.write);
			closeTag();

		}
		levelDown();
		// @formatter:on
	}

	private void renderDiscriminatorMaybe(PropertyDescriptor pd) {
		EntityDescriptor ped = pd.entityDescriptor;
		if (pd.isIdProperty && ped.isTopLevel && ped.hasSubTypes) {
			print("<discriminator");
			if (empty(ped.discriminatorFormula)) {
				attr("column", ped.getQuotedDiscriminatorColumnName());
				attr("type", valueOrDefault(ped.discriminatorType, "string"));
			} else {
				attr("formula", ped.discriminatorFormula);
			}
			closeTag();
		}
	}

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	private void optAttr(String name, Object value) {
		if (value != null)
			attr(name, value.toString());
	}

	private void optAttr(String name, String value) {
		if (!empty(value))
			attr(name, value);
	}

	private void attr(String name, String value) {
		print(" ");
		print(name);
		print("=");
		print(Q);
		print(value);
		print(Q);
	}

	private void openTag(String tag) {
		print("<");
		print(tag);
	}

	private void endOpenTag() {
		println(">");
	}

	private void closeTag(String tag) {
		println("</" + tag + ">");
	}

	private void closeTag() {
		println(" />");
	}

	private String valueOrDefault(String s, String _default) {
		return empty(s) ? _default : s;
	}

	private boolean empty(String s) {
		return s == null || s.isEmpty();
	}

}
