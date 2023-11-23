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
package com.braintribe.model.access;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport;
import com.braintribe.model.access.BasicAccessAdapter.ListEntry;
import com.braintribe.model.access.BasicAccessAdapter.MapEntry;
import com.braintribe.model.access.impls.ReportTestingAccess;
import com.braintribe.model.access.model.BasicAccesAdapterTestModelProvider;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.access.model.Library;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.testing.category.KnownIssue;

public class BasicAccessAdapterReportTest {

	private static Library la, lb;
	private static Book ba1, ba2, bb1, bb2;
	private static Map<String, String> ba1Prop;
	private static Map<String, String> ba2Prop;
	private static Map<String, String> bb1Prop;
	private static Map<String, String> bb2Prop;
	private static List<String> ba1Author;
	private static List<String> ba2Author;
	private static List<String> bb1Author;
	private static List<String> bb2Author;
	
	
	static {
		ba1Prop = new HashMap<String, String>();
		ba2Prop = new HashMap<String, String>();
		bb1Prop = new HashMap<String, String>();
		bb2Prop = new HashMap<String, String>();
		ba1Prop.put("Name", "abc");
		ba1Prop.put("Publisher", "braintribe");
		ba2Prop.put("Name", "abcdef");
		ba2Prop.put("Publisher", "braintribe");
		bb1Prop.put("Name", "xyz");
		bb1Prop.put("Publisher", "braintribe");
		bb2Prop.put("Name", "qwert");
		bb2Prop.put("Publisher", "braintribe");
		ba1Author = new ArrayList<String>();
		ba2Author = new ArrayList<String>();
		bb1Author = new ArrayList<String>();
		bb2Author = new ArrayList<String>();
		ba1Author.add("Braintribe");
		ba1Author.add("tribefire");
		ba1Author.add("Wien");
		ba2Author.add("Braintribe");
		ba2Author.add("Wien");
		ba2Author.add("tribefire");
		bb1Author.add("Wien");
		bb1Author.add("Braintribe");
		bb1Author.add("tribefire");
		bb2Author.add("tribefire");
		bb2Author.add("Wien");
		bb2Author.add("Braintribe");
	}

	private static Library newLibrary(String name) {
		Library result = Library.T.create();
		result.setName(name);

		return result;
	}

	private static Book newBook(String title, int pages, Library library, String chars, List<String> writer, Map<String, String> properties) {
		Book result = Book.T.create();
		result.setTitle(title);
		result.setPages(pages);
		result.setLibrary(library);
		
		Set<String> charsUsed = new HashSet<String>();
		for (char c : chars.toCharArray()) {
			charsUsed.add(String.valueOf(c));
		}
		result.setCharsUsed(charsUsed);
		
		result.setWriter(writer);
		result.setProperties(properties);
		
		return result;
	}

	private static Smood configureSmood() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(BasicAccesAdapterTestModelProvider.INSTANCE.get());

		la = newLibrary("la");
		lb = newLibrary("lb");

		ba1 = newBook("ba1", 100, la, "abcdefg", ba1Author, ba1Prop);
		ba2 = newBook("ba2", 200, la, "abcd", ba2Author, ba2Prop);
		bb1 = newBook("bb1", 300, lb, "xyz", bb1Author, bb1Prop);
		bb2 = newBook("bb2", 400, lb, "qwerty", bb2Author, bb2Prop);

		smood.initialize(Arrays.asList(la, lb, ba1, ba2, bb1, bb2));

		return smood;
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeSetForClearCollectionManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getCharsUsed().clear();
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		Set<Object> removedItems = report.getRemovedElementsForPropertyOfTypeSet(entity, property);
		
