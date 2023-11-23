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
package tribefire.extension.xml.schemed.demo;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.xml.schemed.marshaller.xml.SchemedXmlMarshaller;

public class Demo {
	private static Logger log = Logger.getLogger(Demo.class);

	private static void marshall(File input, String modelName, String xmlName) {
		
		// prepare mapping model
		StaxMarshaller staxMarshaller = new StaxMarshaller();
		GmMetaModel mappingModel;
		File modelFile = new File( input, modelName);
		try ( FileInputStream in = new FileInputStream( modelFile)) {
			mappingModel = (GmMetaModel) staxMarshaller.unmarshall(in);
		} catch (Exception e) {
			log.error( "cannot read mapping model from [" + modelFile + "]", e);
			return;
		}
		
		// get & initialize marshaller
		SchemedXmlMarshaller marshaller = new SchemedXmlMarshaller();
		marshaller.setMappingMetaModel(mappingModel);

		
		// unmarshall, aka decode
		GenericEntity assembly;
		File xmlFile = new File( input, xmlName);
	
		try ( FileInputStream in = new FileInputStream( xmlFile)) {
			assembly = (GenericEntity) marshaller.unmarshall(in);
		} catch (MarshallException e) {
			log.error("cannot unmarshall xml file [" + xmlFile + "]" + e);
			return;
		}
		catch (IOException e) {
			log.error("cannot read xml file [" + xmlFile + "]", e);
			return;
		}
		
		System.out.println( assembly.entityType().getTypeSignature());
				
		// marshall, aka encode
		File ouputDir = new File( input, "output");
		ouputDir.mkdir();
		File xmlOutputFile = new File( ouputDir, xmlName); 
				
		try (OutputStream out = new FileOutputStream(xmlOutputFile)) {
			marshaller.marshall(out, assembly);
		} catch (MarshallException e) {
			log.error("cannot marshall", e);
			return;
		}	
		catch (IOException e) {
			log.error("cannot write xml file", e);
			return;
		}
				
	}
	
	public static void main(String [] args) {
		//marshall( new File( args[0]), args[1], args[2]);
		marshall( new File( "res/Swift/pain"), "com.braintribe.fin.swift.Pain-001.001.03-mapping#1.0.xml", "pain.1.001.001.03.xml");
	}
	
	
}
