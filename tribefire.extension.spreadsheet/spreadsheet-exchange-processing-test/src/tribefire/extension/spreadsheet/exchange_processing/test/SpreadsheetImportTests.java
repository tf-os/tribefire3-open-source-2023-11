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
package tribefire.extension.spreadsheet.exchange_processing.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.IOTools;

import tribefire.extension.spreadsheet.model.exchange.api.data.ImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.data.TransientImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportCsvSheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportExcelSheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheetRequest;
import tribefire.extension.spreadsheet.model.test.DateTestRecord;
import tribefire.extension.spreadsheet.model.test.Person;
import tribefire.extension.spreadsheet.model.test.PersonContext;
import tribefire.extension.spreadsheet.model.test.PersonImport;
import tribefire.extension.spreadsheet.model.test.Record;
import tribefire.extension.spreadsheet.model.test.TestRecord;

public class SpreadsheetImportTests extends SpreadsheetExchangeProcessingTestBase implements TestConstants {

	@Test
	public void testStream() throws IOException {
		try (InputStream in = new FileInputStream("res/test.csv")) {

			int i = 0;

			while ((i = in.read()) != -1) {
				System.out.println((char) i);
			}
		}
	}
	@Test
	public void testSimpleExcelImport() throws Exception {
		ImportResult<Record> importResult = doExcelImport("res/simple-test.xlsx", ACCESS_IMPORT, Record.T, true);
		importResult.outputResults();
	}

	@Test
	public void testSimpleExcelOffsetImport() throws Exception {
		ImportResult<Record> importResult = doExcelImport("res/simple-test-offset.xlsx", ACCESS_IMPORT, Record.T, false);
		importResult.outputResults();
	}

	@Test
	public void testRegexExcelImport() throws Exception {
		ImportResult<Record> importResult = doExcelImport("res/regex-test.xlsx", ACCESS_IMPORT_REGEX, Record.T, true);
		importResult.outputResults();
	}

	@Test
	public void testSimpleCsvImport() throws Exception {
		ImportResult<Record> importResult = doCsvImport("res/simple-test.csv", ACCESS_IMPORT_SIMPLE_CSV, Record.T, ';', true);
		importResult.outputResults();
	}

	@Test
	public void testSimpleSheetImport() throws Exception {
		ImportResult<Record> importResult = doSheetImport("res/simple-test.csv", ACCESS_IMPORT_SIMPLE_CSV, Record.T, true);
		importResult.outputResults();
	}

	@Test
	public void testSimpleTransientCsvImport() throws Exception {
		ImportResult<Record> importResult = doSheetImport("res/simple-test.csv", ACCESS_IMPORT_SIMPLE_CSV, Record.T, true, r -> {
			r.setTransient(true);
			r.setStartRow(1);
			r.setMaxRows(1);
		});

		List<Record> results = importResult.results;

		Assertions.assertThat(results.size()).isEqualTo(1);

		Record record = results.get(0);

		LocalDate localDate = LocalDate.of(2020, 12, 24);
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		Assertions.assertThat(record.getDoubleValue()).isEqualTo(0.2D);
		Assertions.assertThat(record.getIntegerValue()).isEqualTo(2);
		Assertions.assertThat(record.getStringValue()).isEqualTo("Jesus");
		Assertions.assertThat(record.getDateValue()).isEqualTo(date);
		Assertions.assertThat(record.getBooleanValue()).isEqualTo(false);

	}

	@Test
	public void testSimpleTransientXlsxImport() throws Exception {
		ImportResult<Record> importResult = doSheetImport("res/simple-test.xlsx", ACCESS_IMPORT_SIMPLE_XLSX, Record.T, true, r -> {
			r.setTransient(true);
			r.setStartRow(1);
			r.setMaxRows(1);
		});

		List<Record> results = importResult.results;

		Assertions.assertThat(results.size()).isEqualTo(1);

		Record record = results.get(0);

		LocalDate localDate = LocalDate.of(2020, 12, 24);
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		Assertions.assertThat(record.getDoubleValue()).isEqualTo(0.2D);
		Assertions.assertThat(record.getIntegerValue()).isEqualTo(2);
		Assertions.assertThat(record.getStringValue()).isEqualTo("Jesus");
		Assertions.assertThat(record.getDateValue()).isEqualTo(date);
		Assertions.assertThat(record.getBooleanValue()).isEqualTo(false);
	}

