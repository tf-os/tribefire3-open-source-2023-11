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
package com.braintribe.devrock.artifactcontainer.quickImport;

import java.io.File;
import java.util.List;

import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultBroadcaster;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceArtifact;

public interface QuickImportControl extends QuickImportScanResultBroadcaster{

	boolean isScanActive();
	void rescan();
	void rescan( SourceRepositoryPairing parring);
	void stop();
	void setup();
	List<SourceArtifact> runCoarseSourceArtifactQuery( String expression);
	List<SourceArtifact> runSourceArtifactQuery( String expression);
	List<SourceArtifact> runPartialSourceArtifactQuery( String expression);
	List<SourceArtifact> runPomFileToSourceArtifactQuery( File pomFile);
}
