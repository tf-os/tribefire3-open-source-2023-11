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
package tribefire.platform.impl.userimage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.web.servlet.auth.providers.ResourceFromIconProvider;

public class UserImageServlet extends HttpServlet {

	private final Logger logger = Logger.getLogger(UserImageServlet.class);

	private static final long serialVersionUID = 1L;

	private PersistenceGmSessionFactory sessionFactory;
	private String defaultUserIconPath = "webpages/logo-user-default.png";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		User sessionUser = acquireUser(request);

		Icon userIcon = null;
		if (sessionUser != null) {
			userIcon = sessionUser.getPicture();
		}

		boolean resourceStreamed = false;
		if (userIcon != null) {

			Resource userResource = ResourceFromIconProvider.getImageFromIcon(userIcon, 4, Integer.MAX_VALUE);

			if (userResource != null) {
				logger.trace(() -> "Streaming back the user image of type " + userResource.getMimeType());

				long cacheAge = Numbers.SECONDS_PER_HOUR;
				long expiry = new Date().getTime() + (cacheAge * Numbers.MILLISECONDS_PER_SECOND);
				response.setDateHeader("Expires", expiry);
				response.setHeader("Cache-Control", "max-age=" + cacheAge);
				response.setContentType(userResource.getMimeType());

				try (InputStream in = new BufferedInputStream(userResource.openStream())) {
					ServletOutputStream outputStream = response.getOutputStream();
					resourceStreamed = true;
					IOTools.pump(in, outputStream);
				}
			} else {
				logger.trace(() -> "No user resource could be found.");
			}
		}
		if (!resourceStreamed) {
			logger.trace(() -> "Providing a fallback image.");

			String requestURI = request.getRequestURI();
			String redirectURL = defaultUserIconPath;
			if (requestURI.endsWith("/")) {
				redirectURL = "../" + defaultUserIconPath;
			}
			response.sendRedirect(redirectURL);

		}
	}

	protected User acquireUser(HttpServletRequest request) {

		String username = request.getParameter("name");
		if (!StringTools.isBlank(username)) {
			// @formatter:off
			EntityQuery userQuery = 
					EntityQueryBuilder
					.from(User.T)
					.where()
					.property(User.name).eq(username)
					.done();
			// @formatter:on

			PersistenceGmSession authSession = sessionFactory.newSession("auth");

			User user = authSession.query().entities(userQuery).first();
			return user;
		}

		User sessionUser = null;
		try {
			UserSession session = AttributeContexts.peek().findOrNull(UserSessionAspect.class);

			if (session != null) {
				logger.trace(() -> "Found a user session in the current request object.");

				// @formatter:off
				EntityQuery userQuery = 
						EntityQueryBuilder
						.from(User.T)
						.where()
						.property(User.name).eq(session.getUser().getName())
						.done();
				// @formatter:on

				PersistenceGmSession authSession = sessionFactory.newSession("auth");

				sessionUser = authSession.query().entities(userQuery).first();

			} else {
				logger.debug(() -> "The current request does not contain a UserSession object.");
			}

			// the User from the UserSession might not exist in the auth access (e.g.: custom AuthenticationService(s))
			if (sessionUser == null) {
				sessionUser = session.getUser();
			}

		} catch (Exception e) {
			logger.debug(() -> "Error while trying to get the user object.", e);
		}
		return sessionUser;
	}

	@Configurable
	public void setDefaultUserIconUrl(String defaultUserIconUrl) {
		this.defaultUserIconPath = defaultUserIconUrl;
	}
	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
