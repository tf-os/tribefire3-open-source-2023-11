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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.artifact;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.stylers.ParametricStyler;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider  {

	private ParametricStyler idStyler;
	private ParametricStyler groupStyler;
	private ParametricStyler versionStyler;

	@Configurable  @Required
	public void setInitialFont(Font initialFont) {		
		idStyler = new ParametricStyler(initialFont, SWT.BOLD);
		groupStyler = new ParametricStyler(initialFont, null);
		versionStyler = new ParametricStyler(initialFont, null);
	}
	
	@Override
	public Image getImage(Object arg0) {	
		return null;
	}

	@Override
	public String getToolTipText(Object obj) {
		CompiledArtifactIdentification cai = (CompiledArtifactIdentification) obj;
		return cai.asString();
	}

	@Override
	public StyledString getStyledText(Object obj) {
		
		CompiledArtifactIdentification cai = (CompiledArtifactIdentification) obj;
		
		// solution identification	
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		String sgId = cai.getGroupId();

		int cS = sb.length();
		sb.append( sgId);
		sb.append( " ");		
		int cE = sb.length();
		sequences.add( new StyledTextSequenceData(cS, cE, groupStyler));		

		String saId = cai.getArtifactId();		
		cS = sb.length();
		sb.append( saId);
		cE = sb.length();
		sequences.add( new StyledTextSequenceData(cS, cE, idStyler));
		
		cS = sb.length();
		sb.append( " ");
		sb.append( cai.getVersion().asString());
		cE = sb.length();
		sequences.add( new StyledTextSequenceData(cS, cE, versionStyler));					
	
		
		String txt = sb.toString();					
		StyledString styledString = new StyledString(txt);
	
		StyledTextHandler.applyRanges(styledString, sequences);
	
		return styledString;
	}

	@Override
	public void update(ViewerCell arg0) {}

	@Override
	public void dispose() {
		
		groupStyler.dispose();
		idStyler.dispose();
		versionStyler.dispose();
		
		super.dispose();
	}
	
	
	

}
