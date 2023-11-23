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
package com.braintribe.devrock.zed.forensics.fingerprint.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map.Entry;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.register.FingerPrintRegistry;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * marshalls the {@link FingerPrintRegistry}
 * 
 * @author pit
 *
 */
public class FingerPrintMarshaller implements Marshaller {
	
	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		try (Writer wr = new OutputStreamWriter(out);) {
			if (value instanceof FingerprintOverrideContainer == false) {
				throw new MarshallException("only [" + FingerprintOverrideContainer.class.getName() + "] supported here");
			}
			FingerprintOverrideContainer fpr = (FingerprintOverrideContainer) value;
			boolean first = true;
			for (Entry<FingerPrint, ForensicsRating> entry : fpr.getFingerprintOverrideRatings().entrySet()) {																
				String fps = FingerPrintExpert.toString(entry.getKey()) + "=" + entry.getValue().toString().toLowerCase();
				
				if (!first) {
					wr.write( "\n");
				}
				else {
					first = false;
				}
				wr.write(fps);
			}
		} catch (IOException e) {
			throw new MarshallException("cannot open stream to write to", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		FingerprintOverrideContainer fpr = new FingerprintOverrideContainer();
		try (BufferedReader br = new BufferedReader( new InputStreamReader( in));) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String expression = line.trim();
		    	if (expression.startsWith("#"))
		    		continue;
		    	int p = expression.indexOf('=');
		    	// no '=' no valid line 
		    	if (p <= 0) {
		    		continue;
		    	}
		    	String fps = expression.substring(0, p);
		    	
		    	String frs = expression.substring( p+1).toUpperCase();
				FingerPrint fp = FingerPrintExpert.build(fps);
				
		    	fpr.getFingerprintOverrideRatings().put( fp,  ForensicsRating.valueOf(frs));		    			    	
		    }
		} catch (IOException e) {
			throw new MarshallException("cannot read from input stream", e);
		}
		catch (IllegalArgumentException e) {
			throw new MarshallException("invalid code for either ForensicsFindingCode or ForensicRating passed", e);
		}
		return fpr;
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		marshall(out, value);
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(in);
	}

	
	

}
