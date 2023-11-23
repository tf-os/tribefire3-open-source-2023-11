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
package tribefire.extension.xml.schemed.test.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.utils.IOTools;

import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallResponse;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallResponse;
import tribefire.extension.xml.schemed.test.commons.commons.roundtrip.RoundTripRequest;

public class Runner extends AbstractXmlMarshallerLab {
	
	public static void main( String [] args) {
		
		StaxMarshaller marshaller = new StaxMarshaller();
		File file = new File( args[0]);
		if (!file.exists()) {
			throw new IllegalStateException("file ["+ file.getAbsolutePath() + "] doesn't exist");
		}
		RoundTripRequest request;
		try (InputStream in = new FileInputStream( file)){
			request = (RoundTripRequest) marshaller.unmarshall(in);
		}
		catch ( IOException e) {
			throw new IllegalStateException("cannot read file ["+ file.getAbsolutePath() + "]");
		}
		Runner runner = new Runner();
		
		runner.run( request);
		
		
		
	}

	private void run(RoundTripRequest request) {
		File workingDirectory = new File( request.getWorkingDirectoryname());
		if (!workingDirectory.exists()) {
			throw new IllegalStateException("directory ["+ workingDirectory.getAbsolutePath() + "] doesn't exist");
		}
		List<String> xmlFileNames = request.getXmlFilenames();
		String mappingModelFileName = request.getModelFilename();
		String xsdFileName = request.getXsdFilename();

		int threshold = request.getMeasuringThreshold();
		int repeats = request.getRepeats() - threshold;
		
		//for (String xmlFileName : xmlFileNames) {
		xmlFileNames.parallelStream().forEach( xmlFileName -> {		
			process(request, workingDirectory, mappingModelFileName, xsdFileName, threshold, repeats, xmlFileName);	
		});
		
	}

	private void process(RoundTripRequest request, File workingDirectory, String mappingModelFileName, String xsdFileName, int threshold, int repeats, String xmlFileName) {
		SchemedXmlMarshallerUnmarshallRequest unmarshallRequest = buildRequest(workingDirectory, xmlFileName, mappingModelFileName);					
		SchemedXmlMarshallerUnmarshallResponse unmarshallResponse = null;
		
		System.out.print("unmarshalling [" + xmlFileName + "] to assembly with [" + repeats +"] repeats... ");
		
		long sumReads = 0;
		
		for (int i = 0; i < request.getRepeats(); i++) {
			try {
				long startUmMarshall = System.nanoTime();
				unmarshallResponse = process( unmarshallRequest);
				long endUmMarshall = System.nanoTime();
				long umMarshall = endUmMarshall-startUmMarshall;
				if (i >= threshold) {
					sumReads += umMarshall;
				}
			} catch (Exception e1) {
				throw new IllegalStateException("cannot unmarshall [" + xmlFileName +"] to assembly as " + e1);
			}
		}

		
		SchemedXmlMarshallerMarshallRequest marshallRequest = buildRequest(workingDirectory, unmarshallResponse.getAssembly(), mappingModelFileName);
		SchemedXmlMarshallerMarshallResponse marshallResponse = null;
		System.out.print("marshalling assembly to xml...with [" + repeats +"] repeats... ");
		
		long sumWrites = 0;
		for (int i = 0; i < request.getRepeats(); i++) {
			try {
				long startMarshall = System.nanoTime();
				marshallResponse = process(marshallRequest);
				long endMarshall = System.nanoTime();
				long marshall = endMarshall-startMarshall;
				if (i >= threshold) {
					sumWrites += marshall;
				}
			} catch (Exception e1) {
				throw new IllegalStateException("cannot marshall assembly to [" + xmlFileName +"] as " + e1);
			}
		}
		
		String contents = marshallResponse.getExpression();
				
		
		File writtenXml;
		try {
			writtenXml = File.createTempFile("sxxm", "xml", workingDirectory);
		} catch (IOException e1) {
			throw new IllegalStateException("cannot create tempory file in [" + workingDirectory.getAbsolutePath() +"] as " + e1);
		}
		
		try {
			IOTools.spit( writtenXml, contents, "UTF-8", false);
		} catch (IOException e) {
			throw new IllegalStateException("cannot dump expression to [" + writtenXml.getAbsolutePath() +"] as " + e);
		}
		
		
		try {
			System.out.print("validating result...");
			validate(new File( workingDirectory, xsdFileName), writtenXml);
			System.out.println("done");
		} catch (Exception e) {
			throw new IllegalStateException("marshalled data in doesn't validate by xsd as" + e);
		}
		
		if (threshold > 0) {				
			System.out.println("unmarshalling averaging [" + (sumReads / repeats)/ 1E6 + "] ms over [" + repeats + "] repeats");
			System.out.println("marshalling averaging [" + (sumWrites / repeats)/ 1E6 + "] ms over [" + repeats + "] repeats");
		}
		else {
			System.out.println("unmarshalling done in [" + sumReads / 1E6 + "]ms");
			System.out.println("marshalling done in [" + sumWrites / 1E6 + "]ms");				
		}
	}
}
