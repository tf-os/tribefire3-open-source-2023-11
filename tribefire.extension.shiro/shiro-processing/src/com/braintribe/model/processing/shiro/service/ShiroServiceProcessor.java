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
package com.braintribe.model.processing.shiro.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.SessionFactoryBasedSessionProvider;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.processing.shiro.bootstrapping.MulticastSessionDao;
import com.braintribe.model.processing.shiro.util.IdTokenContent;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroClient;
import com.braintribe.model.shiro.service.AuthenticationSpecification;
import com.braintribe.model.shiro.service.EnsureUserByIdToken;
import com.braintribe.model.shiro.service.EnsuredUser;
import com.braintribe.model.shiro.service.GetSupportedLogins;
import com.braintribe.model.shiro.service.ShiroRequest;
import com.braintribe.model.shiro.service.ShiroResult;
import com.braintribe.model.shiro.service.SupportedLogins;
import com.braintribe.model.shiro.service.dist.DeleteSession;
import com.braintribe.model.shiro.service.dist.GetSession;
import com.braintribe.model.shiro.service.dist.SerializedSession;
import com.braintribe.model.shiro.service.dist.UpdateSession;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.utils.lcd.StringTools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

public class ShiroServiceProcessor extends AbstractDispatchingServiceProcessor<ShiroRequest, ShiroResult> implements LifecycleAware {

	private final static Logger logger = Logger.getLogger(ShiroServiceProcessor.class);

	private ShiroAuthenticationConfiguration configuration;
	private String pathIdentifier = ShiroConstants.PATH_IDENTIFIER;
	private String staticImagesRelativePath;
	private MulticastSessionDao multicastSessionDao = null;
	private Supplier<String> authAccessIdSupplier;
	private PersistenceGmSessionFactory sessionFactory;

	@Override
	protected void configureDispatching(DispatchConfiguration<ShiroRequest, ShiroResult> dispatching) {
		dispatching.register(GetSupportedLogins.T, this::getSupportedLogins);
		dispatching.register(GetSession.T, this::getShiroSession);
		dispatching.register(UpdateSession.T, this::updateShiroSession);
		dispatching.register(DeleteSession.T, this::deleteShiroSession);
		dispatching.register(EnsureUserByIdToken.T, this::ensureUserByIdToken);
	}

	protected EnsuredUser ensureUserByIdToken(ServiceRequestContext context, EnsureUserByIdToken request) {
		EnsuredUser result = EnsuredUser.T.create();

		String idToken = request.getIdToken();
		IdTokenContent tokenContent = parseIdToken(idToken, request);
		logger.debug(() -> "Parsed ID token " + idToken + " to: " + tokenContent);

		User user = ensureUser(tokenContent.username, tokenContent.firstName, tokenContent.lastName, tokenContent.email, tokenContent.roles);

		result.setSuccess(true);
		result.setEnsuredUser(user);
		result.setSubject(tokenContent.subject);

		return result;
	}

	protected IdTokenContent parseIdToken(String idToken, EnsureUserByIdToken request) {
		int i = idToken.lastIndexOf('.');
		String withoutSignature = idToken.substring(0, i + 1);
		Jwt<Header, Claims> claimsJwt = Jwts.parser().parseClaimsJwt(withoutSignature);
		Claims body = claimsJwt.getBody();

		IdTokenContent result = new IdTokenContent();

		result.audience = body.getAudience();
		result.expiration = body.getExpiration();
		result.issuedAt = body.getIssuedAt();
		result.issuer = body.getIssuer();

		result.subject = body.getSubject();
		result.roles = new HashSet<>();
		String rolesClaimId = request.getRolesClaim();
		if (!StringTools.isBlank(rolesClaimId)) {
			Object rolesObject = body.get(rolesClaimId);
			if (rolesObject instanceof Collection) {
				Collection<String> r = (Collection<String>) rolesObject;
				result.roles.addAll(r);
			}
		}
		result.roles.add("$" + result.subject);
		result.email = getClaim(body, request.getEmailClaim(), null);
		result.username = getClaim(body, request.getUsernameClaim(), result.subject);
		result.firstName = getClaim(body, request.getFirstNameClaim(), null);
		result.lastName = getClaim(body, request.getLastNameClaim(), null);
		result.fullName = getClaim(body, request.getNameClaim(), null);
		if (result.firstName == null && result.lastName == null && !StringTools.isBlank(result.fullName)) {
			result.fullName = result.fullName.trim();
			int idx = result.fullName.indexOf(' ');
			if (idx == -1) {
				result.lastName = result.fullName;
			} else {
				result.firstName = result.fullName.substring(0, idx).trim();
				result.lastName = result.fullName.substring(idx + 1).trim();
			}
		}

		return result;
	}

	protected User ensureUser(String username, String firstName, String lastName, String email, Set<String> roles) {

		PersistenceGmSession authSession = authSession();
		EntityQuery query = EntityQueryBuilder.from(User.T).where().property(User.name).eq(username).done();
		User result = null;
		User existingUser = authSession.query().entities(query).first();
		if (existingUser == null) {

			logger.debug(() -> "User " + username + " does not yet exist but we will create it now.");

			User newUser = authSession.create(User.T);
			newUser.setName(username);
			newUser.setFirstName(firstName);
			newUser.setLastName(lastName);
			newUser.setEmail(email);
			if (roles != null) {
				Set<Role> roleObjects = ensureRoles(authSession, roles);
				newUser.getRoles().addAll(roleObjects);
			}
			result = newUser;

		} else {

			setIfChanged(existingUser.getFirstName(), firstName, existingUser::setFirstName);
			setIfChanged(existingUser.getLastName(), lastName, existingUser::setLastName);
			setIfChanged(existingUser.getEmail(), email, existingUser::setEmail);

			Set<Role> roleObjects = ensureRoles(authSession, roles);
			if (roleObjects != null) {
				Set<Role> existingRoles = existingUser.getRoles();
				if (!roleObjects.equals(existingRoles)) {
					existingRoles.clear();
					existingRoles.addAll(roleObjects);
				}
			}

			result = existingUser;
		}

		authSession.commit();

		return result;
	}

