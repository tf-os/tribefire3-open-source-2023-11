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
package com.braintribe.devrock.ac.container.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * helper class to load/save {@link AnalysisArtifactResolution} from and to YAML formatted files
 * 
 * @author pit
 *
 */
public class ResolutionYamlMarshaller {
	private static YamlMarshaller marshaller;
	static {
		 marshaller = new YamlMarshaller();
		 marshaller.setWritePooled( true);
	}
	

	/**
	 * load the {@link AnalysisArtifactResolution} from an YAML file 
	 * @param file - the {@link File} to load from 
	 * @return - the 
	 */
	public static AnalysisArtifactResolution fromYamlFile( File file) {
		try (InputStream in = new FileInputStream( file)) {
			return (AnalysisArtifactResolution) marshaller.unmarshall( in);
		}
		catch( Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Cannot unmarshall the resolution from the file [" + file.getAbsolutePath() + "]", e);
			ArtifactContainerPlugin.instance().log(status);
		}
		return null;
	}
	
	/**
	 * write the resolution as YAML to disk
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @param file - the {@link File} to write to 
	 */
	public static void toYamlFile( AnalysisArtifactResolution resolution, File file) {
		try (OutputStream out = new FileOutputStream( file)) {
			marshaller.marshall(out, resolution);			
		} catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Cannot marshall the resolution to the file [" + file.getAbsolutePath() + "]", e);
			ArtifactContainerPlugin.instance().log(status);
		}
	}
}
