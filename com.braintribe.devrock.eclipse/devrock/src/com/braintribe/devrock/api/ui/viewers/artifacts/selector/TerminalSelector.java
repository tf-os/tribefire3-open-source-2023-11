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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.graphics.Font;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public interface TerminalSelector {

	void setInitialIdentifications( List<VersionedArtifactIdentification> vais);	
	Maybe<List<CompiledTerminal>> getSelectedTerminals();
	
	void setBigFont(Font bigFont);	
	void setBaseFont(Font initialFont);
	
	void setValidEntryConsumer( Consumer<Boolean> validEntryConsumer);
	
	void dispose();
}
