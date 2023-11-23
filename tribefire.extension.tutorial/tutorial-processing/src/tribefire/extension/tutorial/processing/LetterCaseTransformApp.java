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
package tribefire.extension.tutorial.processing;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LetterCaseTransformApp extends BasicTemplateBasedServlet implements InitializationAware {

	private static final long serialVersionUID = 5594198356463202820L;
	private static final String templateLocation = "tribefire/extension/tutorial/processing/template/transform.html.vm";
	
	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
		// TODO implement
		return null;
	}

	@Override
	public void postConstruct() {		
		setTemplateLocation(templateLocation);
	}

}
