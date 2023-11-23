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
package com.braintribe.model.processing.wopi.app;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.wopi.WopiConnectorUtil;
import com.braintribe.model.processing.wopi.WopiQueryingUtil;
import com.braintribe.model.processing.wopi.service.AbstractWopiProcessing;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.html.HtmlTools;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;

/**
 * Expert for WopiIntegrationExample
 * 
 * 
 * 
 * <br/>
 * <b>Important:</b> make sure to use the actual IP address and not 'localhost' for accessing the example
 * 
 * @see <a href= "https://wopi.readthedocs.io/en/latest/hostpage.html">Building a host page</a>
 * 
 *
 */
public class WopiIntegrationExample extends BasicTemplateBasedServlet implements AbstractWopiProcessing {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(WopiIntegrationExample.class);

	private static final String templateLocation = "com/braintribe/model/processing/wopi/app/template/wopiIntegrationExample.html.vm";

	private Supplier<PersistenceGmSession> sessionSupplier;

	private com.braintribe.model.wopi.service.WopiIntegrationExample deployable;

	// -----------------------------------------------------------------------
	// INITIALIZATION
	// -----------------------------------------------------------------------

	@Override
	public void init() throws ServletException {
		super.init();
		setTemplateLocation(templateLocation);
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {

		String correlationId = request.getParameter("correlationId");
		String title = request.getParameter("title");
		if (CommonTools.isNull(title)) {
			title = "";
		}

		PersistenceGmSession session = sessionSupplier.get();

		String wopiUrl;
		WopiSession wopiSession;

		String demoCorrelationId = resolveDefaultContentCorrelationId(AbstractWopiProcessing.DEMO_CORRELATION_ID_PREFIX,
				AbstractWopiProcessing.DEMO_CORRELATION_ID_POSTFIX, "demo.docx", DocumentMode.edit);
		if (StringTools.isBlank(correlationId)) {
			wopiSession = WopiQueryingUtil.queryWopiSession(session, demoCorrelationId);
		} else {
			wopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);
		}
		if (wopiSession == null) {
			throw new IllegalStateException("Neither a correlationId was provided nor a docx demo edit WopiSession (with correlationId: '"
					+ demoCorrelationId
					+ "') exists for demonstration. Either provide a correlationId as URL parameter or initialized the default demo WOPI sessions");
		}

		wopiUrl = wopiSession.getWopiUrl();

		// just filling here with dummy values
		String accessToken = "myAccessToken";
		String accessTokenTtl = Long.toString(deployable.getWopiApp().getAccessTokenTtlInSec());

		String servicesUrl = WopiConnectorUtil.getPublicServicesUrl(deployable.getWopiApp().getWopiWacConnector().getCustomPublicServicesUrl());
		String status;
		try {
			URL url = new URL(servicesUrl + "/healthz");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(deployable.getHealthConnectTimeoutInMs());
			con.setReadTimeout(deployable.getHealthReadTimeoutInMs());
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				status = "OKAY - " + responseCode;
			} else {
				status = "NOT OKAY - " + responseCode;
			}
		} catch (Exception e) {
			status = "ERROR - " + e.getMessage();
		}

		// invalid accessToken
		// sessionId
		// TODO: the session probably should not be added here
		wopiUrl = wopiUrl + "&sessionId=" + session.getSessionAuthorization().getSessionId();

		wopiUrl = servicesUrl + wopiUrl;

		VelocityContext context = new VelocityContext();
		context.put("wopiUrl", wopiUrl);
		context.put("accessToken", accessToken);
		context.put("accessTokenTtl", accessTokenTtl);
		context.put("title", title);
		context.put("status", status);
		context.put("HtmlTools", HtmlTools.class);
		context.put("StringTools", StringTools.class);
		return context;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}

	@Configurable
	@Required
	public void setDeployable(com.braintribe.model.wopi.service.WopiIntegrationExample deployable) {
		this.deployable = deployable;
	}

}
