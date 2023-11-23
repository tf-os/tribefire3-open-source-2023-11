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
package com.braintribe.model.processing.zargo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.w3c.dom.Document;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.xmi.converter.XmiToMetaModelCodec;
import com.braintribe.model.processing.xmi.converter.coding.MetaModelDependencyHandler;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * decodes a Zargo file to a GmMetaModel<br/>
 * written for <b>ARGO v0.34</b> & <b>XMI 1.4</b>, other versions check first and if it doesn't work, please contact me 
 *  
 * @author pit
 *
 */
public class ZargoToMetaModelConverterWorker {
	private static Logger log = Logger.getLogger(ZargoToMetaModelConverterWorker.class);
	private static String OVERLAYED_PROPERTIES = "overlayed.properties.xml";

	private OutputStream debugOutputStream;	
	private XmiToMetaModelCodec xmiToMetaModelCodec;
		
	@Configurable @Required
	public void setXmiToMetaModelCodec(XmiToMetaModelCodec xmiToMetaModelCodec) {
		this.xmiToMetaModelCodec = xmiToMetaModelCodec;
	}
	
	@Configurable
	public void setDebugOutputStream(OutputStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}

	/**
	 * actual worker function 
	 * @param inputStream - the {@link InputStream} pointing to the zargo file
	 * @return - the GmMetaModel created 
	 * @throws ZargoConverterException -
	 */
	public GmMetaModel execute(InputStream inputStream) throws ZargoConverterException {
		//
		// extract zargo 
		//
		String contents = null;
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		List<GmProperty> overlayedProperties = null;
		
		try {
			ZipEntry zipEntry = null;
			while ( (zipEntry = zipInputStream.getNextEntry()) != null) {
				// read zip entry to payload 
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				byte [] buffer = new byte[2048];
				int count  = 0;
				while ((count = zipInputStream.read( buffer)) != -1) {
					byteArrayOutputStream.write( buffer, 0, count);
				}
				byte [] payload = byteArrayOutputStream.toByteArray();			 
				
				ZargoEntry zargoEntry = new ZargoEntry();
				zargoEntry.setBuffer(payload);
				zargoEntry.setZipEntry(zipEntry);
				
				// if we find the xmi in there, write it out to the metaModel
			
				String name = zipEntry.getName();
				if (name.endsWith( ".xmi")) {
					if (debugOutputStream != null) {					
						try {
							
							debugOutputStream.write(payload);
							debugOutputStream.flush();
							debugOutputStream.close();
						} catch (Exception e) {
							String msg = "cannot write xmi contents to debug output stream";
							log.warn( msg, e);
						}										
					}
					contents = new String( payload);	
				}
				
				if (name.equalsIgnoreCase( OVERLAYED_PROPERTIES)) {
					throw new UnsupportedOperationException("not implemented yet");
					/*
					// decoding of overlayed properties should be lenient ... as there may be types in there, that 
					// the current Classpath cannot support (especially when running the extractor outside of TF
					// with a zargo created from within TF. 
					String overlayedPropertiesAsString = new String( payload, "UTF-8");					
					Document overlayedPropertiesAsDocument = DomParser.load().from(overlayedPropertiesAsString);
					GenericModelRootDomCodec<List<GmProperty>> codec = new GenericModelRootDomCodec<List<GmProperty>>();
					DecodingLenience lenience = new DecodingLenience(true);
					codec.setDecodingLenience( lenience);					
					overlayedProperties = codec.decode( overlayedPropertiesAsDocument);
					*/
				}
				
			}
		} catch (Exception e) {
			String msg ="cannot read the contents of the zargo file";
			log.error( msg, e);
			throw new ZargoConverterException(msg);
		}

		// 
		// convert to meta model 
		//
		GmMetaModel metaModel = null;
		try {
			//Document document = DomParser.loadXmlFromString( contents, false);
			Document document = DomParser.load().setNamespaceAware().from( contents);		
			xmiToMetaModelCodec.setOverlayedProperties(overlayedProperties);
			metaModel = xmiToMetaModelCodec.decode(document);			
		} catch (DomParserException e) {
			String msg ="cannot load the document from the xmi in the zargo file]";
			log.error( msg, e);
			throw new ZargoConverterException(msg);
		} catch (CodecException e) {
			String msg ="cannot convert the xmi contained in the zargo file";
			log.error( msg, e);
			throw new ZargoConverterException(msg);
		}
		
		MetaModelDependencyHandler.logTypes(metaModel);
		
		return metaModel;

	}

}
