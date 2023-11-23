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
package com.braintribe.model.processing.test.jta;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Bidirectional;
import com.braintribe.model.meta.data.constraint.CompoundUnique;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.NonDeletable;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.display.Color;
import com.braintribe.model.meta.data.display.Emphasized;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.meta.data.mapping.PositionalArguments;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysisException;
import com.braintribe.model.processing.test.jta.model.EntityWithAnnotations;
import com.braintribe.model.processing.test.jta.model.EnumWithAnnotations;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.i18n.I18nTools;

/**
 * @author peter.gazdik
 */
public class JtaAnnotationProcessingTest {

	private Map<EntityType<? extends MetaData>, List<MetaData>> entityMd;
	private Map<String, Map<EntityType<? extends MetaData>, List<MetaData>>> propertyMd;
	private Map<EntityType<? extends MetaData>, List<MetaData>> enumMd;
	private Map<String, Map<EntityType<? extends MetaData>, List<MetaData>>> constantMd;

	/** Testing MD annotations of {@link EntityWithAnnotations} */
	@Test
	public void checkMdAnnotations() {
		runJta();

		// Entity MD
		assertSelectiveInformation();
		assertCompountUnique();

		assertEntityMd(PositionalArguments.T, "properties", asList("unique", "bidi"));
		assertEntityMd(NonDeletable.T);

		// Property MD
		assertName();
		assertNames();
		assertDescription();
		assertDescriptions();
		assertAliasedMulti();

		assertPropertyMd("unique", Unique.T);
		assertPropertyMd("bidi", Bidirectional.T);
		assertPropertyMd("hasLimit", Min.T, "limit", 0);
		assertPropertyMd("hasLimit", Max.T, "limit", 100);
		assertPropertyMd("aliased", Alias.T, "name", "alias");
		assertPropertyMd("prioritized", Priority.T, "priority", 666.666D);
		assertPropertyMd("emphasized", Emphasized.T);
		assertPropertyMd("colored", Color.T, "code", "#0F0");
		assertPropertyMd("deprecated", Deprecated.T);

		// enumMd
		assertEnumDescription();

		assertConstantMd("constant1", Color.T, "code", "red");
	}

	private void assertEnumDescription() {
		Description d = first(getEnumMd(Description.T));

		assertThat(I18nTools.getDefault(d.getDescription())).isEqualTo("enum description");
	}

	private void runJta() throws JavaTypeAnalysisException {
		GenericEntity.T.getTypeSignature(); // init JTA

		JavaTypeAnalysis jta = new JavaTypeAnalysis();
		GmEntityType type = (GmEntityType) jta.getGmType(EntityWithAnnotations.class);
		GmEnumType enumType = (GmEnumType) jta.getGmType(EnumWithAnnotations.class);

		entityMd = mdsByType(type);
		enumMd = mdsByType(enumType);

		propertyMd = indexPropOrConstMd(type.getProperties(), GmProperty::getName);
		constantMd = indexPropOrConstMd(enumType.getConstants(), GmEnumConstant::getName);
	}

	private <T extends HasMetaData> Map<String, Map<EntityType<? extends MetaData>, List<MetaData>>> indexPropOrConstMd( //
			List<T> propsOrConstants, Function<T, String> nameResolver) {

		return propsOrConstants.stream() //
				.filter(p -> !p.getMetaData().isEmpty()) //
				.collect( //
						Collectors.toMap( //
								nameResolver, //
								p -> mdsByType(p) //
						) //
				);
	}

	private Map<EntityType<? extends MetaData>, List<MetaData>> mdsByType(HasMetaData mdOwner) {
		Set<MetaData> md = mdOwner.getMetaData();
		assertThat(md).as("MD of " + mdOwner.entityType().getShortName() + " should not be null.").isNotNull();

		return md //
				.stream().collect( //
						Collectors.groupingBy( //
								GenericEntity::entityType //
						) //
				);
	}

	private void assertSelectiveInformation() {
		SelectiveInformation si = first(getEntityMd(SelectiveInformation.T));

		assertThat(I18nTools.getDefault(si.getTemplate())).isEqualTo("Selective INFORMATION");
	}

	private void assertCompountUnique() {
		List<CompoundUnique> md = getEntityMd(CompoundUnique.T);
		Iterator<CompoundUnique> it = md.iterator();

		Set<String> props1 = it.next().getUniqueProperties();
		Set<String> props2 = it.next().getUniqueProperties();

		if (props1.size() > props2.size()) {
			Set<String> tmp = props1;
			props1 = props2;
			props2 = tmp;
		}

		Assertions.assertThat(props1).containsExactly("compoundUniqueSingle");
		Assertions.assertThat(props2).containsExactly("compoundUnique1", "compoundUnique2");
	}

	private void assertName() {
		List<Name> mds = getPropertyMd("propertyWithName", Name.T);
		Assertions.assertThat(mds).hasSize(1);

		Name md = first(mds);

		Assertions.assertThat(md.getGlobalId())
				.isEqualTo("Name:property:com.braintribe.model.processing.test.jta.model.EntityWithAnnotations/propertyWithName");
		assertLocalizedValues(md.getName(), "default", "default name");
	}

