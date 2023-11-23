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
package com.braintribe.tribefire.cartridge.library.integration.test;

import static com.braintribe.wire.api.util.Lists.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.library.service.license.CheckLicenseAvailability;
import com.braintribe.model.library.service.license.CreateLicenseImportSpreadsheet;
import com.braintribe.model.library.service.license.ExportLicenseSpreadsheet;
import com.braintribe.model.library.service.license.ImportSpreadsheet;
import com.braintribe.model.library.service.license.LicenseAvailability;
import com.braintribe.model.library.service.license.LicenseExportSpreadsheet;
import com.braintribe.model.library.service.license.LicenseImportSpreadsheet;
import com.braintribe.model.library.service.license.SpreadsheetImportReport;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.MemoryInputStreamProviders;

public class LicenseTest extends LibraryIntegrationTestBase {

	@Test
	public void testCheckLicenses() throws Exception {

		CheckLicenseAvailability checkAvail = CheckLicenseAvailability.T.create();

		List<String> list = new ArrayList<>();
		list.add("org.jboss:jandex#1.1.0.Final");
		list.add("com.googlecode.mp4parser:isoparser#1.0.2");
		list.add("edu.ucar:thredds-parent#4.5.5");
		list.add("antlr:antlr#2.7.7");
		list.add("aopalliance:aopalliance#1.0");
		list.add("aopalliance:aopalliance#1.0");
		list.add("aopalliance:aopalliance#1.0");
		list.add("cglib:cglib-nodep#2.2");
		list.add("com.adobe.xmp:xmpcore#5.1.2");
		list.add("com.adobe.xmp:xmpcore#5.1.2");
		list.add("com.aspose:aspose-cells#8.5.2");
		list.add("com.aspose:aspose-imaging#2.6.0");
		list.add("com.aspose:aspose-pdf#10.6.1");
		list.add("com.aspose:aspose-slides#15.6.0");
		list.add("com.aspose:aspose-words#15.7.0");
		list.add("com.auxilii:glf-client#1.1");
		list.add("com.drewnoakes:metadata-extractor#2.9.1");
		list.add("com.drewnoakes:metadata-extractor#2.9.1");
		list.add("com.fasterxml.jackson.core:jackson-annotations#2.3.0");
		list.add("com.fasterxml.jackson.core:jackson-annotations#2.8.9");
		list.add("com.fasterxml.jackson.core:jackson-annotations#2.8.9");
		list.add("com.fasterxml.jackson.core:jackson-core#2.3.1");
		list.add("com.fasterxml.jackson.core:jackson-core#2.8.9");
		list.add("com.fasterxml.jackson.core:jackson-core#2.8.9");
		list.add("com.fasterxml.jackson.core:jackson-databind#2.3.1");
		list.add("com.fasterxml.jackson.core:jackson-databind#2.8.9");
		list.add("com.fasterxml.jackson.core:jackson-databind#2.8.9");
		list.add("com.github.dblock:oshi-core#2.6-m-java7");
		list.add("com.github.junrar:junrar#0.7");
		list.add("com.github.junrar:junrar#0.7");
		list.add("com.github.virtuald:curvesapi#1.04");
		list.add("com.github.virtuald:curvesapi#1.04");
		list.add("com.google.code.gson:gson#2.2.4");
		list.add("com.google.code.gson:gson#2.2.4");
		list.add("com.google.guava:guava#16.0");
		list.add("com.google.guava:guava#16.0");
		list.add("com.google.guava:guava#19.0");
		list.add("com.googlecode.json-simple:json-simple#1.1.1");
		list.add("com.googlecode.json-simple:json-simple#1.1.1");
		list.add("com.googlecode.json-simple:json-simple#1.1.1");
		list.add("com.googlecode.juniversalchardet:juniversalchardet#1.0.3");
		list.add("com.googlecode.juniversalchardet:juniversalchardet#1.0.3");
		list.add("com.googlecode.mp4parser:isoparser#1.1.18");
		list.add("com.googlecode.mp4parser:isoparser#1.1.18");
		list.add("com.healthmarketscience.jackcess:jackcess-encrypt#2.1.1");
		list.add("com.healthmarketscience.jackcess:jackcess-encrypt#2.1.1");
		list.add("com.healthmarketscience.jackcess:jackcess#2.1.4");
		list.add("com.healthmarketscience.jackcess:jackcess#2.1.4");
		list.add("com.lowagie:itext#4.2.1");
		list.add("com.mchange:c3p0#0.9.5");
		list.add("com.mchange:mchange-commons-java#0.2.9");
		list.add("com.pff:java-libpst#0.8.1");
		list.add("com.pff:java-libpst#0.8.1");
		list.add("com.rometools:rome-utils#1.5.1");
		list.add("com.rometools:rome-utils#1.5.1");
		list.add("com.rometools:rome#1.5.1");
		list.add("com.rometools:rome#1.5.1");
		list.add("com.sun.media:jai-codec#1.1.3");
		list.add("com.zaxxer:HikariCP-java7#2.4.12");
		list.add("commons-beanutils:commons-beanutils#1.9.3");
		list.add("commons-beanutils:commons-beanutils#1.9.3");
		list.add("commons-codec:commons-codec#1.10");
		list.add("commons-codec:commons-codec#1.10");
		list.add("commons-codec:commons-codec#1.6");
		list.add("commons-collections:commons-collections#3.2.1");
		list.add("commons-collections:commons-collections#3.2.2");
		list.add("commons-collections:commons-collections#3.2.2");
		list.add("commons-fileupload:commons-fileupload#1.3.2");
		list.add("commons-fileupload:commons-fileupload#1.3.3");
		list.add("commons-io:commons-io#2.2");
		list.add("commons-io:commons-io#2.5");
		list.add("commons-io:commons-io#2.5");
		list.add("commons-jxpath:commons-jxpath#1.3");
		list.add("commons-lang:commons-lang#2.6");
		list.add("commons-lang:commons-lang#2.6");
		list.add("commons-lang:commons-lang#2.6");
		list.add("commons-logging:commons-logging-api#1.1");
		list.add("commons-logging:commons-logging#1.2");
		list.add("commons-logging:commons-logging#1.2");
		list.add("de.l3s.boilerpipe:boilerpipe#1.1.0");
		list.add("de.l3s.boilerpipe:boilerpipe#1.1.0");
		list.add("dom4j:dom4j#1.6.1");
		list.add("dom4j:dom4j#1.6.1");
		list.add("flyingsaucer:flyingsaucer#8");
		list.add("javassist:javassist#3.12.1.GA");
		list.add("javax.activation:activation#1.1.1");
		list.add("javax.annotation:javax.annotation-api#1.2");
		list.add("javax.annotation:javax.annotation-api#1.2");
		list.add("javax.jms:jms-api#1.1-rev-1");
		list.add("javax.jms:jms-api#1.1-rev-1");
		list.add("javax.media:jai_core#1.1.3");
		list.add("javax.transaction:javax.transaction-api#1.2");
		list.add("javax.ws.rs:javax.ws.rs-api#2.0.1");
		list.add("javax.ws.rs:javax.ws.rs-api#2.0.1");
		list.add("jfree:jcommon#1.0.15");
		list.add("jfree:jfreechart#1.0.12");
		list.add("joda-time:joda-time#2.3");
		list.add("joda-time:joda-time#2.3");
		list.add("joda-time:joda-time#2.3");
		list.add("net.java.dev.jna:jna-platform#4.2.2");
		list.add("net.java.dev.jna:jna#4.2.2");
		list.add("net.sf.ehcache:ehcache-core#2.5.0");
		list.add("net.sf.jtidy:jtidy#r938");
		list.add("net.sf.jwordnet:jwnl#1.3.3");
		list.add("net.sf.jwordnet:jwnl#1.3.3");
		list.add("org.antlr:antlr-runtime#3.1.1");
		list.add("org.antlr:stringtemplate#3.2");
		list.add("org.apache-extras.beanshell:bsh#2.0b6");
		list.add("org.apache-extras.beanshell:bsh#2.0b6");
		list.add("org.apache.activemq:activemq-client#5.14.0");
		list.add("org.apache.activemq:activemq-client#5.14.0");
		list.add("org.apache.ant:ant-launcher#1.8.2");
		list.add("org.apache.ant:ant#1.8.2");
		list.add("org.apache.commons:commons-collections4#4.1");
		list.add("org.apache.commons:commons-collections4#4.1");
		list.add("org.apache.commons:commons-compress#1.12");
		list.add("org.apache.commons:commons-compress#1.12");
		list.add("org.apache.commons:commons-csv#1.0");
		list.add("org.apache.commons:commons-csv#1.0");
		list.add("org.apache.commons:commons-exec#1.3");
		list.add("org.apache.commons:commons-exec#1.3");
		list.add("org.apache.commons:commons-vfs2#2.0");
		list.add("org.apache.commons:commons-vfs2#2.0");
		list.add("org.apache.cxf:cxf-core#3.1.12");
		list.add("org.apache.cxf:cxf-core#3.1.12");
		list.add("org.apache.cxf:cxf-rt-frontend-jaxrs#3.1.12");
		list.add("org.apache.cxf:cxf-rt-frontend-jaxrs#3.1.12");
		list.add("org.apache.cxf:cxf-rt-rs-client#3.1.12");
		list.add("org.apache.cxf:cxf-rt-rs-client#3.1.12");
		list.add("org.apache.cxf:cxf-rt-transports-http#3.1.12");
		list.add("org.apache.cxf:cxf-rt-transports-http#3.1.12");
		list.add("org.apache.geronimo.specs:geronimo-j2ee-management_1.1_spec#1.0.1");
		list.add("org.apache.geronimo.specs:geronimo-j2ee-management_1.1_spec#1.0.1");
		list.add("org.apache.geronimo.specs:geronimo-jms_1.1_spec#1.1.1");
		list.add("org.apache.geronimo.specs:geronimo-jms_1.1_spec#1.1.1");
		list.add("org.apache.httpcomponents:httpclient#4.3.6");
		list.add("org.apache.httpcomponents:httpclient#4.3.6");
		list.add("org.apache.httpcomponents:httpclient#4.3.6");
		list.add("org.apache.httpcomponents:httpcore#4.3.3");
		list.add("org.apache.httpcomponents:httpcore#4.3.3");
		list.add("org.apache.httpcomponents:httpcore#4.3.3");
		list.add("org.apache.httpcomponents:httpmime#4.3.6");
		list.add("org.apache.httpcomponents:httpmime#4.3.6");
		list.add("org.apache.httpcomponents:httpmime#4.3.6");
		list.add("org.apache.james:apache-mime4j-core#0.8.1");
		list.add("org.apache.james:apache-mime4j-core#0.8.1");
		list.add("org.apache.james:apache-mime4j-dom#0.8.1");
		list.add("org.apache.james:apache-mime4j-dom#0.8.1");
		list.add("org.apache.logging.log4j:log4j-api#2.2");
		list.add("org.apache.logging.log4j:log4j-api#2.8.2");
		list.add("org.apache.logging.log4j:log4j-api#2.8.2");
		list.add("org.apache.logging.log4j:log4j-to-slf4j#2.2");
		list.add("org.apache.logging.log4j:log4j-to-slf4j#2.8.2");
		list.add("org.apache.logging.log4j:log4j-to-slf4j#2.8.2");
		list.add("org.apache.maven.scm:maven-scm-api#1.4");
		list.add("org.apache.maven.scm:maven-scm-api#1.4");
		list.add("org.apache.maven.scm:maven-scm-provider-svn-commons#1.4");
		list.add("org.apache.maven.scm:maven-scm-provider-svn-commons#1.4");
		list.add("org.apache.maven.scm:maven-scm-provider-svnexe#1.4");
		list.add("org.apache.maven.scm:maven-scm-provider-svnexe#1.4");
		list.add("org.apache.opennlp:opennlp-maxent#3.0.3");
		list.add("org.apache.opennlp:opennlp-maxent#3.0.3");
		list.add("org.apache.opennlp:opennlp-tools#1.5.3");
		list.add("org.apache.opennlp:opennlp-tools#1.5.3");
		list.add("org.apache.pdfbox:fontbox#1.8.13");
		list.add("org.apache.pdfbox:fontbox#2.0.6");
		list.add("org.apache.pdfbox:fontbox#2.0.6");
		list.add("org.apache.pdfbox:jempbox#1.8.12");
		list.add("org.apache.pdfbox:jempbox#1.8.13");
		list.add("org.apache.pdfbox:pdfbox-debugger#2.0.3");
		list.add("org.apache.pdfbox:pdfbox-debugger#2.0.3");
		list.add("org.apache.pdfbox:pdfbox-debugger#2.0.6");
		list.add("org.apache.pdfbox:pdfbox-tools#2.0.3");
		list.add("org.apache.pdfbox:pdfbox-tools#2.0.3");
		list.add("org.apache.pdfbox:pdfbox-tools#2.0.6");
		list.add("org.apache.pdfbox:pdfbox#1.8.13");
		list.add("org.apache.pdfbox:pdfbox#2.0.6");
		list.add("org.apache.pdfbox:pdfbox#2.0.6");
		list.add("org.apache.poi:poi-ooxml-schemas#3.15");
		list.add("org.apache.poi:poi-ooxml-schemas#3.15");
		list.add("org.apache.poi:poi-ooxml#3.15");
		list.add("org.apache.poi:poi-ooxml#3.15");
		list.add("org.apache.poi:poi-scratchpad#3.15");
		list.add("org.apache.poi:poi-scratchpad#3.15");
		list.add("org.apache.poi:poi#3.15");
		list.add("org.apache.poi:poi#3.15");
		list.add("org.apache.sanselan:sanselan#0.97-incubator");
		list.add("org.apache.tika:tika-core#1.14");
		list.add("org.apache.tika:tika-core#1.14");
		list.add("org.apache.tika:tika-parsers#1.14");
		list.add("org.apache.tika:tika-parsers#1.14");
		list.add("org.apache.velocity:velocity#1.7");
		list.add("org.apache.velocity:velocity#1.7");
		list.add("org.apache.velocity:velocity#1.7");
		list.add("org.apache.ws.xmlschema:xmlschema-core#2.2.2");
		list.add("org.apache.ws.xmlschema:xmlschema-core#2.2.2");
		list.add("org.apache.xmlbeans:xmlbeans#2.6.0");
		list.add("org.apache.xmlbeans:xmlbeans#2.6.0");
		list.add("org.bouncycastle:bcmail-jdk14#1.38");
		list.add("org.bouncycastle:bcmail-jdk15on#1.54");
		list.add("org.bouncycastle:bcmail-jdk15on#1.54");
		list.add("org.bouncycastle:bcpkix-jdk15on#1.54");
		list.add("org.bouncycastle:bcpkix-jdk15on#1.54");
		list.add("org.bouncycastle:bcprov-jdk14#1.38");
		list.add("org.bouncycastle:bcprov-jdk15on#1.54");
		list.add("org.bouncycastle:bcprov-jdk15on#1.54");
		list.add("org.bouncycastle:bctsp-jdk14#1.38");
		list.add("org.ccil.cowan.tagsoup:tagsoup#1.2.1");
		list.add("org.ccil.cowan.tagsoup:tagsoup#1.2.1");
		list.add("org.codehaus.plexus:plexus-utils#1.5.6");
		list.add("org.codehaus.plexus:plexus-utils#1.5.6");
		list.add("org.codehaus.woodstox:stax2-api#3.1.4");
		list.add("org.codehaus.woodstox:stax2-api#3.1.4");
		list.add("org.codehaus.woodstox:woodstox-core-asl#4.4.1");
		list.add("org.codehaus.woodstox:woodstox-core-asl#4.4.1");
		list.add("org.codelibs:jhighlight#1.0.2");
		list.add("org.codelibs:jhighlight#1.0.2");
		list.add("org.fusesource.hawtbuf:hawtbuf#1.11");
		list.add("org.fusesource.hawtbuf:hawtbuf#1.11");
		list.add("org.gagravarr:vorbis-java-core#0.8");
		list.add("org.gagravarr:vorbis-java-core#0.8");
		list.add("org.gagravarr:vorbis-java-tika#0.8");
		list.add("org.gagravarr:vorbis-java-tika#0.8");
		list.add("org.glassfish:javax.json#1.0.4");
		list.add("org.glassfish.api:mail#1.4.1");
		list.add("org.hibernate:hibernate-commons-annotations#3.2.0.Final");
		list.add("org.hibernate:hibernate-core#3.6.10.Final");
		list.add("org.hibernate.javax.persistence:hibernate-jpa-2.0-api#1.0.1.Final");
		list.add("org.jdom:jdom#1.1");
		list.add("org.jdom:jdom#1.1");
		list.add("org.jdom:jdom#1.1");
		list.add("org.json:json#20140107");
		list.add("org.json:json#20140107");
		list.add("org.ow2.asm:asm#5.0.4");
		list.add("org.ow2.asm:asm#5.0.4");
		list.add("org.slf4j:jcl-over-slf4j#1.7.12");
		list.add("org.slf4j:jcl-over-slf4j#1.7.12");
		list.add("org.slf4j:jcl-over-slf4j#1.7.12");
		list.add("org.slf4j:log4j-over-slf4j#1.7.12");
		list.add("org.slf4j:log4j-over-slf4j#1.7.12");
		list.add("org.slf4j:log4j-over-slf4j#1.7.12");
		list.add("org.slf4j:slf4j-api#1.7.13");
		list.add("org.slf4j:slf4j-api#1.7.24");
		list.add("org.slf4j:slf4j-api#1.7.24");
		list.add("org.slf4j:slf4j-jdk14#1.7.12");
		list.add("org.slf4j:slf4j-jdk14#1.7.12");
		list.add("org.slf4j:slf4j-jdk14#1.7.12");
		list.add("org.springframework:spring-aop#3.0.5.RELEASE");
		list.add("org.springframework:spring-aop#3.0.5.RELEASE");
		list.add("org.springframework:spring-aop#3.0.5.RELEASE");
		list.add("org.springframework:spring-asm#3.0.5.RELEASE");
		list.add("org.springframework:spring-asm#3.0.5.RELEASE");
		list.add("org.springframework:spring-asm#3.0.5.RELEASE");
		list.add("org.springframework:spring-beans#3.0.5.RELEASE");
		list.add("org.springframework:spring-beans#3.0.5.RELEASE");
		list.add("org.springframework:spring-beans#3.0.5.RELEASE");
		list.add("org.springframework:spring-context-support#3.0.5.RELEASE");
		list.add("org.springframework:spring-context-support#3.0.5.RELEASE");
		list.add("org.springframework:spring-context#3.0.5.RELEASE");
		list.add("org.springframework:spring-context#3.0.5.RELEASE");
		list.add("org.springframework:spring-context#3.0.5.RELEASE");
		list.add("org.springframework:spring-core#3.0.5.RELEASE");
		list.add("org.springframework:spring-core#3.0.5.RELEASE");
		list.add("org.springframework:spring-core#3.0.5.RELEASE");
		list.add("org.springframework:spring-expression#3.0.5.RELEASE");
		list.add("org.springframework:spring-expression#3.0.5.RELEASE");
		list.add("org.springframework:spring-expression#3.0.5.RELEASE");
		list.add("org.springframework:spring-jdbc#3.0.5.RELEASE");
		list.add("org.springframework:spring-orm#3.0.5.RELEASE");
		list.add("org.springframework:spring-tx#3.0.5.RELEASE");
		list.add("org.springframework:spring-web#3.0.5.RELEASE");
		list.add("org.springframework:spring-web#3.0.5.RELEASE");
		list.add("org.springframework:spring-web#3.0.5.RELEASE");
		list.add("org.swinglabs:pdf-renderer#1.0.5");
		list.add("org.tallison:jmatio#1.2");
		list.add("org.tallison:jmatio#1.2");
		list.add("org.threeten:threetenbp#1.3.1");
		list.add("org.tukaani:xz#1.5");
		list.add("org.tukaani:xz#1.5");
		list.add("regexp:regexp#1.3");
		list.add("regexp:regexp#1.3");
		list.add("xalan:xalan#2.7.0");
		list.add("xerces:xercesImpl#2.8.0");
		list.add("xml-apis:xml-apis#1.0.b2");
		list.add("xml-apis:xml-apis#2.0.2");
		list.add("xom:xom#1.2.5");
		list.add("unknown:unknown#10000000");

		checkAvail.setArtifactIdList(list);
		checkAvail.setResolveDependencies(false);
		checkAvail.setIncludeTerminalArtifact(true);

		EvalContext<? extends LicenseAvailability> eval = checkAvail.eval(librarySession);
		LicenseAvailability licenseAvailability = eval.get();

		List<String> licensesAvailable = licenseAvailability.getLicensesAvailable();
		log("====================================");
		log("Available: (" + licensesAvailable.size() + ")");
		for (String l : licensesAvailable) {
			log("   " + l);
		}
		assertThat(licensesAvailable.size()).isEqualTo(169);
		List<String> licensesMissing = licenseAvailability.getLicensesMissing();
		log("====================================");
		log("Missing: (" + licensesMissing.size() + ")");
		for (String l : licensesMissing) {
			log("   " + l);
		}
		assertThat(licensesMissing.size()).isEqualTo(1);
	}