		assertThat(removedItems).containsOnly("x", "y", "z");
	}

	/**
	 * Regarding {@link KnownIssue} - These tests started to fail when we changed manipulation tracking to also record
	 * {@link RemoveManipulation}s in case the element was not in the actual collection. This is relevant for example in a case (which is
	 * currently not supported, but we will probably add it later) when we have not loaded the collection at all, but we call a remove. In
	 * this case, we do not need to load the collection, but simply creates the RemoveManipulation and locally store the information that
	 * the element was removed (so we handle it correctly if we have to load the collection later).
	 * 
	 * The thing is now, me (PGA) and Yaroslav were not sure what the expected {@link ManipulationReport} should look like in this case, so
	 * for now I do not change the test to also expect this remove to be part of the result. Somebody who knows what the
	 * {@link ManipulationReport} is all about will have to review this. But seems currently nobody has an issue with this.
	 */
	@Test
	@Category(KnownIssue.class)
	public void EXPTECTED_TO_FAIL_testGetRemovedElementsForPropertyOfTypeSetForRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getCharsUsed().remove("a");
		book.getCharsUsed().remove("y");
		book.getCharsUsed().remove("z");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		Set<Object> removedItems = report.getRemovedElementsForPropertyOfTypeSet(entity, property);
		
		assertThat(removedItems).containsOnly("y", "z");
	}

	private ReportTestingAccess newReportTestingAccess(Smood smood) {
		ReportTestingAccess result = new ReportTestingAccess(smood);
		result.setMetaModelProvider(BasicAccesAdapterTestModelProvider.INSTANCE);
		
		return result;
	}
	
	/** Regarding {@link KnownIssue}, see {@link #EXPTECTED_TO_FAIL_testGetRemovedElementsForPropertyOfTypeSetForRemoveManipulation()} */
	@Test
	@Category(KnownIssue.class)
	public void EXPTECTED_TO_FAIL_testGetRemovedElementsForPropertyOfTypeSetForRemoveManipulation2() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getCharsUsed().remove("a");
		book.getCharsUsed().remove("b");
		book.getCharsUsed().remove("c");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		Set<Object> removedItems = report.getRemovedElementsForPropertyOfTypeSet(entity, property);
		
		assertThat(removedItems).isEmpty();
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeSetAddRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getCharsUsed().add("a");
		book.getCharsUsed().add("b");
		book.getCharsUsed().add("c");
		book.getCharsUsed().add("y");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Property property = touchedPropertiesOfEntities.get(entity).iterator().next();
		Set<Object> removedItems = report.getRemovedElementsForPropertyOfTypeSet(entity, property);
		Set<Object> addedItems = report.getAddedElementsForPropertyOfTypeSet(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		assertThat(removedItems).isEmpty();
		assertThat(addedItems).containsOnly("a", "b", "c");
	}

	/** Regarding {@link KnownIssue}, see {@link #EXPTECTED_TO_FAIL_testGetRemovedElementsForPropertyOfTypeSetForRemoveManipulation()} */
	@Test
	@Category(KnownIssue.class)
	public void EXPTECTED_TO_FAIL_testGetRemovedElementsForPropertyOfTypeSetComplexManipulations() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getCharsUsed().remove("x");
		book.getCharsUsed().remove("y");
		book.getCharsUsed().add("y");
		book.getCharsUsed().add("y");

		book.getCharsUsed().remove("a");
		book.getCharsUsed().add("a");
		book.getCharsUsed().add("b");
		book.getCharsUsed().remove("b");
		book.getCharsUsed().add("c");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Set<Property> touchedProperties = touchedPropertiesOfEntities.get(entity);
		Property property = touchedProperties.iterator().next();
		Set<Object> removedItems = report.getRemovedElementsForPropertyOfTypeSet(entity, property);
		Set<Object> addedItems = report.getAddedElementsForPropertyOfTypeSet(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		assertThat(removedItems).containsOnly("x");
		assertThat(addedItems).containsOnly("a", "c");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeListForClearCollectionManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getWriter().clear();
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<ListEntry> removedItems = report.getRemovedElementsForPropertyOfTypeList(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (ListEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}
		assertThat(result).contains("Wien", "Braintribe", "tribefire");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeListForRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getWriter().remove("Test");
		book.getWriter().remove("Wien");
		book.getWriter().remove("tribefire");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<ListEntry> removedItems = report.getRemovedElementsForPropertyOfTypeList(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (ListEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}
		assertThat(result).containsOnly("Wien", "tribefire");
	}
	
	@Test
	public void testGetRemovedElementsForPropertyOfTypeListForRemoveManipulation2() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getWriter().remove("Braintribe");
		book.getWriter().remove("Wien");
		book.getWriter().remove("tribefire");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<ListEntry> removedItems = report.getRemovedElementsForPropertyOfTypeList(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (ListEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}
		assertThat(result).containsOnly("Wien", "tribefire", "Braintribe");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeListAddRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getWriter().add("Braintribe");
		book.getWriter().add("Wien");
		book.getWriter().add("tribefire");
		book.getWriter().add("Test");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Property property = touchedPropertiesOfEntities.get(entity).iterator().next();
		List<ListEntry> removedItems = report.getRemovedElementsForPropertyOfTypeList(entity, property);
		List<ListEntry> addedItems = report.getAddedElementsForPropertyOfTypeList(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		assertThat(removedItems).isEmpty();

		List<String> result = new ArrayList<String>();
		for (ListEntry entry : addedItems) {
			result.add((String) entry.getValue());
		}
		assertThat(result).containsOnly("Braintribe", "Wien", "tribefire", "Test");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeListComplexManipulations() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getWriter().remove("Braintribe");
		book.getWriter().remove("Wien");
		book.getWriter().add("Wien");
		book.getWriter().add("Wien");

		book.getWriter().remove("tribefire");
		book.getWriter().add("tribefire");
		book.getWriter().add("Test");
		book.getWriter().remove("Test");
		book.getWriter().add("Test2");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Set<Property> touchedProperties = touchedPropertiesOfEntities.get(entity);
		Property property = touchedProperties.iterator().next();
		List<ListEntry> removedItems = report.getRemovedElementsForPropertyOfTypeList(entity, property);
		List<ListEntry> addedItems = report.getAddedElementsForPropertyOfTypeList(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		
		List<String> result = new ArrayList<String>();
		for (ListEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}
		List<String> result2 = new ArrayList<String>();
		for (ListEntry entry : addedItems) {
			result2.add((String) entry.getValue());
		}

		assertThat(result).containsOnly("Braintribe", "Wien", "tribefire", "Test");
		assertThat(result2).containsOnly("Wien", "tribefire", "Test", "Test2");
		assertThat(result2).containsSequence("Wien", "Wien");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeMapForClearCollectionManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getProperties().clear();
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<MapEntry> removedItems = report.getRemovedElementsForPropertyOfTypeMap(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (MapEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}

		assertThat(result).containsOnly("xyz", "braintribe");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeMapForRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getProperties().remove("Name");
		book.getProperties().remove("Publisher");
		book.getProperties().remove("Test");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<MapEntry> removedItems = report.getRemovedElementsForPropertyOfTypeMap(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (MapEntry entry : removedItems) {
			result.add((String) entry.getKey());
		}

		assertThat(result).containsOnly("Name", "Publisher");
	}
	
	@Test
	public void testGetRemovedElementsForPropertyOfTypeMapForRemoveManipulation2() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getProperties().remove("Name");
		book.getProperties().remove("Publisher");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		GenericEntity entity = report.getTouchedPropertiesOfEntities().keySet().iterator().next();
		Property property = report.getTouchedPropertiesOfEntities().get(entity).iterator().next();
		List<MapEntry> removedItems = report.getRemovedElementsForPropertyOfTypeMap(entity, property);
		
		List<String> result = new ArrayList<String>();
		for (MapEntry entry : removedItems) {
			result.add((String) entry.getKey());
		}

		assertThat(result).containsOnly("Name", "Publisher");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeMapAddRemoveManipulation() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getProperties().put("Name", "Test");
		book.getProperties().put("Publisher", "Meyer");
		book.getProperties().put("Date", "1.1.2015");
		book.getProperties().put("Origin", "Austria");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Property property = touchedPropertiesOfEntities.get(entity).iterator().next();
		List<MapEntry> removedItems = report.getRemovedElementsForPropertyOfTypeMap(entity, property);
		List<MapEntry> addedItems = report.getAddedElementsForPropertyOfTypeMap(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		assertThat(removedItems).isEmpty();

		List<String> result = new ArrayList<String>();
		for (MapEntry entry : addedItems) {
			result.add((String) entry.getValue());
		}

		assertThat(result).containsOnly("Test", "Meyer", "1.1.2015", "Austria");
	}

	@Test
	public void testGetRemovedElementsForPropertyOfTypeMapComplexManipulations() throws GmSessionException {
		Smood smood = configureSmood();
		ReportTestingAccess adapter = newReportTestingAccess(smood);
		
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(adapter);
		
		Book book = s.query().entity(Book.T, bb1.getId()).find();
		book.getProperties().remove("Name");
		book.getProperties().remove("Publisher");
		book.getProperties().put("Publisher", "Meyer");
		book.getProperties().put("Publisher", "Müller");

		book.getProperties().remove("Date");
		book.getProperties().put("Date", "1.1.2015");
		book.getProperties().put("Origin", "Austria");
		book.getProperties().remove("Origin");
		book.getProperties().put("Testresult", "done");
		s.commit();
		
		AdapterManipulationReport report = adapter.getReport();
		Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = report.getTouchedPropertiesOfEntities();
		GenericEntity entity = touchedPropertiesOfEntities.keySet().iterator().next();
		Set<Property> touchedProperties = touchedPropertiesOfEntities.get(entity);
		Property property = touchedProperties.iterator().next();
		List<MapEntry> removedItems = report.getRemovedElementsForPropertyOfTypeMap(entity, property);
		List<MapEntry> addedItems = report.getAddedElementsForPropertyOfTypeMap(entity, property);
		
		Assert.assertEquals(1, touchedPropertiesOfEntities.size());
		
		List<String> result = new ArrayList<String>();
		for (MapEntry entry : removedItems) {
			result.add((String) entry.getValue());
		}
		List<String> result2 = new ArrayList<String>();
		for (MapEntry entry : addedItems) {
			result2.add((String) entry.getValue());
		}
		assertThat(result).containsOnly("xyz", "braintribe", "Austria");
		assertThat(result2).containsOnly("Meyer", "Müller", "1.1.2015", "Austria", "done");
	}

}
