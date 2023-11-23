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
package com.braintribe.devrock.artifactcontainer.ui.intelligence.manual;

import java.util.List;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class SolutionBiasColumnLabelProvider extends AbstractSolutionBiasLabelProvider {
	
	private Font font;
	private Image image;
	
	
	@Configurable @Required
	public void setFont(Font font) {
		this.font = font;
	}
	
	@Configurable @Required
	public void setImage(Image image) {
		this.image = image;
	}
	
	
	
	
	@Override
	public Font getFont(Object element) {
		if (getBias( element) != null)
			return font;
		return super.getFont(element);
	}



	@Override
	public Image getImage(Object element) {
		if (getBias( element) != null) {
			return image;
		}
		return super.getImage(element);
	}



	@Override
	public String getText(Object element) {
		return null;
		/*
		ArtifactBias artifactBias = getBias(element);
		if (artifactBias != null) {
			return "*";
		}
		return "";
		*/
	}

	@Override
	public String getToolTipText(Object element) {	
		ArtifactBias artifactBias = getBias(element);
		if (artifactBias == null) {
			return "no bias";
		}
		StringBuffer buffer = new StringBuffer();
		
		if (artifactBias.hasLocalBias()) {
			buffer.append( "local");
		}
		List<String> activeRepositories = artifactBias.getActiveRepositories();
		if (activeRepositories != null && activeRepositories.size() > 0) {
			if (buffer.length() > 0) 
				buffer.append(';');
			buffer.append("positive:");
			boolean first = true;
			for (String activeRepository : activeRepositories) {
				if (!first) {
					buffer.append( ",");
					first = false;
				}
				buffer.append( activeRepository);
			}
			
		}
		List<String> inactiveRepositories = artifactBias.getInactiveRepositories();
		if (inactiveRepositories != null && inactiveRepositories.size() > 0) {
			if (buffer.length() > 0) 
				buffer.append(';');
			buffer.append("negative:");
			boolean first = true;
			for (String inactiveRepository : inactiveRepositories) {
				if (!first) {
					buffer.append( ",");
					first = false;
				}
				buffer.append( inactiveRepository);
			}
		}
		String tooltip = buffer.toString();
		return tooltip;
	}
}