	@Test
	public void testSheetNameXlsxImport() throws Exception {
		ImportResult<Record> importResult = doExcelSheetImport("res/multi-sheet.xlsx", ACCESS_IMPORT_SIMPLE_XLSX, Record.T, true, r -> {
			r.setTransient(true);
			r.setSheetName("Sheet2");
		});

		List<Record> results = importResult.results;

		Assertions.assertThat(results.size()).isEqualTo(5);

		Record record = results.get(0);

		LocalDate localDate = LocalDate.of(1951, 3, 17);
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		Assertions.assertThat(record.getDoubleValue()).isEqualTo(3.14D);
		Assertions.assertThat(record.getIntegerValue()).isEqualTo(5);
		Assertions.assertThat(record.getStringValue()).isEqualTo("one");
		Assertions.assertThat(record.getDateValue()).isEqualTo(date);
		Assertions.assertThat(record.getBooleanValue()).isEqualTo(false);
	}

	@Test
	public void testProblematicExcelImport() throws Exception {
		ImportResult<Record> importResult = doExcelImport("res/problematic-test.xlsx", ACCESS_IMPORT, Record.T, true);
		importResult.outputResults();
	}

	@Test
	public void testDateExcelImport() throws Exception {
		ImportResult<DateTestRecord> importResult = doExcelImport("res/date-test.xlsx", ACCESS_IMPORT3, DateTestRecord.T, true);
		importResult.outputResults();
	}

	@Test
	public void testDateExcelImportCustomDateFormat() throws Exception {
		ImportResult<DateTestRecord> importResult = doExcelImport("res/date-test2.xlsx", ACCESS_IMPORT2, DateTestRecord.T, true);
		importResult.outputResults();
	}

	@Test
	public void testTransientEntityShareImport() throws Exception {

		PersistenceGmSession session = testContract.sessionFactory().newSession(ACCESS_IMPORT_SHARED);
		PersonImport personImport1 = session.create(PersonImport.T);
		personImport1.setName("test-import-1");
		personImport1.setImportDate(new Date());

		session.commit();

		doCsvImport("res/test-share-first.csv", ACCESS_IMPORT_SHARED, Person.T, ';', true, r -> {
			r.getEnrichments().put("importPackage", personImport1);
			r.setTransient(true);
		});
	}

	@Test
	public void testEntityShareImport() throws Exception {

		PersistenceGmSession session = testContract.sessionFactory().newSession(ACCESS_IMPORT_SHARED);
		PersonImport personImport1 = session.create(PersonImport.T);
		personImport1.setName("test-import-1");
		personImport1.setImportDate(new Date());

		PersonImport personImport2 = session.create(PersonImport.T);
		personImport2.setName("test-import-2");
		personImport2.setImportDate(new Date());

		session.commit();

		doCsvImport("res/test-share-first.csv", ACCESS_IMPORT_SHARED, Person.T, ';', true,
				r -> r.getEnrichments().put("importPackage", personImport1));

		doCsvImport("res/test-share-second.csv", ACCESS_IMPORT_SHARED, Person.T, ';', true,
				r -> r.getEnrichments().put("importPackage", personImport2));

		List<PersonContext> contexts = session.query().entities(EntityQuery.create(PersonContext.T)).list();
		List<Person> persons = session.query().entities(EntityQuery.create(Person.T)).list();

		// expected person count 1000
		// expected context count 1500

		Assertions.assertThat(persons.size()).isEqualTo(1000);
		Assertions.assertThat(contexts.size()).isEqualTo(1500);

		int import1Matches = 0;
		int import2Matches = 0;
		int sharedEntities = 0;

		Set<Person> personsVisited = new HashSet<>();

		for (PersonContext context : contexts) {
			if (context.getImportPackage() == personImport1)
				import1Matches++;

			if (context.getImportPackage() == personImport2)
				import2Matches++;

			if (!personsVisited.add(context.getPerson())) {
				sharedEntities++;
			}
		}

		Assertions.assertThat(import1Matches).isEqualTo(750);
		Assertions.assertThat(import2Matches).isEqualTo(750);
		Assertions.assertThat(sharedEntities).isEqualTo(500);
	}

