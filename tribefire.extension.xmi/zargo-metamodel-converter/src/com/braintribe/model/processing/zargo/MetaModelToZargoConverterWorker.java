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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.xmi.converter.XmiToMetaModelCodec;
import com.braintribe.model.processing.xmi.converter.coding.MetaModelDependencyHandler;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * encodes a GmMetaModel to a Zargo file<br/> 
 * written for <b>ARGO v0.34</b> & <b>XMI 1.4</b>, other versions check first and if it doesn't work, please contact me 
 * 
 * @author pit
 *
 */
public class MetaModelToZargoConverterWorker {
	
	private static Logger log = Logger.getLogger(MetaModelToZargoConverterWorker.class);
	private static String OVERLAYED_PROPERTIES = "overlayed.properties.xml";

	private XmiToMetaModelCodec xmiToMetaModelCodec;
	
	private Supplier<String> argoProvider;
	private Supplier<String> profileProvider;
	private Supplier<String> toDoProvider;
	private Supplier<String> diagrammProvider;
	
	
	
	OutputStream debugOutputStream;
	 
	@Required
	public void setXmiToMetaModelCodec(XmiToMetaModelCodec xmiToMetaModelCodec) {
		this.xmiToMetaModelCodec = xmiToMetaModelCodec;
	}
	@Required
	public void setArgoProvider(Supplier<String> argoProvider) {
		this.argoProvider = argoProvider;
	}
	@Required
	public void setProfileProvider(Supplier<String> profileProvider) {
		this.profileProvider = profileProvider;
	}
	@Required
	public void setToDoProvider(Supplier<String> toDoProvider) {
		this.toDoProvider = toDoProvider;
	}
	@Required
	public void setDiagrammProvider(Supplier<String> diagrammProvider) {
		this.diagrammProvider = diagrammProvider;
	}	
	

	@Configurable 
	public void setDebugOutputStream(OutputStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}
	
	
	
	/**
	 * actual worker function
	 * @throws ZargoConverterException -
	 */
	public void execute(GmMetaModel metaModel, OutputStream outputStream, InputStream inputStream) throws ZargoConverterException {		
		// 
		// write debugging info 
		//
		
		/*
		if (debugOutputStream != null) {
			final List<String> instances = new ArrayList<String>();
			GmMetaModel.T.traverse(metaModel, null, new EntityVisitor() {
				
				@Override
				protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
					String identity = entity.getClass().getName();
					instances.add(identity);
				}
			});		
			Collections.sort(instances);
			StringBuilder builder = new StringBuilder();
			Iterator<String> iterator2 = instances.iterator();
			while (iterator2.hasNext()) {			
				if (builder.length() > 0)
					builder.append("\n");
				builder.append(iterator2.next());
			}
			
		
			try {
				OutputStreamWriter writer = new OutputStreamWriter(debugOutputStream, "UTF-8");
				writer.write( builder.toString());
				writer.close();
			} catch (IOException e1) {
				String msg = "cannot write class name list to debug output stream";
				log.error( msg, e1);
				throw new ZargoConverterException(msg, e1);
			} 
			finally {
				if (debugOutputStream != null)
					try {
						debugOutputStream.flush();
						debugOutputStream.close();
					} catch (IOException e) {
						String msg = "cannot close debug outputstream from provider";
						log.error( msg, e);
						throw new ZargoConverterException(msg, e);
					}
			}
		}
		*/
		
		
		
		MetaModelDependencyHandler.logTypes(metaModel);
		
		//
		// start processing here 
		//
		String metaModelName = metaModel.getName();
		String modelNameAsFileName = metaModelName.replace( ":", ".");
		modelNameAsFileName = modelNameAsFileName.replace( "#", "-");
		
	
		
		// 
		// read (or create) payloads for zargo file 
		// 
		byte [] xmiBuffer = null;
		ZargoEntry xmiEntry = null;
		List<ZargoEntry> zargoPayload = new ArrayList<ZargoEntry>();
		
		//
		// read in an existing zargo if there's one 
		// 
		
		if (inputStream != null) {
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			
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
					
					String name = zipEntry.getName();	
					// we do not read in the properties file 
					if (name.equalsIgnoreCase( OVERLAYED_PROPERTIES)) {
						continue;
					}
					ZargoEntry zargoEntry = new ZargoEntry();
					zargoEntry.setBuffer(payload);
					zargoEntry.setZipEntry(zipEntry);
					zargoPayload.add( zargoEntry);
					// if we find the xmi in there, write it out to the metaModel
				
					if (name.endsWith( ".xmi")) {
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						output.write(payload);
						output.close();			
						xmiBuffer = output.toByteArray();			
						//
						xmiEntry = zargoEntry;
					}
					
				}				
			} catch (IOException e) {
				String msg = "cannot parse the zip input stream";
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
			finally {				
				try {
					zipInputStream.close();
				} catch (IOException e) {
					String msg = "cannot close the zip input stream";
					log.error( msg, e);
					throw new ZargoConverterException(msg, e);
				}
			}
		}
		
