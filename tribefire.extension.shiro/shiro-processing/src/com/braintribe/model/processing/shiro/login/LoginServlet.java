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
package com.braintribe.model.processing.shiro.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.velocity.VelocityContext;
import org.pac4j.core.profile.UserProfile;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.SessionFactoryBasedSessionProvider;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.processing.shiro.bootstrapping.NewUserRoleProvider;
import com.braintribe.model.processing.shiro.util.ExternalIconUrlHelper;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.shiro.deployment.FieldEncoding;
import com.braintribe.model.shiro.deployment.HasRolesField;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroClient;
import com.braintribe.model.shiro.deployment.client.ShiroInstagramOAuth20Client;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.util.servlet.remote.DefaultRemoteClientAddressResolver;
import com.braintribe.util.servlet.remote.RemoteAddressInformation;
import com.braintribe.util.servlet.remote.RemoteClientAddressResolver;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.stream.MemoryThresholdBuffer;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;
import com.braintribe.web.servlet.auth.Constants;
import com.braintribe.web.servlet.auth.CookieHandler;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import io.buji.pac4j.subject.Pac4jPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class LoginServlet extends BasicTemplateBasedServlet {

	private static final long serialVersionUID = -1L;

	private static Logger logger = Logger.getLogger(LoginServlet.class);

	public final static String TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED = "TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED";

	private static final Pattern PATTERN_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\{.*?\\}");

	private static final String signinPageTemplateLocation = "com/braintribe/model/processing/shiro/templates/login.html.vm";

	private Codec<Map<String, String>, String> urlParamCodec;

	private RemoteClientAddressResolver remoteAddressResolver;
	private String servicesUrl = "/tribefire-services";
	private boolean addSessionUrlParameter = false;

	private PersistenceGmSessionFactory sessionFactory;
	private ShiroAuthenticationConfiguration configuration;
	private String externalId;

	private HttpClientProvider httpClientProvider;

	private NewUserRoleProvider newUserRoleProvider;

	private boolean createUsers = false;
	private Set<String> userAcceptList;
	private Set<String> userBlockList;

	private boolean showStandardLoginForm = true;
	private boolean showTextLinks = false;
	private String staticImagesRelativePath;
	private String pathIdentifier = ShiroConstants.PATH_IDENTIFIER;

	private Supplier<String> authAccessIdSupplier;

	private CookieHandler cookieHandler;

	private Evaluator<ServiceRequest> evaluator;

	private ExternalIconUrlHelper externalIconUrlHelper;

	private final ConcurrentHashMap<String, String> iconContentCache = new ConcurrentHashMap<>();

	private Boolean obfuscateLogOutput = Boolean.TRUE;

	@Override
	public void init() throws ServletException {
		setTemplateLocation(signinPageTemplateLocation);
		super.init();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// resp.setHeader("Cache-Control", "no-cache, no-store");
		resp.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
		resp.setDateHeader("Last-Modified", new Date().getTime());
		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		resp.setHeader("Pragma", "no-cache");

		String showLogin = req.getParameter("showLogin");
		if (showLogin != null && showLogin.equalsIgnoreCase("true")) {
			logger.debug(() -> "The showLogin parameter is true. Hence, showing the login dialog and don't do any further action.");
			super.service(req, resp);
			return;
		}

		boolean authenticated = false;

		Subject user = null;
		try {
			user = SecurityUtils.getSubject();
		} catch (UnavailableSecurityManagerException nosm) {
			// Ignore
		}
		if (user != null) {

			logger.debug("Subject: " + user);

			PrincipalCollection principals = user.getPrincipals();
			if (principals != null && !principals.isEmpty()) {

				logger.debug(() -> "At least one principal found.");

				Iterator<Object> iterator = principals.iterator();

				while (iterator != null && iterator.hasNext()) {

					Object principal = iterator.next();
					logger.debug(() -> "Inspecting principal: " + principal);

					if (principal instanceof Pac4jPrincipal) {

						logger.debug(() -> "Principal is of the expected type.");

						Pac4jPrincipal pp = (Pac4jPrincipal) principal;
						UserProfile cp = pp.getProfile();
						Map<String, Object> attributeMap = getAttributeMap(cp);

						final String username;
						ShiroClient shiroClient = getShiroClient(cp);
						if (shiroClient != null) {
							username = getUsername(shiroClient, cp, attributeMap);
						} else {
							username = null;
						}

						logPrincipal(cp, attributeMap, username, shiroClient);

						// We do not to use Shiro to maintain the user session; this is our turf
						SecurityUtils.getSecurityManager().logout(user);

						if (username != null && acceptUsername(username) && ensureUser(username, shiroClient, cp, attributeMap)) {

							logger.debug(() -> "Authenticating user: " + username);

							initializeContext(req);
							try {

								OpenUserSession authRequest = createOpenUserSessionRequest(username, req);

								enrichOpenUserSessionRequest(authRequest, shiroClient, attributeMap);

								UserSession session = null;
								try {

									// Do authentication.
									session = authenticate(resp, authRequest);
									if (session == null) {
										logger.debug("Authentication of user " + username
												+ " failed. Please check whether the Cartridge has the 'tf-admin' role.");
										return; // Response message is already handled in authenticate.
									}

									final String sessionId = session.getSessionId();
									logger.debug(() -> "Successfully authenticated user: " + username + " with session: " + sessionId);

									String continuePath = acquireContinuePath(req);
									String redirectUrl = continuePath;

									cookieHandler.ensureCookie(req, resp, sessionId);

									if (addSessionUrlParameter) {
										redirectUrl = handleUrl(sessionId, continuePath);
									}

									deleteShiroSessionCookies(req, resp);

									String requestUri = req.getRequestURI();
									if (requestUri != null && requestUri.toLowerCase().endsWith("/sessionid")) {

										returnSessionId(resp, sessionId);

									} else {

										// redirect to next side.
										resp.sendRedirect(redirectUrl);
									}

									authenticated = true;

								} catch (Exception e) {
									throw new RuntimeException("Error authenticating to tribefire.", e);
								}
							} finally {
								popContext();
							}

						} else {
							logger.debug(() -> "User " + username + " is either not null or does not exist in the 'auth' Access.");
						}
					} else {
						logger.debug(() -> "Principal " + principal + " is not of the expected type.");
					}
				} // Iterator over all principals
			} else {
				logger.debug(() -> "No principals found");
			}
		} else {
			logger.debug(() -> "No subject found.");
		}

		if (!authenticated) {

			deleteShiroSessionCookies(req, resp);

			String continuePath = configuration.getUnauthenticatedUrl();
			if (!StringTools.isBlank(continuePath)) {
				logger.debug(() -> "No user has been authenticated. Redirecting to " + continuePath);
				resp.sendRedirect(continuePath);
			} else {
				logger.debug(() -> "No user has been authenticated. Showing the login page.");
				super.service(req, resp);
			}
		} else {
			logger.debug(() -> "A user has been authenticated. Hence, a message has been sent to the client already at this point.");
		}
	}

	private void logPrincipal(UserProfile cp, Map<String, Object> attributeMap, final String username, ShiroClient shiroClient) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Authenticated principal received: \n");
			sb.append("Id: " + cp.getId() + "\n");
			sb.append("Linked Id: " + cp.getLinkedId() + "\n");
			sb.append("Username: " + username + "\n");
			sb.append("Client: " + (shiroClient != null ? shiroClient.getName() : "No Client detected.") + "\n");
			for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (obfuscateLogOutput) {
					if (value != null && (key.equalsIgnoreCase("id_token") || key.equalsIgnoreCase("access_token"))) {
						value = StringTools.simpleObfuscatePassword(value.toString());
					}
				}
				sb.append(key + "=" + value + "\n");
			}
			logger.debug("\n" + StringTools.asciiBoxMessage(sb.toString(), -1));
		}
	}

	protected boolean acceptUsername(String username) {
		if ((userAcceptList == null || userAcceptList.isEmpty()) && (userBlockList == null || userBlockList.isEmpty())) {
			return true;
		}
		if (username == null) {
			return false;
		}
		// If there is no accept list, we just check the block list
		// If there IS an accept list, the user has to be accepted by this list
		boolean accepted = userAcceptList == null || userAcceptList.isEmpty();
		if (userAcceptList != null) {
			for (String wl : userAcceptList) {
				if (wl != null) {
					if (username.matches(wl) || username.equals(wl)) {
						accepted = true;
						break;
					}
				}
			}
		}
		if (!accepted) {
			logger.debug(() -> "The user " + username + " is not accepted by the accept-list.");
			return false;
		}
		if (userBlockList != null) {
			for (String bl : userBlockList) {
				if (bl != null) {
					if (username.matches(bl) || username.equals(bl)) {
						accepted = false;
						break;
					}
				}
			}
		}
		if (!accepted) {
			logger.debug(() -> "The user " + username + " is not accepted by the blocklist.");
		}
		return accepted;
	}

	private ShiroClient getShiroClient(UserProfile cp) {
		String clientName = cp.getClientName();
		if (!StringTools.isBlank(clientName)) {
			for (ShiroClient client : configuration.getClients()) {
				if (client.getName() != null && client.getName().equals(clientName)) {
					logger.debug(() -> "For the client name " + clientName + " we identified the client: " + client);
					return client;
				}
			}
		}
		logger.debug(() -> "Could not find a client for name " + clientName);
		return null;
	}

	private void returnSessionId(HttpServletResponse resp, String sessionId) {
		resp.setContentType("text/plain");
		try {
			resp.getWriter().write(sessionId);
		} catch (IOException e) {
			logger.error("Could not send session id", e);
		}
	}

	private void deleteShiroSessionCookies(HttpServletRequest req, HttpServletResponse resp) {
		Pair<String, String> domainAndPath = null;
		Cookie[] cookies = req.getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies) {
				Cookie rmCookie = null;

				String cookieName = cookie.getName();
				if (cookieName.equalsIgnoreCase("JSESSIONID")) {
					rmCookie = new Cookie(cookieName, "");
					if (domainAndPath == null) {
						domainAndPath = getDomainAndPathFromPublicServicesUrl();
					}
					rmCookie.setPath(domainAndPath.second);
				} else if (cookieName.equalsIgnoreCase("pac4jCsrfToken")) {
					rmCookie = new Cookie(cookieName, "");
					rmCookie.setPath("/");
				}

				if (rmCookie != null) {
					if (domainAndPath == null) {
						domainAndPath = getDomainAndPathFromPublicServicesUrl();
					}
					rmCookie.setDomain(domainAndPath.first);
					rmCookie.setMaxAge(0);
					resp.addCookie(rmCookie);
				}

			}
	}

	private Pair<String, String> getDomainAndPathFromPublicServicesUrl() {
		final String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();
		if (!StringTools.isBlank(publicServicesUrl)) {
			try {
				URL url = new URI(publicServicesUrl).toURL();
				String host = url.getHost();
				String path = url.getPath();
				return new Pair<>(host, path);
			} catch (Exception e) {
				logger.trace(() -> "Could not get domain name from public services URL " + publicServicesUrl, e);
			}
		}
		return new Pair<>("", "/tribefire-services");
	}

	private boolean ensureUser(String username, ShiroClient shiroClient, UserProfile cp, Map<String, Object> attributeMap) {

		PersistenceGmSession authSession = authSession();
		EntityQuery query = EntityQueryBuilder.from(User.T).where().property(User.name).eq(username).done();
		User existingUser = authSession.query().entities(query).first();
		if (existingUser == null) {

			if (!createUsers) {
				logger.debug(() -> "User " + username + " does not exist. The configuration is set to NOT create a new user on the fly.");
				return false;
			}

			logger.debug(() -> "User " + username + " does not yet exist but we will create it now.");

			User newUser = authSession.create(User.T);
			newUser.setName(username);
			enrichUser(authSession, shiroClient, newUser, cp, attributeMap);

			handleUserRoles(authSession, shiroClient, newUser, cp, attributeMap, true);

		} else {

			checkExistingUserUpdates(authSession, shiroClient, existingUser, cp, attributeMap);

			handleUserRoles(authSession, shiroClient, existingUser, cp, attributeMap, false);

		}

		authSession.commit();

		return true;
	}

	private void handleUserRoles(PersistenceGmSession authSession, ShiroClient shiroClient, User user, UserProfile cp,
			Map<String, Object> attributeMap, boolean isNewUser) {

		logger.debug(() -> "Starting to handle roles for user " + user.getName());

		HasRolesField rolesProvider = (shiroClient instanceof HasRolesField) ? (HasRolesField) shiroClient : null;
		if (rolesProvider != null) {
			logger.debug(() -> "The client " + shiroClient.getName() + " is a roles provider.");

			Set<String> roles = new HashSet<>();

			List<String> externalRoles = getRolesFromExternal(rolesProvider, attributeMap);
			if (externalRoles != null && !externalRoles.isEmpty()) {
				logger.debug(() -> "The authentication of the user resulted in these roles from the external system: " + externalRoles);
				roles.addAll(externalRoles);
			} else {
				logger.debug(() -> "The authentication of the user resulted in NO roles.");
			}

			boolean exclusive = rolesProvider.getExclusiveRoleProvider() != null ? rolesProvider.getExclusiveRoleProvider() : false;
			logger.debug(() -> "The role provider (client) is set to be the only source of roles: " + exclusive + "; is it a new user: " + isNewUser);

			if (!exclusive && isNewUser) {
				Set<String> internallyProvidedRoles = newUserRoleProvider != null ? newUserRoleProvider.apply(user) : null;
				if (internallyProvidedRoles != null && !internallyProvidedRoles.isEmpty()) {
					logger.debug(() -> "The internal role provider for new users returned: " + internallyProvidedRoles);
					roles.addAll(internallyProvidedRoles);
				}
			}

			if (exclusive) {
				logger.debug(() -> "Resetting the roles of the user " + user.getName() + " because the provider (" + shiroClient.getName()
						+ ") is set to be the exclusive source.");
				user.getRoles().clear();
			} else {
				logger.debug(() -> "Keeping the existing roles of the user " + user.getName() + " because the provider (" + shiroClient.getName()
						+ ") is not set to be the exclusive source.");
			}
			if (externalRoles != null && !externalRoles.isEmpty()) {
				logger.debug(() -> "Got roles " + roles + " from the providers.");
				ensureRoles(authSession, user, roles);
			}

		} else {
			logger.debug(() -> "The client " + shiroClient.getName() + " is not a roles provider. Is it a new user: " + isNewUser);

			if (isNewUser) {
				Set<String> roles = newUserRoleProvider != null ? newUserRoleProvider.apply(user) : null;
				if (roles != null && !roles.isEmpty()) {
					logger.debug(() -> "Got roles " + roles + " from the provider.");
					ensureRoles(authSession, user, roles);
				} else {
					logger.debug(() -> "Got no roles from provider: " + newUserRoleProvider);
				}
			}
		}

	}

	private void ensureRoles(PersistenceGmSession authSession, User user, Collection<String> roles) {
		if (roles == null || roles.isEmpty()) {
			return;
		}
		Set<String> remainingRoles = new HashSet<>(roles);

		EntityQuery roleQuery = EntityQueryBuilder.from(Role.T).where().property(Role.name).in(remainingRoles).done();
		List<Role> roleList = authSession.query().entities(roleQuery).list();
		if (roleList != null) {
			for (Role role : roleList) {
				user.getRoles().add(role);
				remainingRoles.remove(role.getName());
			}
		}
		for (String newRole : remainingRoles) {
			Role role = authSession.create(Role.T);
			role.setName(newRole);
			user.getRoles().add(role);
		}
		authSession.commit();
	}

	private List<String> getRolesFromExternal(HasRolesField rolesProvider, Map<String, Object> attributeMap) {

		return getRolesFromExternal(rolesProvider.getRolesField(), rolesProvider.getRolesFieldEncoding(), attributeMap);

	}

	protected List<String> getRolesFromExternal(String pattern, FieldEncoding fieldEncoding, Map<String, Object> attributeMap) {

		List<String> allRolesCombined = new ArrayList<>();

		if (!StringTools.isBlank(pattern)) {
			logger.debug(() -> "Trying to find " + pattern + " in attribute map.");

			String[] components = pattern.split(",");

			for (String part : components) {

				part = part.trim();

				logger.debug("Trying to find part " + part + " in attribute map.");

				List<String> rolesCollection = null;

				Object rolesObject = attributeMap.get(part);
				if (rolesObject instanceof List) {
					// We got it directly from a claim (within a token)
					rolesCollection = (List<String>) rolesObject;
					logger.debug("Found list " + part + " in attribute map: " + rolesCollection);
					allRolesCombined.addAll(rolesCollection);
				} else {

					if (rolesObject instanceof String) {
						String rolesString = (String) rolesObject;
						rolesCollection = parseCollection(rolesString, fieldEncoding);
						logger.debug("Found plain " + part + " in attribute map: " + rolesCollection);

					} else {
						rolesCollection = readRolesWithPattern(part, fieldEncoding, attributeMap);
						logger.debug("Found pattern " + part + " in attribute map: " + rolesCollection);
					}
				}
				if (rolesCollection != null) {
					allRolesCombined.addAll(rolesCollection);
				}

			}

		}
		return allRolesCombined;
	}

	protected List<String> readRolesWithPattern(final String pattern, FieldEncoding fieldEncoding, final Map<String, Object> attributeMap) {
		int start = pattern.indexOf("{");
		int stop = pattern.indexOf("}", start + 1);
		if (start == -1 || stop == -1) {
			logger.debug(() -> pattern + " does not seem to be a pattern");
			return Collections.EMPTY_LIST;
		}
		String prefix = pattern.substring(0, start);
		String postFix = pattern.substring(stop + 1);
		String rawPattern = pattern.substring(start, stop + 1);

		String rolesString = StringTools.patternFormat(rawPattern, attributeMap, "");
		List<String> rolesCollection = null;
		if (!StringTools.isBlank(rolesString)) {
			rolesCollection = parseCollection(rolesString, fieldEncoding);
		}
		if (prefix.length() > 0 && rolesCollection != null) {
			rolesCollection = rolesCollection.stream().map(r -> prefix + r).collect(Collectors.toList());
		}
		if (postFix.length() > 0 && rolesCollection != null) {
			rolesCollection = rolesCollection.stream().map(r -> r + postFix).collect(Collectors.toList());
		}
		if (rolesCollection == null) {
			return Collections.EMPTY_LIST;
		}
		return rolesCollection;
	}

	private void enrichUser(PersistenceGmSession session, ShiroClient shiroClient, User user, UserProfile cp, Map<String, Object> attributeMap) {

		String pattern = shiroClient.getUserMailField();
		if (!StringTools.isBlank(pattern)) {
			user.setEmail(StringTools.patternFormat(pattern, attributeMap));
		}
		pattern = shiroClient.getUserDescriptionPattern();
		if (!StringTools.isBlank(pattern)) {
			user.setDescription(I18nTools.createLs(session, StringTools.patternFormat(pattern, attributeMap)));
		}
		pattern = shiroClient.getFirstNamePattern();
		if (!StringTools.isBlank(pattern)) {
			user.setFirstName(StringTools.patternFormat(pattern, attributeMap));
		}
		pattern = shiroClient.getLastNamePattern();
		if (!StringTools.isBlank(pattern)) {
			user.setLastName(StringTools.patternFormat(pattern, attributeMap));
		}

		String userIconUrl = getUserIconUrl(shiroClient, user, attributeMap);

		if (!StringTools.isBlank(userIconUrl)) {

			String url = StringTools.patternFormat(userIconUrl, attributeMap);
			if (!StringTools.isBlank(url)) {

				String extension = FileTools.getExtension(url);
				if (StringTools.isBlank(extension)) {
					// We have to assume something
					extension = "jpg";
				} else if (extension.length() < 3) {
					extension = "jpg";
				}

				CloseableHttpClient httpClient = null;
				CloseableHttpResponse response = null;
				try {
					httpClient = httpClientProvider.provideHttpClient();
					HttpGet get = new HttpGet(url);
					response = httpClient.execute(get);
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						logger.debug("Got a non-200 response from " + url + ": " + response);
					} else {
						Resource uploadedResource = null;
						try (InputStream is = new ResponseEntityInputStream(response)) {
							String normalizedUserName = FileTools.normalizeFilename(user.getName(), '_');
							uploadedResource = session.resources().create().name("image-" + normalizedUserName + "." + extension).store(is);
						}

						SimpleIcon picture = session.create(SimpleIcon.T);
						picture.setImage(uploadedResource);
						user.setPicture(picture);

					}
				} catch (Exception e) {
					logger.error("Could not download user icon at: " + url, e);
				} finally {
					IOTools.closeCloseable(response, logger);
					IOTools.closeCloseable(httpClient, logger);
				}
			}
		}
	}

	protected String getUserIconUrl(ShiroClient shiroClient, User user, Map<String, Object> map) {
		String iconUrl = shiroClient.getUserIconUrl();
		if (!StringTools.isBlank(iconUrl)) {
			return iconUrl;
		}

		if (shiroClient instanceof ShiroInstagramOAuth20Client) {
			iconUrl = externalIconUrlHelper.getIconUrlFromInstagram((ShiroInstagramOAuth20Client) shiroClient, map);
		}
		return iconUrl;
	}

	private List<String> parseCollection(String spec, FieldEncoding fieldEncoding) {
		if (StringTools.isBlank(spec)) {
			return null;
		}
		if (fieldEncoding == null) {
			fieldEncoding = FieldEncoding.PLAIN;
		}
		switch (fieldEncoding) {
			case CSV:
				if (spec.startsWith("[") && spec.endsWith("]")) {
					spec = StringTools.removeFirstAndLastCharacter(spec);
				}
				String[] rolesStrings = StringTools.splitCommaSeparatedString(spec, true);
				return CollectionTools2.asLinkedList(rolesStrings);
			case JSON:
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONArray array;
				try {
					List<String> result = new ArrayList<>();
					array = (JSONArray) parser.parse(spec);
					for (int i = 0; i < array.size(); ++i) {
						String entry = (String) array.get(i);
						result.add(entry);
					}
					return result;
				} catch (ParseException e) {
					logger.warn("Error while trying to parse " + spec + " as a JSON structure.", e);
				}
				break;
			case PLAIN:
				return CollectionTools2.asLinkedList(spec);
			default:
				break;

		}
		return null;
	}

	private static void setStringIfUpdated(Consumer<String> toSet, Supplier<String> oldValueSupplier, String newValue) {
		if (newValue != null) {
			String oldValue = oldValueSupplier.get();
			if (oldValue == null || !newValue.equals(oldValue)) {
				toSet.accept(newValue);
			}
		}
	}

	private void checkExistingUserUpdates(PersistenceGmSession session, ShiroClient shiroClient, User user, UserProfile cp,
			Map<String, Object> attributeMap) {

		String pattern = shiroClient.getUserMailField();
		if (!StringTools.isBlank(pattern)) {
			String newEmail = StringTools.patternFormat(pattern, attributeMap);
			setStringIfUpdated(user::setEmail, user::getEmail, newEmail);
		}

		pattern = shiroClient.getUserDescriptionPattern();
		if (!StringTools.isBlank(pattern)) {
			LocalizedString newLocalizedString = I18nTools.createLs(session, StringTools.patternFormat(pattern, attributeMap));
			if (newLocalizedString != null && user.getDescription() == null) {
				user.setDescription(newLocalizedString);
			}
		}

		pattern = shiroClient.getFirstNamePattern();
		if (!StringTools.isBlank(pattern)) {
			String newFirstName = StringTools.patternFormat(pattern, attributeMap);
			setStringIfUpdated(user::setFirstName, user::getFirstName, newFirstName);
		}
		pattern = shiroClient.getLastNamePattern();
		if (!StringTools.isBlank(pattern)) {
			String newLastName = StringTools.patternFormat(pattern, attributeMap);
			setStringIfUpdated(user::setLastName, user::getLastName, newLastName);
		}

		Icon oldPicture = user.getPicture();
		String oldMd5 = null;
		if (oldPicture instanceof SimpleIcon) {
			SimpleIcon si = (SimpleIcon) oldPicture;
			Resource resource = si.getImage();
			if (resource != null) {
				oldMd5 = resource.getMd5();
			}
		}

		pattern = shiroClient.getUserIconUrl();
		if (!StringTools.isBlank(pattern)) {

			String url = StringTools.patternFormat(pattern, attributeMap);
			if (!StringTools.isBlank(url)) {

				String extension = FileTools.getExtension(url);
				if (StringTools.isBlank(extension)) {
					// We have to assume something
					extension = "jpg";
				}

				CloseableHttpClient httpClient = null;
				CloseableHttpResponse response = null;
				MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(Numbers.KILOBYTE * 20);
				try {
					String normalizedUserName = FileTools.normalizeFilename(user.getName(), '_');

					httpClient = httpClientProvider.provideHttpClient();
					HttpGet get = new HttpGet(url);
					response = httpClient.execute(get);
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						logger.debug("Got a non-200 response from " + url + ": " + response);
					} else {
						Resource uploadedResource = null;
						MessageDigest md = MessageDigest.getInstance("MD5");

						try (InputStream is = new DigestInputStream(new ResponseEntityInputStream(response), md)) {
							IOTools.pump(is, buffer);
						}
						String newMd5 = digest(md);

						if (oldMd5 == null || !oldMd5.equals(newMd5)) {

							try (InputStream in = buffer.openInputStream(true)) {

								uploadedResource = session.resources().create().name("image-" + normalizedUserName + "." + extension).store(in);

								SimpleIcon picture = session.create(SimpleIcon.T);
								picture.setImage(uploadedResource);
								user.setPicture(picture);
							}

						}
					}
				} catch (Exception e) {
					logger.error("Could not download user icon at: " + url, e);
				} finally {
					buffer.delete();
					IOTools.closeCloseable(response, logger);
					IOTools.closeCloseable(httpClient, logger);
				}
			}
		}
	}

	private static String digest(MessageDigest md) {
		return convertToHex(md.digest());
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	private String getUsername(ShiroClient shiroClient, UserProfile cp, Map<String, Object> attributeMap) {

		List<String> usernamePatterns = shiroClient.getUsernamePatterns();
		String username = null;

		logger.debug(() -> "Username patterns: " + usernamePatterns);

		if (usernamePatterns == null || usernamePatterns.isEmpty()) {
			// This is the fallback mechanism that is not going to work for all clients
			Object value = attributeMap.get("email");
			if (value instanceof String) {
				username = (String) value;
				if (logger.isDebugEnabled())
					logger.debug("Fallback value for email: " + username);
			}
			if (StringTools.isBlank(username)) {
				value = attributeMap.get("name");
				if (value instanceof String) {
					username = (String) value;
					if (logger.isDebugEnabled())
						logger.debug("Fallback value for name: " + username);
				}
			}
		} else {

			for (String usernamePattern : usernamePatterns) {
				try {
					username = StringTools.patternFormat(usernamePattern, attributeMap);
					if (logger.isDebugEnabled())
						logger.debug("Username derived from pattern: " + username);
					break;
				} catch (IllegalArgumentException iae) {
					logger.debug("Could not derive a username from pattern '" + usernamePattern + "' with properties: " + attributeMap);
				}
			}
		}
		if (StringTools.isBlank(username)) {
			username = null;
		}
		if (logger.isDebugEnabled())
			logger.debug("Username: " + username);

		return username;
	}

	private String handleUrl(String sessionId, String continuePath) throws CodecException {
		String redirectUrl;
		int separatorIdx = continuePath.indexOf("?");
		String continueQuery = null;
		if (separatorIdx > 0) {
			continueQuery = continuePath.substring(separatorIdx + 1);
			continuePath = continuePath.substring(0, separatorIdx);
		}

		Map<String, String> continueQueryMap = getUrlParamCodec().decode(continueQuery);
		continueQueryMap.put(Constants.REQUEST_PARAM_SESSIONID, sessionId);
		continueQuery = getUrlParamCodec().encode(continueQueryMap);

		redirectUrl = continuePath + ((continueQuery.isEmpty()) ? "" : "?" + continueQuery);
		return redirectUrl;
	}

	public Codec<Map<String, String>, String> getUrlParamCodec() {
		if (urlParamCodec == null) {
			MapCodec<String, String> mapCodec = new MapCodec<String, String>();
			mapCodec.setEscapeCodec(new UrlEscapeCodec());
			mapCodec.setDelimiter("&");
			this.urlParamCodec = mapCodec;
		}
		return urlParamCodec;
	}

	protected void initializeContext(HttpServletRequest httpRequest) {

		AttributeContext currentContext = AttributeContexts.peek();
		AttributeContext derivedContext = currentContext.derive().set(RequestedEndpointAspect.class, httpRequest.getRequestURL().toString())
				.set(RequestorAddressAspect.class, getClientRemoteInternetAddress(httpRequest)).build();

		AttributeContexts.push(derivedContext);
	}

	protected void popContext() {
		try {
			AttributeContexts.pop();
		} catch (Exception e) {
			logger.error("Failed to pop the service context" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	private void enrichOpenUserSessionRequest(OpenUserSession authRequest, ShiroClient shiroClient, Map<String, Object> attributeMap) {

		Set<String> propertyNames = shiroClient.getSessionPropertyNames();
		if (propertyNames.isEmpty()) {
			return;
		}
		logger.debug(() -> "Configured session property names: " + propertyNames);

		if (propertyNames.contains("*")) {
			for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
				String propName = entry.getKey();
				String attributeAsString = "" + entry.getValue();
				logger.debug(() -> "Setting open session request property: " + propName + "=" + attributeAsString);
				authRequest.getProperties().put(propName, attributeAsString);
			}
		} else {
			for (String propName : propertyNames) {
				Object attribute = attributeMap.get(propName);
				if (attribute != null) {
					String attributeAsString = attribute.toString();
					logger.debug(() -> "Setting open session request property: " + propName + "=" + attributeAsString);
					authRequest.getProperties().put(propName, attributeAsString);
				} else {
					logger.debug(() -> "Could not find profile property: " + propName);
				}
			}
		}
	}

	protected OpenUserSession createOpenUserSessionRequest(String user, HttpServletRequest request) {
		OpenUserSession authReq = OpenUserSession.T.create();

		UserNameIdentification identification = UserNameIdentification.T.create();
		GrantedCredentials credentials = GrantedCredentials.T.create();

		UserSession trustedSession = AttributeContexts.peek().findOrNull(UserSessionAspect.class);
		String existingSessionId = trustedSession.getSessionId();

		ExistingSessionCredentials existing = ExistingSessionCredentials.T.create();
		existing.setReuseSession(false);
		existing.setExistingSessionId(existingSessionId);

		credentials.setGrantingCredentials(existing);

		identification.setUserName(user);
		credentials.setUserIdentification(identification);

		authReq.setCredentials(credentials);

		RemoteClientAddressResolver resolver = getRemoteAddressResolver();
		try {
			RemoteAddressInformation remoteAddressInformation = resolver.getRemoteAddressInformation(request);
			String remoteAddress = remoteAddressInformation.getRemoteIp();
			logger.info("Received an authentication request for user '" + user + "' from [" + remoteAddress + "]. Remote Address Information: "
					+ remoteAddressInformation.toString());
		} catch (Exception e) {
			String message = "Could not use the client address resolver to get the client's IP address. User: '" + user + "'";
			logger.info(message);
			if (logger.isDebugEnabled())
				logger.debug(message, e);
		}

		return authReq;
	}

	private UserSession authenticate(HttpServletResponse resp, OpenUserSession authRequest) throws AuthenticationException {
		try {
			EvalContext<? extends OpenUserSessionResponse> responseContext = authRequest.eval(evaluator);
			Maybe<? extends OpenUserSessionResponse> reasoned = responseContext.getReasoned();

			if (reasoned.isSatisfied()) {
				return reasoned.get().getUserSession();
			}

			logger.debug(() -> "Could not authenticate the user: " + reasoned.whyUnsatisfied().asString());

			buildResponseMessage(resp, "Invalid authentication!");

			return null;
		} catch (Exception e) {
			throw new AuthenticationException("Error while trying to evaluate the authentication request: " + authRequest, e);
		}
	}

	private void buildResponseMessage(HttpServletResponse resp, String message) throws IOException, UnsupportedEncodingException {
		String redirectUrl = TribefireRuntime.getPublicServicesUrl() + "/component/" + externalId + "?message=" + URLEncoder.encode(message, "UTF-8");
		resp.sendRedirect(redirectUrl);
	}

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse repsonse) {

		String continueParameter = request.getParameter(Constants.REQUEST_PARAM_CONTINUE);
		String urlEncodedContinueParameter = null;
		if (!StringTools.isBlank(continueParameter)) {
			try {
				urlEncodedContinueParameter = URLEncoder.encode(continueParameter, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.info("The continue parameter " + continueParameter + " could not be URL-encoded.", e);
			}
		}

		Map<String, String> authUrls = new LinkedHashMap<>();
		Map<String, String> authImageUrls = new LinkedHashMap<>();
		Map<String, String> authEmbeddedImages = new LinkedHashMap<>();
		String tfs = TribefireRuntime.getPublicServicesUrl();
		PersistenceGmSession cortexSession = sessionFactory.newSession(TribefireConstants.ACCESS_CORTEX);
		for (ShiroClient client : configuration.getClients()) {
			String clientName = client.getName();
			String authUrl = tfs + "/component/" + pathIdentifier + "/auth/" + clientName.toLowerCase();
			if (urlEncodedContinueParameter != null) {
				authUrl = authUrl.concat("?").concat(Constants.REQUEST_PARAM_CONTINUE).concat("=").concat(urlEncodedContinueParameter);
			}
			authUrls.put(clientName, authUrl);

			// tribefire-services/static/tribefire.extension.shiro.shiro-cartridge/login-images/google.png
			String imageUrl = tfs + staticImagesRelativePath + clientName.toLowerCase() + ".png";
			authImageUrls.put(clientName, imageUrl);

			Resource icon = client.getLoginIcon();
			if (icon != null) {
				String iconContent = iconContentCache.computeIfAbsent(icon.getId(), id -> {
					try (InputStream in = cortexSession.resources().openStream(icon)) {
						byte[] imageBytes = IOTools.slurpBytes(in);
						String base64Image = Base64.getEncoder().encodeToString(imageBytes);
						String fullData = "data:".concat(icon.getMimeType()).concat(";base64,").concat(base64Image);
						return fullData;

					} catch (Exception e) {
						logger.warn(() -> "Could not load icon " + icon, e);
					}
					return null;
				});
				if (iconContent != null) {
					authEmbeddedImages.put(clientName, iconContent);
				}
			}
		}

		VelocityContext context = new VelocityContext();
		context.put("continue", request.getParameter(Constants.REQUEST_PARAM_CONTINUE));
		context.put("message", request.getParameter(Constants.REQUEST_PARAM_MESSAGE));
		context.put("messageStatus", request.getParameter(Constants.REQUEST_PARAM_MESSAGESTATUS));
		context.put("tribefireRuntime", TribefireRuntime.class);
		context.put("authUrls", authUrls);
		context.put("authImageUrls", authImageUrls);
		context.put("authEmbeddedImages", authEmbeddedImages);
		context.put("showStandardLoginForm", showStandardLoginForm);
		context.put("showTextLinks", showTextLinks);

		if (offerStaySigned()) {
			context.put("offerStaySigned", Boolean.TRUE);
		} else {
			context.put("offerStaySigned", Boolean.FALSE);
		}

		context.put("publicServicesUrl", TribefireRuntime.getPublicServicesUrl());

		return context;
	}

	protected static boolean offerStaySigned() {
		String offerStayLoggedIn = TribefireRuntime.getProperty(TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED);
		if (offerStayLoggedIn != null && offerStayLoggedIn.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * <p>
	 * Retrieves the client's remote Internet protocol address.
	 * 
	 * @param request
	 *            The request from the client.
	 * @return The remote address of the client.
	 */
	private String getClientRemoteInternetAddress(HttpServletRequest request) {
		return getRemoteAddressResolver().getRemoteIpLenient(request);
	}

	private String acquireContinuePath(HttpServletRequest req) {
		String continuePath = req.getParameter(Constants.REQUEST_PARAM_CONTINUE);
		if (continuePath == null) {
			String defaultRedirectUrl = configuration.getDefaultRedirectUrl();
			if (!StringTools.isBlank(defaultRedirectUrl)) {
				continuePath = defaultRedirectUrl;
			} else {
				continuePath = servicesUrl; // Default page after successful sign-in.
			}
		}
		return continuePath;
	}

	private PersistenceGmSession authSession() {
		SessionFactoryBasedSessionProvider bean = new SessionFactoryBasedSessionProvider();
		bean.setAccessId(authAccessIdSupplier.get());
		bean.setPersistenceGmSessionFactory(this.sessionFactory);
		return bean.get();
	}

	private static Map<String, Object> getAttributeMap(UserProfile cp) {
		Map<String, Object> map = new LinkedHashMap<>(cp.getAttributes());
		map.putIfAbsent("id", cp.getId());
		map.putIfAbsent("linkedId", cp.getLinkedId());

		Map<String, Object> attributes = cp.getAttributes();
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key.toLowerCase().endsWith("token")) {

				String token = null;

				if (value instanceof String) {
					token = (String) value;
				} else if (value instanceof AccessToken) {
					AccessToken bac = (AccessToken) value;
					token = bac.getValue();
				}

				if (!StringTools.isBlank(token)) {
					logger.debug("Looking at potential JWT token: " + key + "=" + token);

					int i = token.lastIndexOf('.');
					if (i > 0 && StringTools.countOccurrences(token, ".") == 2) {
						String withoutSignature = token.substring(0, i + 1);

						try {
							Jwt jwt = Jwts.parserBuilder().build().parse(withoutSignature);
							Claims body = (Claims) jwt.getBody();

							for (Map.Entry<String, Object> claim : body.entrySet()) {
								String claimKey = claim.getKey();
								if (!map.containsKey(claimKey)) {
									Object claimValue = claim.getValue();
									map.put(claimKey, claimValue);
									logger.debug(() -> "Added claim: " + claimKey + " = " + claimValue);
								}
							}
						} catch (ExpiredJwtException e) {
							logger.warn("Not accepting an expired token: " + key + ": " + token);
						} catch (Exception e) {
							logger.debug("Error while parsing potential JWT token: " + key + ": " + token, e);
						}
					}
				}
			}
		}

		return map;
	}

	@Configurable
	public void setRemoteAddressResolver(RemoteClientAddressResolver remoteAddressResolver) {
		this.remoteAddressResolver = remoteAddressResolver;
	}
	public RemoteClientAddressResolver getRemoteAddressResolver() {
		if (remoteAddressResolver == null) {
			remoteAddressResolver = DefaultRemoteClientAddressResolver.getDefaultResolver();
		}
		return remoteAddressResolver;
	}

	@Configurable
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	@Configurable
	public void setAddSessionParameter(boolean addSessionParameter) {
		this.addSessionUrlParameter = addSessionParameter;
	}

	@Configurable
	public void setUrlParamCodec(Codec<Map<String, String>, String> urlParamCodec) {
		this.urlParamCodec = urlParamCodec;
	}

	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Configurable
	public void setCreateUsers(boolean createUsers) {
		this.createUsers = createUsers;
	}
	@Configurable
	public void setConfiguration(ShiroAuthenticationConfiguration configuration) {
		this.configuration = configuration;
	}
	@Configurable
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	@Configurable
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}
	@Configurable
	public void setNewUserRoleProvider(NewUserRoleProvider newUserRoleProvider) {
		this.newUserRoleProvider = newUserRoleProvider;
	}

	@Configurable
	public void setUserAcceptList(Set<String> userAcceptList) {
		this.userAcceptList = userAcceptList;
	}
	@Configurable
	public void setUserBlockList(Set<String> userBlockList) {
		this.userBlockList = userBlockList;
	}

	@Configurable
	public void setShowStandardLoginForm(boolean showStandardLoginForm) {
		this.showStandardLoginForm = showStandardLoginForm;
	}
	@Configurable
	public void setShowTextLinks(boolean showTextLinks) {
		this.showTextLinks = showTextLinks;
	}
	@Configurable
	@Required
	public void setPathIdentifier(String pathIdentifier) {
		this.pathIdentifier = pathIdentifier;
	}
	@Required
	public void setAuthAccessIdSupplier(Supplier<String> authAccessIdSupplier) {
		this.authAccessIdSupplier = authAccessIdSupplier;
	}
	@Configurable
	@Required
	public void setCookieHandler(CookieHandler cookieHandler) {
		this.cookieHandler = cookieHandler;
	}
	@Configurable
	@Required
	public void setStaticImagesRelativePath(String staticImagesRelativePath) {
		this.staticImagesRelativePath = staticImagesRelativePath;
	}
	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	@Configurable
	@Required
	public void setExternalIconUrlHelper(ExternalIconUrlHelper externalIconUrlHelper) {
		this.externalIconUrlHelper = externalIconUrlHelper;
	}
	@Configurable
	@Required
	public void setObfuscateLogOutput(Boolean obfuscateLogOutput) {
		if (obfuscateLogOutput != null) {
			this.obfuscateLogOutput = obfuscateLogOutput;
		}
	}

}