	@Test
	public void testCreateImportExcel() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("avalon-framework:avalon-framework#100000"); // unknown, but a previous version available
		list.add("unknown:unknown#100000"); // completely unknown

		CreateLicenseImportSpreadsheet clds = CreateLicenseImportSpreadsheet.T.create();
		clds.setArtifactIdList(list);
		clds.setResolveDependencies(false);
		clds.setIncludeTerminalArtifact(true);

		EvalContext<? extends LicenseImportSpreadsheet> eval = clds.eval(librarySession);
		LicenseImportSpreadsheet doc = eval.get();

		Resource docResource = doc.getLicenseImportSpreadsheet();

		// make a copy
		File tempFile = File.createTempFile("import", ".xlsx");
		try (InputStream openStream = docResource.openStream()) {
			IOTools.inputToFile(openStream, tempFile);
		}
		log("Downloaded Import Excel file.");

		try {
			List<List<String>> content = readExcel(tempFile);
			assertThat(content).isNotNull();
			assertThat(content.size()).isEqualTo(3);
			//@formatter:off
			assertThat(content.get(0)).isEqualTo(
					list(
							"Artifact", 
							"Filename", 
							"License ID",
							"SPDX License", 
							"SPDX Expression", 
							"SHA1", 
							"SHA256",
							"Name", 
							"Artifact URL", 
							"Copyright", 
							"Organization", 
							"Template"
						));

			assertThat(content.get(1)).isEqualTo(
					list(
							"avalon-framework:avalon-framework#100000", 
							"avalon-framework-100000.jar",
							"Apache License, Version 2.0",
							"Apache-2.0",
							"Apache-2.0",
							"x",
							"x",
							"Avalon Framework",
							"https://avalon.apache.org/", 
							"Copyright (C) The Apache Software Foundation. All rights reserved.",
							"The Apache Software Foundation", 
							"Based on: avalon-framework:avalon-framework#4.1.5"
						));
						
			assertThat(content.get(2)).isEqualTo(
					list(
							"unknown:unknown#100000", 
							"unknown-100000.jar",
							"x", 
							"x", 
							"x", 
							"x", 
							"x", 
							"x", 
							"x", 
							"x", 
							"x"));
			//@formatter:on

		} finally {
			FileTools.deleteFile(tempFile);
		}
	}

	private static List<List<String>> readExcel(File file) throws Exception {

		List<List<String>> result = new ArrayList<>();

		Workbook workbook = null;
		FileInputStream excelFile = null;
		try {

			excelFile = new FileInputStream(file);
			workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(0);

			Iterator<Row> iterator = datatypeSheet.iterator();

			while (iterator.hasNext()) {

				List<String> rowList = new ArrayList<>();
				result.add(rowList);

				Row currentRow = iterator.next();

				Iterator<Cell> cellIterator = currentRow.iterator();
				while (cellIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					rowList.add(currentCell.getStringCellValue());
				}
			}
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find Excel file: " + file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new Exception("Error while reading Excel file: " + file.getAbsolutePath(), e);
		} finally {
			IOTools.closeCloseable(workbook, null);
			IOTools.closeCloseable(excelFile, null);
		}

		return result;
	}

	private static void addLibraryHeader(Row row) {
		row.createCell(0).setCellValue("Artifact");
		row.createCell(0).setCellValue("Filename");
		row.createCell(1).setCellValue("License ID");
		row.createCell(2).setCellValue("SPDX License");
		row.createCell(2).setCellValue("SPDX Expression");
		row.createCell(2).setCellValue("Name");
		row.createCell(2).setCellValue("SHA1");
		row.createCell(2).setCellValue("SHA256");
		row.createCell(3).setCellValue("Artifact URL");
		row.createCell(4).setCellValue("Copyright");
		row.createCell(5).setCellValue("Organization");
		row.createCell(6).setCellValue("Template");
	}

	private static void addLibraryExcelRow(Workbook wb, Row row, String artifactId, String filename, String licenseIds, String spdxId,
			String spdxExpression, String sha1, String sha256, String name, String artifactUrl, String copyright, String organization,
			String comment) {

		CellStyle wrapStyle = wb.createCellStyle();
		wrapStyle.setWrapText(true);

		row.createCell(0).setCellValue(artifactId);
		row.createCell(1).setCellValue(filename);
		row.createCell(2).setCellValue(licenseIds != null ? licenseIds : "x");
		row.createCell(3).setCellValue(spdxId != null ? spdxId : "x");
		row.createCell(4).setCellValue(spdxExpression != null ? spdxExpression : "x");
		row.createCell(5).setCellValue(sha1 != null ? sha1 : "x");
		row.createCell(6).setCellValue(sha256 != null ? sha256 : "x");
		row.createCell(7).setCellValue(name != null ? name : "x");
		row.createCell(8).setCellValue(artifactUrl != null ? artifactUrl : "x");
		Cell copyrightCell = row.createCell(9);
		copyrightCell.setCellStyle(wrapStyle);
		copyrightCell.setCellValue(copyright != null ? copyright : "x");
		row.createCell(10).setCellValue(organization != null ? organization : "x");
		if (!StringTools.isEmpty(comment)) {
			row.createCell(11).setCellValue(comment);
		}
	}

	private static File generateImportExcel() throws Exception {
		File tempFile = File.createTempFile("import", ".xlsx");

		Workbook wb = new XSSFWorkbook();

		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			Sheet sheet = wb.createSheet("License Import");

			Row row = sheet.createRow(0);
			addLibraryHeader(row);

			short rowNumber = 1;
			row = sheet.createRow(rowNumber++);

			addLibraryExcelRow(wb, row, "nowKnown:nowKnown#1", "nowKnow-1.jar", "Apache License, Version 2.0", "Apache-2.0", "Apache-2.0", "SHA-1",
					"SHA-256", "Some new Library", "https://example.com/", "Copyright (C) Founder.", "Founder", null);
			row = sheet.createRow(rowNumber++);
			addLibraryExcelRow(wb, row, "missingInfo:missingInfo#1", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", null);
			row = sheet.createRow(rowNumber++);
			addLibraryExcelRow(wb, row, "unknownLicense:unknownLicense#1", "unknownLicense-1.jar", "Some unknown License", "SPDX-Unknown",
					"SPDX-Unknown", "SHA-1", "SHA-256", "Some new Library", "https://example.com/", "Copyright (C) Founder.", "Founder", null);
			row = sheet.createRow(rowNumber++);
			addLibraryExcelRow(wb, row, "avalon-framework:avalon-framework#4.1.5", "avalon-framework-4.1.5.jar", "Apache License, Version 2.0",
					"Apache-2.0", "Apache-2.0", "3532aaf90b552ed1e1e1e29392b77b3b1980d8a8",
					"833f1e50dfa628f13d4a4206b3ec6d8f42e96284a35d76c072d880804b747a04", "Avalon Framework", "https://avalon.apache.org/",
					"Copyright (C) The Apache Software Foundation. All rights reserved.", "The Apache Software Foundation", null); // already known

			for (int col = 0; col < 6; ++col) {
				sheet.autoSizeColumn(col);
			}

			wb.write(fos);
		} catch (Exception e) {
			FileTools.deleteFile(tempFile);
			throw e;
		} finally {
			wb.close();
		}

		return tempFile;
	}

	@Test
	@Ignore // Something about fonts in CI
	public void importExcel() throws Exception {

		ImportSpreadsheet req = ImportSpreadsheet.T.create();

		byte[] spreadsheetBytes = null;
		File file = generateImportExcel();
		try {

			try (InputStream in = new FileInputStream(file)) {
				spreadsheetBytes = IOTools.slurpBytes(in);
			}
			Resource callResource = Resource.createTransient(MemoryInputStreamProviders.from(spreadsheetBytes));

			callResource.setName("license-import.xlsx");
			callResource.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			callResource.setFileSize((long) spreadsheetBytes.length);
			callResource.setCreated(new Date());

			req.setImportSpreadsheet(callResource);

			EvalContext<? extends SpreadsheetImportReport> eval = req.eval(librarySession);
			SpreadsheetImportReport report = eval.get();

			log("====================================");
			log("Imported: (" + report.getLibrariesImported().size() + ")");
			for (String l : report.getLibrariesImported()) {
				log("   " + l);
			}
			log("====================================");
			log("Already Imported: (" + report.getLibrariesAlreadyImported().size() + ")");
			for (String l : report.getLibrariesAlreadyImported()) {
				log("   " + l);
			}
			log("====================================");
			log("Not Imported: (" + report.getLibrariesNotImported().size() + ")");
			for (String l : report.getLibrariesNotImported()) {
				log("   " + l);
			}
			log("====================================");
			log("Licenses Found: (" + report.getLicensesFound().size() + ")");
			for (String l : report.getLicensesFound()) {
				log("   " + l);
			}
			log("====================================");
			log("Licenses Not Found: (" + report.getLicensesMissing().size() + ")");
			for (String l : report.getLicensesMissing()) {
				log("   " + l);
			}
			log("====================================");
			log(report.getMessage());

			assertThat(report.getLibrariesImported().size()).isEqualTo(1); // nowKnown
			assertThat(report.getLibrariesImported().get(0)).isEqualTo("nowKnown:nowKnown#1");

			assertThat(report.getLibrariesAlreadyImported().size()).isEqualTo(1); // avalon-framework
			assertThat(report.getLibrariesAlreadyImported().get(0)).isEqualTo("avalon-framework:avalon-framework#4.1.5");

			assertThat(report.getLibrariesNotImported().size()).isEqualTo(2); // missingInfo and unknownLicense
			assertThat(report.getLibrariesNotImported()).contains("missingInfo:missingInfo#1", "unknownLicense:unknownLicense#1");

			assertThat(report.getLicensesFound().size()).isEqualTo(1); // Apache
			assertThat(report.getLicensesFound().get(0)).isEqualTo("Apache License, Version 2.0");

			assertThat(report.getLicensesMissing().size()).isEqualTo(1); // Some unknown License
			assertThat(report.getLicensesMissing().get(0)).isEqualTo("Some unknown License");

		} finally {
			FileTools.deleteFile(file);
		}
	}

	@Test
	public void createExportExcel() throws Exception {

		ExportLicenseSpreadsheet clds = ExportLicenseSpreadsheet.T.create();

		EvalContext<? extends LicenseExportSpreadsheet> eval = clds.eval(librarySession);
		LicenseExportSpreadsheet doc = eval.get();

		Resource docResource = doc.getLicenseExportSpreadsheet();

		File tempFile = File.createTempFile("export", ".xlsx");

		try {
			try (InputStream openStream = docResource.openStream()) {
				IOTools.inputToFile(openStream, tempFile);
			}
			List<List<String>> content = readExcel(tempFile);
			int size = content.size();
			log("Export excel contains " + size + " rows.");

			assertThat(size).isGreaterThan(450);

		} finally {
			FileTools.deleteFile(tempFile);
		}
	}

}