	@Test
	public void testEntityUpdateImport() throws Exception {
		doCsvImport("res/test-update.csv", ACCESS_IMPORT_UPDATE, Person.T, ';', true);

		PersistenceGmSession session = testContract.sessionFactory().newSession(ACCESS_IMPORT_UPDATE);

		long countAfterImport = session.query().select(TestSelectQueries.personCount()).unique();

		doCsvImport("res/test-update2.csv", ACCESS_IMPORT_UPDATE, Person.T, ';', true);

		long countAfterUpdate = session.query().select(TestSelectQueries.personCount()).unique();

		long newPersonCount = countAfterUpdate - countAfterImport;

		Assertions.assertThat(newPersonCount).isEqualTo(3);

		Person p1 = session.query().entities(TestEntityQueries.personBySocialContractNumber("42227")).unique();
		Person p2 = session.query().entities(TestEntityQueries.personBySocialContractNumber("333333")).unique();

		Assertions.assertThat(p1.getHobby()).isEqualTo("Reiten");
		Assertions.assertThat(p2.getHobby()).isEqualTo("Wandern");

	}

	@Test
	public void testTrimming() throws Exception {

		PersistenceGmSession session = testContract.sessionFactory().newSession(ACCESS_IMPORT_TRIMMING);
		doCsvImport("res/test-trim.csv", ACCESS_IMPORT_TRIMMING, Person.T, ';', false);

		Person personWithTrimmedHobby1 = queryPersonBySocialContractNumber(session, "65247");
		Person personWithTrimmedHobby2 = queryPersonBySocialContractNumber(session, "76310");
		Person personWithUntrimmedLastName = queryPersonBySocialContractNumber(session, "25094");

		Assertions.assertThat(personWithTrimmedHobby1.getHobby()).isEqualTo("Philosophie");
		Assertions.assertThat(personWithTrimmedHobby2.getHobby()).isEqualTo("Wandern");
		Assertions.assertThat(personWithUntrimmedLastName.getLastName()).isEqualTo(" Weiss");
	}

	private static Person queryPersonBySocialContractNumber(PersistenceGmSession session, String socialContractNumber) {
		return session.query().entities(TestEntityQueries.personBySocialContractNumber(socialContractNumber)).unique();
	}

	private static class TestEntityQueries extends EntityQueries {
		static EntityQuery personBySocialContractNumber(String socialContractNumber) {
			return EntityQuery.create(Person.T).where(eq(property(Person.socialContractNumber), socialContractNumber));
		}
	}

	@Test
	public void testNullForbiddenEntityUpdateImport() throws Exception {
		doCsvImport("res/test-update-null-forbidden.csv", ACCESS_IMPORT_UPDATE_NULL_FORBIDDEN, Person.T, ';', true);

		PersistenceGmSession session = testContract.sessionFactory().newSession(ACCESS_IMPORT_UPDATE_NULL_FORBIDDEN);

		long countAfterImport = session.query().select(TestSelectQueries.personCount()).unique();

		doCsvImport("res/test-update-null-forbidden.csv", ACCESS_IMPORT_UPDATE_NULL_FORBIDDEN, Person.T, ';', true);

		long countAfterUpdate = session.query().select(TestSelectQueries.personCount()).unique();

		long newPersonCount = countAfterUpdate - countAfterImport;

		Assertions.assertThat(newPersonCount).isEqualTo(2);

	}

	private static class TestSelectQueries extends SelectQueries {
		static SelectQuery personCount() {
			From p = source(Person.T);
			return from(p).select(count(p));
		}
	}

	private static class ImportResult<T extends GenericEntity> {
		EntityType<T> targetType;
		List<T> results;
		ImportReport report;
		String errorReport = "";

		void outputResults() {

			if (!errorReport.isEmpty())
				System.out.println(errorReport);

			System.out.println("Imported " + report.getImportedEntityCount() + " entities.");

			for (T record : results) {
				if (record instanceof TestRecord) {
					System.out.print(((TestRecord) record).getRowNum() + ": ");
				}

				boolean first = true;
				for (Property property : targetType.getDeclaredProperties()) {
					if (first)
						first = false;
					else
						System.out.print(", ");

					Object value = property.get(record);
					System.out.print(value);

				}
				System.out.println();
			}
		}

	}