	private Set<Role> ensureRoles(PersistenceGmSession authSession, Collection<String> roles) {
		if (roles == null || roles.isEmpty()) {
			return null;
		}
		Set<String> remainingRoles = new HashSet<>(roles);

		Set<Role> allRoles = new HashSet<>();

		EntityQuery roleQuery = EntityQueryBuilder.from(Role.T).where().property(Role.name).in(remainingRoles).done();
		List<Role> roleList = authSession.query().entities(roleQuery).list();
		if (roleList != null) {
			for (Role role : roleList) {
				remainingRoles.remove(role.getName());
				allRoles.add(role);
			}
		}
		for (String newRole : remainingRoles) {
			Role role = authSession.create(Role.T);
			role.setName(newRole);
			allRoles.add(role);
		}
		authSession.commit();
		return allRoles;
	}

	private void setIfChanged(String existingValue, String newValue, Consumer<String> setter) {
		if (existingValue == null && newValue == null) {
			return;
		}
		if ((existingValue == null && newValue != null) || (existingValue != null && newValue == null)) {
			setter.accept(newValue);
			return;
		}
		if (existingValue.equals(newValue)) {
			setter.accept(newValue);
		}
	}

	private PersistenceGmSession authSession() {
		SessionFactoryBasedSessionProvider bean = new SessionFactoryBasedSessionProvider();
		bean.setAccessId(authAccessIdSupplier.get());
		bean.setPersistenceGmSessionFactory(this.sessionFactory);
		return bean.get();
	}

	private String getClaim(Claims body, String key, String defaultValue) {
		if (!StringTools.isBlank(key)) {
			String mapValue = (String) body.get(key);
			if (mapValue != null) {
				return mapValue;
			}
		}
		return defaultValue;
	}

	private SerializedSession getShiroSession(ServiceRequestContext context, GetSession request) {

		SerializedSession result = SerializedSession.T.create();

		String shiroSessionId = request.getShiroSessionId();

		String serSession = multicastSessionDao.getLocalSessionSerialized(shiroSessionId);
		if (serSession != null) {
			result.setSerializedSession(serSession);
		}
		result.setShiroSessionId(shiroSessionId);

		logger.debug(() -> "Received a GetSession with ID: " + shiroSessionId + ", exists: " + (serSession != null));

		return result;
	}

	public ShiroResult updateShiroSession(ServiceRequestContext context, UpdateSession request) {
		ShiroResult result = ShiroResult.T.create();

		String shiroSessionId = request.getShiroSessionId();
		String serializedSession = request.getSerializedSession();
		boolean updated = multicastSessionDao.updateIfExists(shiroSessionId, serializedSession);

		logger.debug(() -> "Received an UpdateSession with ID: " + shiroSessionId + ", updated: " + updated);

		result.setSuccess(updated);

		return result;
	}

	public ShiroResult deleteShiroSession(ServiceRequestContext context, DeleteSession request) {
		ShiroResult result = ShiroResult.T.create();

		String shiroSessionId = request.getShiroSessionId();
		boolean deleted = multicastSessionDao.deleteIfExists(shiroSessionId);

		logger.debug(() -> "Received an DeleteSession with ID: " + shiroSessionId + ", deleted: " + deleted);

		result.setSuccess(deleted);

		return result;
	}

	public SupportedLogins getSupportedLogins(ServiceRequestContext context, GetSupportedLogins request) {

		SupportedLogins result = SupportedLogins.T.create();

		String tfs = TribefireRuntime.getPublicServicesUrl();
		for (ShiroClient client : configuration.getClients()) {
			String clientName = client.getName();
			String authUrl = tfs + "/component/" + pathIdentifier + "/auth/" + clientName.toLowerCase();
			String imageUrl = tfs + staticImagesRelativePath + clientName.toLowerCase() + ".png";

			AuthenticationSpecification spec = AuthenticationSpecification.T.create();
			spec.setName(clientName);
			spec.setAuthenticationUrl(authUrl);
			spec.setImageUrl(imageUrl);

			result.getLogins().add(spec);
		}

		return result;
	}

	@Override
	public void postConstruct() {
		logger.debug(() -> ShiroServiceProcessor.class.getSimpleName() + " deployed.");
	}

	@Override
	public void preDestroy() {
		logger.debug(() -> ShiroServiceProcessor.class.getSimpleName() + " undeployed.");
	}

	@Configurable
	@Required
	public void setConfiguration(ShiroAuthenticationConfiguration configuration) {
		this.configuration = configuration;
	}
	@Configurable
	@Required
	public void setPathIdentifier(String pathIdentifier) {
		this.pathIdentifier = pathIdentifier;
	}
	@Configurable
	@Required
	public void setStaticImagesRelativePath(String staticImagesRelativePath) {
		this.staticImagesRelativePath = staticImagesRelativePath;
	}
	@Configurable
	@Required
	public void setMulticastSessionDao(MulticastSessionDao multicastSessionDao) {
		this.multicastSessionDao = multicastSessionDao;
	}
	@Configurable
	@Required
	public void setAuthAccessIdSupplier(Supplier<String> authAccessIdSupplier) {
		this.authAccessIdSupplier = authAccessIdSupplier;
	}
	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
