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
package tribefire.extension.xml.schemed.marshaller.xsd.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.braintribe.logging.Logger;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;

import tribefire.extension.xml.schemed.marshaller.xsd.SchemedXmlXsdMarshaller;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchema;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchemata;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class BasicSchemaReferenceResolver implements SchemaReferenceResolver, ConfigurableSchemaReferenceResolver {

	private static Logger log = Logger.getLogger(BasicSchemaReferenceResolver.class);
	private Map<String, Schema> uriToSchemaMap = new HashMap<>();
	private Map<Schema, String> schemaToUriMap = new HashMap<>();
	private SchemedXmlXsdMarshaller marshaller = new SchemedXmlXsdMarshaller();
	private Map<String, Schema> shortNameToSchemaMap = new HashMap<>();
	private ReferencedSchemata referencedSchemata;
	private Resource containerResource;
	private ZipContext archive;
	
	@Override
	public String getUriOfSchema(Schema schema) {
		return schemaToUriMap.get(schema);
	}

	@Override
	public Schema getSchemaOfUri(String uri) {
		return uriToSchemaMap.get( uri);		
	}
	
	@Override
	public void setReferencedSchemata(ReferencedSchemata schemata) {
		referencedSchemata = schemata;
	}

	

	@Override
	public void setContainerResource(Resource resource) {
		containerResource = resource;
		
	}

	@Override
	public Schema resolve(String uri, Resource resource) {
		
		uri = uri.replace( '\\', '/');
		
		Schema schema = uriToSchemaMap.get( uri);
		String msg = "returning cached schema for [" + uri + "]";
		if (schema != null) {
			if (log.isDebugEnabled()) {
				log.debug(msg);
			}
			return schema;
		}
		schema = shortNameToSchemaMap.get( uri);
		if (schema != null) {
			if (log.isDebugEnabled()) {
				log.debug(msg);
			}
			return schema;
		}
		
		InputStream inputStream = resource.openStream();
		
		try {
			return resolve( uri, inputStream);
		} catch (XMLStreamException e) {
			throw  new IllegalStateException("resolve referenced schema [" + uri + "]", e);
		}
		finally {			
			try {
				inputStream.close();
			} catch (IOException e) {
				throw  new IllegalStateException("cannot close stream of [" + uri + "]", e);
			}
		}
	}


	@Override
	public Schema resolve(Schema parent, String uri) {
		
		if (log.isDebugEnabled()) {
			String msg = "Resolving [" + uri + "]";
			log.debug(msg);
		}	
		
		uri = uri.replace( '\\', '/');
		String msg = "returning cached schema for [" + uri + "]";
		Schema schema = uriToSchemaMap.get( uri);
		if (schema != null) {
			if (log.isDebugEnabled()) {
				log.debug(msg);
			}
			return schema;
		}
		
		schema = shortNameToSchemaMap.get( uri);
		if (schema != null) {
			if (log.isDebugEnabled()) {
				log.debug(msg);
			}
			return schema;
		}
				
		InputStream in = getInputStreamForUri(uri);
		
		try {
			return resolve( uri, in);
		} catch (XMLStreamException e) {
			throw  new IllegalStateException("cannot unmarshall stream of [" + uri + "]", e);
		}
		finally {			
			try {
				in.close();
			} catch (IOException e) {
				throw  new IllegalStateException("cannot close stream of [" + uri + "]", e);
			}
		}
	}
	

	/**
	 * @param uri
	 * @param inputStream
	 * @return
	 * @throws XMLStreamException
	 */
	private Schema resolve( String uri, InputStream inputStream) throws XMLStreamException {			
		Schema schema = marshaller.unmarshall( inputStream);
		if (schema == null) {
			throw  new IllegalStateException("unmarshalling the stream of [" + uri + "] returns no schema");
		}
		uriToSchemaMap.put(uri, schema);
		schemaToUriMap.put(schema, uri);
		
		int i = uri.lastIndexOf( '/');
		String shortName = uri.substring(i+1);
		shortNameToSchemaMap.put(shortName, schema);
		
		return schema;				
	}
	
	/**
	 * @param uri
	 * @return
	 */
	private InputStream getInputStreamForUri( String uri) {		
		if (referencedSchemata != null) {
			for (ReferencedSchema rSchema : referencedSchemata.getReferencedSchemata()) {
				if (rSchema.getUri().equalsIgnoreCase(uri)) {
					return rSchema.getSchema().openStream();
				}
			}
		}
		if (containerResource != null) {
			if (archive == null) {
				try {
					archive = Archives.zip().from( containerResource.openStream());
				} catch (ArchivesException e) {
					throw new IllegalStateException("cannot extract zip context from container resource", e);
				}
			}
			// correct URI (drop all relative paths)
			ZipContextEntry entry = archive.getEntry(uri);
			if (entry == null) {
				entry = getMatchingEntry(archive, uri);
				if (entry == null) {
					throw  new IllegalStateException("cannot open stream to [" + uri + "] as it's not part of the archive resource");
				}
			}
			return entry.getPayload();			
		}
		try {
			URL url = new URL( uri);
			try {
				return url.openStream();			
			} catch (IOException e) {
				throw  new IllegalStateException("cannot open stream to [" + uri + "]", e);
			}
		} catch (MalformedURLException e) {			
			throw  new IllegalStateException("cannot open stream to [" + uri + "]", e);			
		}
	}

	private ZipContextEntry getMatchingEntry( ZipContext archive, String uri) {
		int p = uri.lastIndexOf( '/');
		String key = uri.substring(p+1);
		List<ZipContextEntry> entries = archive.getEntries( ".*" + key);
		if (entries == null || entries.size() == 0)
			return null;
		
		return entries.get(0);
	}

	
}
