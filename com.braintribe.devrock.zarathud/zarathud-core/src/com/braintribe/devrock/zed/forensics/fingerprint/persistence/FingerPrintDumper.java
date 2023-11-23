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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * simple dumper for finger prints 
 * 
 * @author pit
 *
 */
public class FingerPrintDumper {
	private static FingerPrintMarshaller marshaller = new FingerPrintMarshaller();

	public static void dump( File contents, Artifact terminalArtifact, Map<FingerPrint, ForensicsRating> ratedCurrentFingerPrints) {
		FingerprintOverrideContainer fpovrc = new FingerprintOverrideContainer();
		fpovrc.setFingerprintOverrideRatings(ratedCurrentFingerPrints);		
		
		String terminalName = terminalArtifact.toVersionedStringRepresentation();
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + ".fpr.txt";
		File output = new File( contents, name);
		output.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream( output)) {
			marshaller.marshall(out, fpovrc);
		}
		catch (Exception e) {
			throw new IllegalStateException("boink during safe", e);
		}
	}
	
	public static Map<FingerPrint, ForensicsRating> load( File contents, Artifact terminalArtifact) {
		String terminalName = terminalArtifact.toVersionedStringRepresentation();
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + ".fpr.txt";
		File input = new File( contents, name);
		try (InputStream in = new FileInputStream(input)) {
			FingerprintOverrideContainer fpovrc = (FingerprintOverrideContainer) marshaller.unmarshall(in);
			return fpovrc.getFingerprintOverrideRatings();
		} catch (Exception e) {
			throw new IllegalStateException("boink during load", e);
		}
	}
}