		// prepare the recycle stream for the codec 
		ByteArrayInputStream recycleStream = null;
		if (xmiBuffer != null) {
			recycleStream = new ByteArrayInputStream(xmiBuffer);
			xmiToMetaModelCodec.setRecycleInputStream(recycleStream);		
		}
		
		List<GmProperty> overlayedProperties = null;
		//
		// actually build the xmi 
		//
		String modelId;
		String xmiAsString;
		try {
			Document xmiDocument = xmiToMetaModelCodec.encode( metaModel);	
			Element modelE = DomUtils.getElementByPath( xmiDocument.getDocumentElement(), ".*content/.*Model", false);
			modelId = modelE.getAttribute( "xmi.id");			
			//xmiAsString = DomParser.saveXmlToString(xmiDocument, false, false, null, null);
			xmiAsString = DomParser.write().from(xmiDocument).to();
			overlayedProperties = xmiToMetaModelCodec.getOverlayedProperties();
		} catch (CodecException e1) {
			String msg = "cannot encode meta model in XMI";
			log.error( msg, e1);
			throw new ZargoConverterException(msg, e1);
		} catch (DomParserException e) {
			String msg = "cannot save xmi document to string";
			log.error( msg, e);
			throw new ZargoConverterException(msg, e);
		}
		finally {
			// close the recycle stream if set 
			if (recycleStream != null)
				try {
					recycleStream.close();
				} catch (IOException e) {
					String msg = "cannot close recycle stream";
					log.warn( msg, e);
				}
		}
		
