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
package tribefire.extension.xml.schemed.test.commons.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.utils.xml.parser.builder.ValidateDocumentContext;

import tribefire.extension.xml.schemed.test.commons.xsd.test.util.TestUtil;

public class ArchiveValidator {
		
	public static boolean validate(InputStream in, File xmlFile) {
		try {
			ZipContext archive = Archives.zip().from( in);			
			return validate( archive, xmlFile);			
		} catch (ArchivesException e) {
			throw new IllegalStateException( "cannot open archive from passed stream");
		}			
	}
	
	public static boolean validate( File xsdArchive, File xmlFile) {
	
		try {
			ZipContext archive = Archives.zip().from(xsdArchive);			
			return validate( archive, xmlFile);			
		} catch (ArchivesException e) {
			throw new IllegalStateException( "cannot open archive from passed file [" + xsdArchive.getAbsolutePath() + "]");
		}				
	}
	
	
	private static boolean validate( ZipContext archive, File xmlFile) {
		List<InputStream> entries = new ArrayList<>();
		try {						
			List<String> entryNames = archive.getHeaders();
			for (String entryName : entryNames) {
				if (entryName.endsWith( ".xsd")) {
					ZipContextEntry zipContextEntry = archive.getEntry(entryName);
					entries.add( zipContextEntry.getPayload());
				}
			}		
			return DomParser.validate().from(xmlFile).schema(entries.toArray( new InputStream[0])).makeItSo();			
		} catch (DomParserException e) {
			throw new IllegalStateException( "cannot validate file with xsds from archive");
		}
		finally {
			if (!entries.isEmpty()) {
				for (InputStream entry : entries) {
					try {
						entry.close();
					} catch (IOException e) {
						;
					}
				}
			}
		}
	}
	
	/**
	 * validates a file against the xsds as passed in the archive
	 * @param xmlFile
	 * @param mainXsd
	 * @param archiveFile
	 * @param tempDirectory
	 * @return
	 */
	public static boolean validate( File xmlFile, String mainXsd, File archiveFile, File tempDirectory, String ... validationSettings) {
		ZipContext archive;
		try {
			archive = Archives.zip().from(archiveFile);
		} catch (ArchivesException e) {
			throw new IllegalStateException( "cannot open archive from passed file [" + archiveFile.getAbsolutePath() + "]");
		}
		
		TestUtil.ensure(tempDirectory);
		try {
			archive.unpack(tempDirectory);
		} catch (ArchivesException e) {
			throw new IllegalStateException( "cannot unpack archive from passed file [" + archiveFile.getAbsolutePath() + "]");
		}
		
		File mainXsdFile = new File( tempDirectory, mainXsd);
		
		
		try {
			SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI);
			if (validationSettings != null && validationSettings.length != 0) {
				for (String setting : validationSettings) {
					String [] split = setting.split("=");
					String key = split[0];
					String value = split[1];
					if (value.equalsIgnoreCase( "true")) {
						factory.setFeature( key, true);
					}
					else if (value.equalsIgnoreCase( "false")){
						factory.setFeature( key, false);
					}
					else {
						factory.setProperty(key, value);
					}
				}
			}
			
			ValidateDocumentContext documentContext = DomParser.validate( factory).from(xmlFile);
			
			return documentContext.schema(mainXsdFile).makeItSo();
		} catch (DomParserException e) {
			throw new IllegalStateException( "cannot validate file [" + xmlFile.getAbsolutePath() + "] with the xsds from passed file [" + archiveFile.getAbsolutePath() + "]", e);
		} catch (SAXNotRecognizedException e) {
			throw new IllegalStateException( "cannot validate file [" + xmlFile.getAbsolutePath() + "] with the xsds from passed file [" + archiveFile.getAbsolutePath() + "]", e);
		} catch (SAXNotSupportedException e) {
			throw new IllegalStateException( "cannot validate file [" + xmlFile.getAbsolutePath() + "] with the xsds from passed file [" + archiveFile.getAbsolutePath() + "]", e);
		}
				
		
	}
	
	
	
}