	private void assertNames() {
		List<Name> mds = getPropertyMd("propertyWithNames", Name.T);
		Assertions.assertThat(mds).hasSize(1);

		Name md = first(mds);

		Assertions.assertThat(md.getGlobalId()).isEqualTo("md.names");
		assertLocalizedValues(md.getName(), "default", "default name", "de", "Der Name", "br", "O Nome");
	}

	private void assertDescription() {
		List<Description> mds = getPropertyMd("propertyWithDescription", Description.T);
		Assertions.assertThat(mds).hasSize(1);

		Description md = first(mds);

		Assertions.assertThat(md.getGlobalId()).isEqualTo("md.description");
		assertLocalizedValues(md.getDescription(), "default", "default description");
	}

	private void assertDescriptions() {
		List<Description> mds = getPropertyMd("propertyWithDescriptions", Description.T);
		Assertions.assertThat(mds).hasSize(1);

		Description md = first(mds);

		Assertions.assertThat(md.getGlobalId())
				.isEqualTo("Description:property:com.braintribe.model.processing.test.jta.model.EntityWithAnnotations/propertyWithDescriptions");
		assertLocalizedValues(md.getDescription(), "default", "default description", "de", "Unfug");
	}

	private void assertAliasedMulti() {
		List<Alias> mds = getPropertyMd("aliasedMulti", Alias.T);
		Assertions.assertThat(mds).hasSize(2);

		Set<String> aliases = mds.stream() //
				.map(Alias::getName) //
				.collect(Collectors.toSet());

		assertThat(aliases).containsExactly("a", "ALIAS");
	}

	// #############################################
	// ## . . . . . Generic MD assertions . . . . ##
	// #############################################

	private <T extends MetaData> void assertEntityMd(EntityType<T> mdType, Object... expectedPropValues) {
		List<T> mds = getEntityMd(mdType);
		assertSingleMd(mdType, mds, expectedPropValues);
	}

	private void assertPropertyMd(String propertyName, EntityType<? extends MetaData> mdType, Object... expectedPropValues) {
		assertPropOrConstMd(propertyMd, propertyName, mdType, expectedPropValues);
	}

	private void assertConstantMd(String constantName, EntityType<? extends MetaData> mdType, Object... expectedPropValues) {
		assertPropOrConstMd(constantMd, constantName, mdType, expectedPropValues);
	}

	private void assertPropOrConstMd(Map<String, Map<EntityType<? extends MetaData>, List<MetaData>>> mdMap, //
			String memberName, EntityType<? extends MetaData> mdType, Object... expectedPropValues) {

		Map<EntityType<? extends MetaData>, List<MetaData>> mdForProperty = mdMap.get(memberName);

		Assertions.assertThat(mdForProperty).isNotNull().containsKey(mdType);

		List<MetaData> mds = mdForProperty.get(mdType);

		assertSingleMd(mdType, mds, expectedPropValues);
	}

	private void assertSingleMd(EntityType<?> mdType, List<? extends MetaData> mds, Object... expectedMdPropValues) {
		Assertions.assertThat(mds).hasSize(1);

		MetaData md = first(mds);
		Map<String, Object> mdPropValues = asMap(expectedMdPropValues);

		for (Entry<String, Object> entry : mdPropValues.entrySet()) {
			String mdPropName = entry.getKey();
			Object expectedValue = entry.getValue();

			Object mdPropValue = mdType.getProperty(mdPropName).get(md);
			Assertions.assertThat(mdPropValue).isEqualTo(expectedValue);
		}
	}

	// #############################################
	// ## . . . . . . . . Helpers . . . . . . . . ##
	// #############################################

	@SuppressWarnings("unused")
	private <T extends MetaData> T getSingleEntityMd(EntityType<T> mdType) {
		List<MetaData> md = entityMd.get(mdType);
		Assertions.assertThat(md).hasSize(1);
		return (T) first(md);
	}

	private <T extends MetaData> List<T> getEntityMd(EntityType<T> mdType) {
		List<MetaData> md = entityMd.get(mdType);
		Assertions.assertThat(md).as("Entity MD not found: " + mdType.getShortName()).isNotEmpty();
		return (List<T>) md;
	}

	private <T extends MetaData> List<T> getPropertyMd(String propertyName, EntityType<T> mdType) {
		Map<EntityType<? extends MetaData>, List<MetaData>> mdForProperty = propertyMd.get(propertyName);
		Assertions.assertThat(mdForProperty).isNotNull().containsKey(mdType);

		List<MetaData> md = mdForProperty.get(mdType);
		Assertions.assertThat(md).isNotEmpty();
		return (List<T>) md;
	}

	private <T extends MetaData> List<T> getEnumMd(EntityType<T> mdType) {
		List<MetaData> md = enumMd.get(mdType);
		Assertions.assertThat(md).as("Enum MD not found: " + mdType.getShortName()).isNotEmpty();
		return (List<T>) md;
	}

	private void assertLocalizedValues(LocalizedString ls, String... localizedValues) {
		Map<String, String> expectedLocalizedValues = asMap((Object[]) localizedValues);

		assertThat(ls.getLocalizedValues()).isNotEmpty().isEqualTo(expectedLocalizedValues);
	}

}