	private <T extends TestRecord> ImportResult<T> doSheetImport(String fileName, String accessId, EntityType<T> targetType, boolean lenient)
			throws Exception {
		return doSheetImport(fileName, accessId, targetType, lenient, r -> {
			/* noop */ });
	}

	private <T extends TestRecord> ImportResult<T> doExcelImport(String fileName, String accessId, EntityType<T> targetType, boolean lenient)
			throws Exception {
		return doExcelImport(fileName, accessId, targetType, lenient, r -> {
			/* noop */ });
	}

	private <T extends GenericEntity> ImportResult<T> doCsvImport(String fileName, String accessId, EntityType<T> targetType, char delimiter,
			boolean lenient) throws Exception {
		return doCsvImport(fileName, accessId, targetType, delimiter, lenient, r -> {
			/* noop */ });
	}

	private <T extends GenericEntity> ImportResult<T> doSheetImport(String fileName, String accessId, EntityType<T> targetType, boolean lenient,
			Consumer<ImportSpreadsheetRequest> enricher) throws Exception {
		ImportSpreadsheet importRequest = ImportSpreadsheet.T.create();
		importRequest.setLenient(lenient);
		enricher.accept(importRequest);
		return doImport(fileName, accessId, targetType, importRequest);
	}

	private <T extends GenericEntity> ImportResult<T> doExcelSheetImport(String fileName, String accessId, EntityType<T> targetType, boolean lenient,
			Consumer<ImportExcelSheet> enricher) throws Exception {
		ImportExcelSheet importRequest = ImportExcelSheet.T.create();
		importRequest.setLenient(lenient);
		enricher.accept(importRequest);
		return doImport(fileName, accessId, targetType, importRequest);
	}

	private <T extends GenericEntity> ImportResult<T> doExcelImport(String fileName, String accessId, EntityType<T> targetType, boolean lenient,
			Consumer<ImportSpreadsheetRequest> enricher) throws Exception {
		ImportExcelSheet importRequest = ImportExcelSheet.T.create();
		importRequest.setLenient(lenient);
		enricher.accept(importRequest);
		return doImport(fileName, accessId, targetType, importRequest);
	}

	private <T extends GenericEntity> ImportResult<T> doCsvImport(String fileName, String accessId, EntityType<T> targetType, char delimiter,
			boolean lenient, Consumer<ImportSpreadsheetRequest> enricher) throws Exception {
		ImportCsvSheet importRequest = ImportCsvSheet.T.create();
		importRequest.setLenient(lenient);
		importRequest.setDelimiter(Character.toString(delimiter));
		enricher.accept(importRequest);
		return doImport(fileName, accessId, targetType, importRequest);
	}

	private <T extends GenericEntity> ImportResult<T> doImport(String fileName, String accessId, EntityType<T> targetType,
			ImportSpreadsheetRequest importRequest) throws Exception {
		File file = new File(fileName);
		Resource sheetResource = Resource.createTransient(() -> new FileInputStream(file));
		sheetResource.setName(file.getName());

		importRequest.setTargetType(targetType.getTypeSignature());
		importRequest.setSheet(sheetResource);

		PersistenceGmSession session = testContract.sessionFactory().newSession(accessId);

		ImportResult<T> result = new ImportResult<>();
		result.targetType = targetType;

		EvalContext<? extends ImportReport> evalContext = importRequest.eval(session);
		evalContext.setAttribute(RequestorUserNameAspect.class, "test-user");
		ImportReport importReport = evalContext.get();

		result.report = importReport;

		Resource errorReport = importReport.getErrorReport();

		if (errorReport != null) {
			try (InputStream in = errorReport.openStream()) {
				result.errorReport = IOTools.slurp(in, "UTF-8");
			}
		}

		if (importRequest.getTransient()) {
			result.results = (List<T>) (List<?>) ((TransientImportReport) importReport).getEntities();
		} else {

			EntityQuery query = new EntityQueries() {
				{

					supply = from(targetType).orderBy(property(TestRecord.rowNum));

				}
			}.get();

			result.results = session.query().entities(query).list();
		}

		return result;
	}

}