		//	
		// no preexisting xmi -> we must create the full structure of the zargo 
		//
		if (inputStream == null) {
			// 
			try {
				//argoProvider = springLoader.getBean( "zargo.argo");
				String data = argoProvider.get();
				// modify 
				data = data.replace( "${metamodel}", metaModel.getName());
				data = data.replace( "${metamodelfilename}", modelNameAsFileName);
				ZargoEntry argoEntry = new ZargoEntry();				
				argoEntry.setBuffer( data.getBytes());
				ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + ".argo");
				zipEntry.setComment( "Automatically generated entry for .argo content");
				argoEntry.setZipEntry( zipEntry);
				zargoPayload.add( argoEntry);				
			} catch (ProviderException e) {
				String msg = "cannot get template [argo.argo] as " + e;
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
			
			try {
				//profileProvider = springLoader.getBean( "zargo.profile");
				ZargoEntry profileEntry = new ZargoEntry();
				profileEntry.setBuffer( profileProvider.get().getBytes());
				ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + ".profile");
				zipEntry.setComment( "Automatically generated entry for .profile content");
				profileEntry.setZipEntry( zipEntry);
				zargoPayload.add( profileEntry);
			} catch (ProviderException e) {
				String msg = "cannot get template [argo.profile] as " + e;
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
			
			try {
				//toDoProvider = springLoader.getBean( "zargo.todo");
				ZargoEntry todoEntry = new ZargoEntry();
				todoEntry.setBuffer( toDoProvider.get().getBytes());
				ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + ".todo");
				zipEntry.setComment( "Automatically generated entry for .todo content");
				todoEntry.setZipEntry( zipEntry);
				zargoPayload.add( todoEntry);
			} catch (ProviderException e) {
				String msg = "cannot get template [argo.todo] as " + e;
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
			
			try {
				//diagrammProvider = springLoader.getBean( "zargo.pgml");
				ZargoEntry diagrammEntry = new ZargoEntry();
				String data = diagrammProvider.get();
				data = data.replace( "${name}", modelNameAsFileName);
				data = data.replace( "${id}", modelId);
				diagrammEntry.setBuffer( data.getBytes());
				ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + "_Klassendiagramm.pgml");
				zipEntry.setComment( "Automatically generated entry for .pgml content");
				diagrammEntry.setZipEntry( zipEntry);
				zargoPayload.add( diagrammEntry);
			} catch (ProviderException e) {
				String msg = "cannot get template [argo.pgml] as " + e;
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
		
			// build the new xmi entry
			xmiEntry = new ZargoEntry();
			byte [] bytes = xmiAsString.getBytes();
			xmiEntry.setBuffer( bytes);
			ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + ".xmi");
			zipEntry.setComment( "Automatically generated .xmi content for model [" + metaModel.getName() + "]");
			zipEntry.setSize( bytes.length);
			xmiEntry.setZipEntry(zipEntry);
			zargoPayload.add( xmiEntry);
					
		} else { 
			// we already have a xmi entry, so we just replace its content
			ZipEntry zipEntry = new ZipEntry( modelNameAsFileName + ".xmi");
			zipEntry.setComment( "Automatically re-generated .xmi content for model [" + metaModel.getName() + "]");
			xmiEntry.setZipEntry(zipEntry);
			byte [] bytes = xmiAsString.getBytes();
			zipEntry.setSize( bytes.length);
			xmiEntry.setBuffer( bytes);
		}
		
		// add the overlay properties list, if any
		
		if (
				overlayedProperties != null &&
				overlayedProperties.size() > 0
			) {
			try {
				throw new UnsupportedOperationException("not yet implemented");
				/*
				Codec<List<GmProperty>, Document> codec = new GenericModelRootDomCodec<List<GmProperty>>();
				Document overlayedPropertiesAsDocument = codec.encode( overlayedProperties);
				//String overlayedPropertyAsString = DomParser.saveXmlToString(overlayedPropertiesAsDocument, false, false, null, "UTF-8");
				String overlayedPropertyAsString = DomParser.write().from( overlayedPropertiesAsDocument).setEncoding("UTF-8").to();
				ZargoEntry overlayedPropertyEntry = new ZargoEntry();
				ZipEntry zipEntry = new ZipEntry( OVERLAYED_PROPERTIES);
				zipEntry.setComment( "persisted list of overlayed GmProperties");
				overlayedPropertyEntry.setZipEntry( zipEntry);
				overlayedPropertyEntry.setBuffer( overlayedPropertyAsString.getBytes());
				zargoPayload.add( overlayedPropertyEntry);
				*/
			} catch (Exception e) {
				String msg ="cannot write list of overlayed properties";
				log.error( msg, e);
			}
		} else {
			log.info("No properties with set overlay flag found. No entry will be generated");
		}
		
	
		//
		// write modified zargo file
		//
		ZipOutputStream zipOutput = null;
		try {			
			zipOutput = new ZipOutputStream( outputStream);
			for (ZargoEntry zargoEntry : zargoPayload ) {
				zipOutput.putNextEntry( zargoEntry.getZipEntry());
				byte[] bytes = zargoEntry.getBuffer();
				zipOutput.write( bytes, 0, bytes.length);
				zipOutput.closeEntry();
			}
			
		} catch (IOException e) {
			String msg = "cannot write zipped to output stream ";
			log.error( msg, e);
			throw new ZargoConverterException(msg, e);
		} 
		finally {
			try {
				zipOutput.finish();
			} catch (IOException e) {
				String msg = "cannot close output stream";
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
			
		}
	}	

	
	
}
