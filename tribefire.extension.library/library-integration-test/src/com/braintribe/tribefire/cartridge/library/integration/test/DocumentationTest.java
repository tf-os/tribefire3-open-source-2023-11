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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.junit.Test;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.library.service.documentation.DocumentLibraries;
import com.braintribe.model.library.service.documentation.LibraryDocumentation;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class DocumentationTest extends LibraryIntegrationTestBase {

	@Test
	public void testLicenseReportGeneration() throws Exception {
		DocumentLibraries dl = DocumentLibraries.T.create();
		List<String> list = new ArrayList<>();
		list.add("edu.ucar:thredds-parent#4.5.5");
		list.add("org.jboss:jandex#1.1.0.Final");
		list.add("com.googlecode.mp4parser:isoparser#1.0.2");
		list.add("joda-time:joda-time#2.3");
		list.add("joda-time:joda-time#2.8.2");
		list.add("joda-time:joda-time#2.9.5");
		list.add("org.apache.lucene:lucene-solr-grandparent#5.4.1");
		list.add("org.apache.lucene:lucene-solr-grandparent#6.5.0");
		list.add("org.joda:joda-convert#1.2");
		list.add("com.google.code.gson:gson-parent#2.6.2");
		list.add("com.github.spullara.mustache.java:compiler#0.9.3");
		list.add("com.ning:compress-lzf#1.0.2");
		list.add("de.l3s.boilerpipe:boilerpipe#1.1.0");
		list.add("org.ccil.cowan.tagsoup:tagsoup#1.2.1");
		list.add("org.mortbay.jasper:jasper-jsp#8.0.9.M3");
		list.add("org.yaml:snakeyaml#1.15");
		list.add("com.healthmarketscience:openhms-parent#1.1.1");
		list.add("com.healthmarketscience:openhms-parent#1.1.4");
		list.add("io.netty:netty#3.10.5.Final");
		list.add("io.netty:netty#3.10.6.Final");
		list.add("io.netty:netty-parent#4.1.9.Final");
		list.add("javax.validation:validation-api#1.0.0.GA");
		list.add("net.sourceforge.htmlunit:htmlunit#2.19");
		list.add("org.apache.httpcomponents:httpcomponents-asyncclient#4.1.2");
		list.add("org.apache.httpcomponents:httpcomponents-client#4.3.6");
		list.add("org.apache.httpcomponents:httpcomponents-client#4.5.3");
		list.add("org.apache.httpcomponents:httpcomponents-core#4.3.3");
		list.add("org.apache.httpcomponents:httpcomponents-core#4.4.5");
		list.add("org.apache.httpcomponents:httpcomponents-core#4.4.6");
		list.add("org.apache:apache#15");
		list.add("org.apache:apache#16");
		list.add("org.apache:apache#17");
		list.add("org.apache:apache#18");
		list.add("org.assertj:assertj-parent-pom#2.1.4");
		list.add("org.easytesting:fest#1.0.8");
		list.add("org.jboss.logging:jboss-logging#3.1.3.GA");
		list.add("com.googlecode.mp4parser:isoparser#1.1.18");
		list.add("org.eclipse.jetty:jetty-parent#18");
		list.add("org.eclipse.jetty:jetty-parent#23");
		list.add("org.slf4j:log4j-over-slf4j#1.7.12");
		list.add("cglib:cglib-nodep#2.2");
		list.add("org.bouncycastle:bcmail-jdk14#1.38");
		list.add("org.bouncycastle:bcmail-jdk15on#1.52");
		list.add("org.bouncycastle:bcmail-jdk15on#1.55");
		list.add("org.bouncycastle:bcpkix-jdk15on#1.52");
		list.add("org.bouncycastle:bcpkix-jdk15on#1.55");
		list.add("org.bouncycastle:bcprov-jdk14#1.38");
		list.add("org.bouncycastle:bcprov-jdk15on#1.52");
		list.add("org.bouncycastle:bcprov-jdk15on#1.55");
		list.add("org.bouncycastle:bctsp-jdk14#1.38");
		list.add("net.sourceforge.jmatio:jmatio#1.0");
		list.add("org.ow2.asm:asm-parent#5.0.3");
		list.add("org.ow2.asm:asm-parent#5.0.4");
		list.add("org.tallison:jmatio#1.2");
		list.add("org.threeten:threetenbp#1.3.1");
		list.add("org.threeten:threetenbp#1.3.3");
		list.add("org.abego.treelayout:org.abego.treelayout.core#1.0.1");
		list.add("net.sf.jwordnet:jwnl#1.3.3");
		list.add("org.antlr:antlr4#4.4");
		list.add("org.antlr:antlr4-runtime#4.4");
		list.add("org.antlr:ST4#4.0.8");
		list.add("org.antlr:antlr-master#3.5.2");
		list.add("org.antlr:stringtemplate#3.2");
		list.add("antlr:antlr#2.7.7");
		list.add("com.github.virtuald:curvesapi#1.04");
		list.add("dom4j:dom4j#1.6.1");
		list.add("org.antlr:antlr-runtime#3.1.1");
		list.add("com.twitter:jsr166e#1.1.0");
		list.add("javax.annotation:javax.annotation-api#1.2");
		list.add("javax.servlet:javax.servlet-api#3.1.0");
		list.add("org.glassfish:pom#2");
		list.add("com.sun.media:jai-codec#1.1.3");
		list.add("javax.transaction:javax.transaction-api#1.2");
		list.add("javax.ws.rs:javax.ws.rs-api#2.0.1");
		list.add("org.glassfish.api:mail#1.4.1");
		list.add("org.glassfish:javax.json#1.0.4");
		list.add("rome:rome#1.0");
		list.add("org.codelibs:jhighlight#1.0.2");
		list.add("com.aspose:aspose-cells#8.5.2");
		list.add("com.aspose:aspose-imaging#2.6.0");
		list.add("com.aspose:aspose-pdf#10.6.1");
		list.add("com.aspose:aspose-slides#15.6.0");
		list.add("com.aspose:aspose-words#15.7.0");
		list.add("com.auxilii:glf-client#1.1");
		list.add("org.jpedal:jpedal#8.11.6");
		list.add("org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec#1.0.0.Final");
		list.add("javax.activation:activation#1.1.1");
		list.add("com.github.dblock:oshi-core#2.6-m-java7");
		list.add("com.github.oshi:oshi-parent#3.4.2");
		list.add("org.aspectj:aspectjrt#1.8.0");
		list.add("org.hibernate.javax.persistence:hibernate-jpa-2.1-api#1.0.0.Final");
		list.add("junit:junit#4.12");
		list.add("com.lowagie:itext#4.2.1");
		list.add("jfree:jcommon#1.0.15");
		list.add("jfree:jfreechart#1.0.12");
		list.add("c3p0:c3p0#0.9.1.1");
		list.add("colt:colt#1.2.0");
		list.add("net.sourceforge.cssparser:cssparser#0.9.18");
		list.add("net.sourceforge.jtds:jtds#1.2");
		list.add("org.beanshell:beanshell#2.0b4");
		list.add("org.hibernate.common:hibernate-commons-annotations#4.0.5.Final");
		list.add("org.hibernate.javax.persistence:hibernate-jpa-2.0-api#1.0.1.Final");
		list.add("org.hibernate:hibernate-commons-annotations#3.2.0.Final");
		list.add("org.hibernate:hibernate-core#4.3.10.Final");
		list.add("org.hibernate:hibernate-ehcache#4.3.10.Final");
		list.add("org.hibernate:hibernate-parent#3.6.10.Final");
		list.add("flyingsaucer:flyingsaucer#8");
		list.add("com.mchange:c3p0#0.9.5");
		list.add("com.mchange:c3p0#0.9.5.2");
		list.add("com.mchange:mchange-commons-java#0.2.11");
		list.add("com.mchange:mchange-commons-java#0.2.9");
		list.add("com.google.gwt:gwt#2.8.0");
		list.add("com.google.jsinterop:jsinterop#1.0.1");
		list.add("com.ibm.icu:icu4j#50.1.1");
		list.add("net.sf.jtidy:jtidy#r938");
		list.add("javax.media:jai_core#1.1.3");
		list.add("com.vividsolutions:jts#1.13");
		list.add("org.swinglabs:pdf-renderer#1.0.5");
		list.add("net.java.dev.jna:jna#4.2.2");
		list.add("net.java.dev.jna:jna#4.3.0");
		list.add("net.java.dev.jna:jna#4.4.0");
		list.add("net.java.dev.jna:jna-platform#4.2.2");
		list.add("net.java.dev.jna:jna-platform#4.3.0");
		list.add("org.slf4j:slf4j-parent#1.7.12");
		list.add("org.slf4j:slf4j-parent#1.7.13");
		list.add("org.slf4j:slf4j-parent#1.7.25");
		list.add("org.itadaki:bzip2#0.9.1");
		list.add("com.googlecode.juniversalchardet:juniversalchardet#1.0.3");
		list.add("net.sf.saxon:Saxon-HE#9.7.0-15");
		list.add("net.sourceforge.htmlunit:htmlunit-core-js#2.17");
		list.add("javassist:javassist#3.12.1.GA");
		list.add("jboss:javassist#3.0");
		list.add("org.javassist:javassist#3.18.1-GA");
		list.add("com.google.protobuf:protobuf-java#2.5.0");
		list.add("org.hamcrest:hamcrest-parent#1.3");
		list.add("org.opengis:geoapi-parent#3.0.0");
		list.add("aopalliance:aopalliance#1.0");
		list.add("net.jcip:jcip-annotations#1.0");
		list.add("org.jboss:jboss-parent#10");
		list.add("org.tukaani:xz#1.5");
		list.add("org.hdrhistogram:HdrHistogram#2.1.6");
		list.add("org.hdrhistogram:HdrHistogram#2.1.9");
		list.add("concurrent:concurrent#1.3.4");
		list.add("org.jdom:jdom2#2.0.4");
		list.add("org.jdom:jdom2#2.0.6");
		list.add("javax.measure:jsr-275#0.9.3");
		list.add("com.beust:jcommander#1.35");
		list.add("com.carrotsearch:hppc-parent#0.7.1");
		list.add("com.drewnoakes:metadata-extractor#2.8.0");
		list.add("com.drewnoakes:metadata-extractor#2.9.1");
		list.add("com.fasterxml.jackson:jackson-parent#2.6.1");
		list.add("com.fasterxml.jackson:jackson-parent#2.8");
		list.add("com.fasterxml:oss-parent#11");
		list.add("com.fasterxml:oss-parent#12");
		list.add("com.google.code.gson:gson#2.2.4");
		list.add("com.google.guava:guava-parent#16.0");
		list.add("com.google.guava:guava-parent#18.0");
		list.add("com.google.guava:guava-parent#19.0");
		list.add("com.googlecode.json-simple:json-simple#1.1.1");
		list.add("com.pff:java-libpst#0.8.1");
		list.add("com.rometools:rome-parent#1.5.1");
		list.add("com.spatial4j:spatial4j#0.5");
		list.add("com.tdunning:t-digest#3.0");
		list.add("com.zaxxer:HikariCP#2.6.2");
		list.add("com.zaxxer:HikariCP-java6#2.3.13");
		list.add("com.zaxxer:HikariCP-java7#2.4.12");
		list.add("commons-fileupload:commons-fileupload#1.1");
		list.add("commons-logging:commons-logging-api#1.1");
		list.add("edu.ucar:jj2000#5.2");
		list.add("hivemind:hivemind#1.1.1");
		list.add("hivemind:hivemind-lib#1.1.1");
		list.add("jdom:jdom#1.0");
		list.add("net.sf.ehcache:ehcache-core#2.5.0");
		list.add("net.sf.ehcache:ehcache-core#2.6.11");
		list.add("net.sf.ehcache:ehcache-core#2.6.2");
		list.add("net.sourceforge.nekohtml:nekohtml#1.9.22");
		list.add("ognl:ognl#2.6.7");
		list.add("org.apache.ant:ant-parent#1.8.2");
		list.add("org.apache.cxf:cxf#3.0.3");
		list.add("org.apache.geronimo.genesis.config:project-config#1.2");
		list.add("org.apache.logging.log4j:log4j#2.2");
		list.add("org.apache.poi:poi#3.13");
		list.add("org.apache.poi:poi#3.13-beta1");
		list.add("org.apache.poi:poi#3.15");
		list.add("org.apache.poi:poi-ooxml#3.13");
		list.add("org.apache.poi:poi-ooxml#3.13-beta1");
		list.add("org.apache.poi:poi-ooxml#3.15");
		list.add("org.apache.poi:poi-ooxml-schemas#3.13");
		list.add("org.apache.poi:poi-ooxml-schemas#3.13-beta1");
		list.add("org.apache.poi:poi-ooxml-schemas#3.15-beta1");
		list.add("org.apache.poi:poi-scratchpad#3.13");
		list.add("org.apache.poi:poi-scratchpad#3.13-beta1");
		list.add("org.apache.poi:poi-scratchpad#3.15");
		list.add("org.apache.ws.xmlschema:xmlschema#2.1.0");
		list.add("org.apache.xmlbeans:xmlbeans#2.6.0");
		list.add("org.apache:apache#1");
		list.add("org.apache:apache#10");
		list.add("org.apache:apache#13");
		list.add("org.apache:apache#3");
		list.add("org.apache:apache#4");
		list.add("org.apache:apache#7");
		list.add("org.apache:apache#9");
		list.add("org.codehaus.groovy:groovy-all#2.4.6");
		list.add("org.codehaus.plexus:plexus#1.0.12");
		list.add("org.codehaus.woodstox:woodstox-core-asl#4.4.1");
		list.add("org.elasticsearch.client:rest#5.4.0");
		list.add("org.elasticsearch.client:transport#5.4.0");
		list.add("org.elasticsearch.plugin:lang-mustache-client#5.4.0");
		list.add("org.elasticsearch.plugin:percolator-client#5.4.0");
		list.add("org.elasticsearch.plugin:reindex-client#5.4.0");
		list.add("org.elasticsearch.plugin:transport-netty3-client#5.4.0");
		list.add("org.elasticsearch.plugin:transport-netty4-client#5.4.0");
		list.add("org.elasticsearch:elasticsearch#5.4.0");
		list.add("org.elasticsearch:ingest-attachments#5.4.0");
		list.add("org.elasticsearch:jna#4.4.0");
		list.add("org.elasticsearch:parent#2.2.1");
		list.add("org.elasticsearch:securesm#1.0");
		list.add("org.elasticsearch:securesm#1.1");
		list.add("org.fusesource.hawtbuf:hawtbuf-project#1.11");
		list.add("org.gagravarr:vorbis-java-parent#0.6");
		list.add("org.gagravarr:vorbis-java-parent#0.8");
		list.add("org.jdom:jdom#1.1");
		list.add("org.locationtech.spatial4j:spatial4j#0.6");
		list.add("org.mitre.dsmiley.httpproxy:smiley-http-proxy-servlet#1.9");
		list.add("org.quartz-scheduler:quartz-parent#2.2.0");
		list.add("org.quartz-scheduler:quartz-parent#2.3.0");
		list.add("org.springframework:spring-parent#3.0.5.RELEASE");
		list.add("oro:oro#2.0.8");
		list.add("regexp:regexp#1.3");
		list.add("stax:stax-api#1.0.1");
		list.add("tapestry:tapestry#4.0.2");
		list.add("xalan:xalan#2.7.0");
		list.add("xml-apis:xml-apis#1.0.b2");
		list.add("xml-apis:xml-apis#2.0.2");
		list.add("com.adobe.xmp:xmpcore#5.1.2");
		list.add("org.codehaus.woodstox:stax2-api#3.1.4");
		list.add("xom:xom#1.2.5");
		list.add("org.json:json#20140107");
		list.add("net.sf.jopt-simple:jopt-simple#5.0.2");
		list.add("org.jsoup:jsoup#1.7.2");
		list.add("org.w3c.css:sac#1.3");
		list.add("com.github.junrar:junrar#0.7");
		dl.setArtifactIdList(list);
		dl.setResolveDependencies(false);
		dl.setIncludeTerminalArtifact(true);

		log("Sending request to generate report PDF.");
		EvalContext<? extends LibraryDocumentation> eval = dl.eval(librarySession);
		LibraryDocumentation doc = eval.get();

		assertThat(doc.getSuccess()).describedAs(doc.getMessage() != null ? doc.getMessage() : "no description").isTrue();

		Resource docResource = doc.getLibraryDocumentation();

		File tempFile = File.createTempFile("report", ".pdf");
		try (InputStream openStream = docResource.openStream()) {
			IOTools.inputToFile(openStream, tempFile);
		}
		log("Downloaded report file.");

		PDDocument pdfDocument = null;
		try {
			pdfDocument = PDDocument.load(tempFile);

			PDPageTree pages = pdfDocument.getDocumentCatalog().getPages();
			int pageCount = pages.getCount();

			log("Report has " + pageCount + " pages.");
			assertThat(pageCount).isGreaterThan(220);
		} finally {
			IOTools.closeCloseable(pdfDocument, logger);
			FileTools.deleteFile(tempFile);
		}
	}

	// See DependenciesTest for info why it's commented out.
	
	//@Test
	public void testLicenseReportGenerationWithDepResolution() throws Exception {
		DocumentLibraries dl = DocumentLibraries.T.create();
		List<String> list = new ArrayList<>();
		list.add("tribefire.extension.library:library-integration-test#[3.0,3.1)");
		dl.setArtifactIdList(list);
		dl.setResolveDependencies(true);
		dl.setIncludeTerminalArtifact(false);
		dl.getIgnoredDependencies().add("com.braintribe");
		dl.getIgnoredDependencies().add("tribefire");

		log("Sending request to generate report PDF.");
		EvalContext<? extends LibraryDocumentation> eval = dl.eval(librarySession);
		LibraryDocumentation doc = eval.get();

		String message = doc.getMessage();
		boolean success = doc.getSuccess();
		assertThat(success).describedAs(message != null ? message : "no description").isTrue();

		Resource docResource = doc.getLibraryDocumentation();

		File tempFile = File.createTempFile("report", ".pdf");
		try (InputStream openStream = docResource.openStream()) {
			IOTools.inputToFile(openStream, tempFile);
		}
		log("Downloaded report file.");

		PDDocument pdfDocument = null;
		try {
			pdfDocument = PDDocument.load(tempFile);

			PDPageTree pages = pdfDocument.getDocumentCatalog().getPages();
			int pageCount = pages.getCount();

			log("Report has " + pageCount + " pages.");
			assertThat(pageCount).isGreaterThan(50);
		} finally {
			IOTools.closeCloseable(pdfDocument, logger);
			FileTools.deleteFile(tempFile);
		}
	}

}
