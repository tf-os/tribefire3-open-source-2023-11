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
package tribefire.extension.okta.processing.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.processing.securityservice.impl.AbstractAuthenticateCredentialsServiceProcessor;
import com.braintribe.model.processing.securityservice.impl.Roles;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.AuthenticatedUser;
import com.braintribe.model.securityservice.credentials.JwtTokenCredentials;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.okta.jwt.Jwt;

import io.jsonwebtoken.Claims;

abstract public class AbstractOktaJwtTokenCredentialsAuthenticationServiceProcessor
		extends AbstractAuthenticateCredentialsServiceProcessor<JwtTokenCredentials> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AbstractOktaJwtTokenCredentialsAuthenticationServiceProcessor.class);

	private Set<String> propertiesClaims = Collections.emptySet();
	private Set<String> defaultRoles = Collections.emptySet();
	private String usernameClaim = Claims.SUBJECT;

	private Map<String, String> claimRolesAndPrefixes = Collections.emptyMap();

	private boolean invalidateTokenCredentialsOnLogout;

	@Configurable
	public void setPropertiesClaims(Set<String> propertiesClaims) {
		this.propertiesClaims = propertiesClaims;
	}

	@Configurable
	public void setDefaultRoles(Set<String> defaultRoles) {
		this.defaultRoles = defaultRoles;
	}

	@Configurable
	public void setClaimRolesAndPrefixes(Map<String, String> claimRolesAndPrefixes) {
		this.claimRolesAndPrefixes = claimRolesAndPrefixes;
	}

	@Required
	@Configurable
	@Initializer("'sub'")
	public void setUsernameClaim(String usernameClaim) {
		this.usernameClaim = Optional.ofNullable(usernameClaim).orElse(Claims.SUBJECT);
	}

	@Configurable
	public void setInvalidateTokenCredentialsOnLogout(boolean invalidateTokenCredentialsOnLogout) {
		this.invalidateTokenCredentialsOnLogout = invalidateTokenCredentialsOnLogout;
	}

	@Override
	protected Maybe<AuthenticateCredentialsResponse> authenticateCredentials(ServiceRequestContext context, AuthenticateCredentials request,
			JwtTokenCredentials credentials) {

		String token = credentials.getToken();

		if (token == null) {
			return Reasons.build(InvalidCredentials.T).text("JwtTokenCredentials.token must not be null").toMaybe();
		}

		Maybe<Jwt> jwtMaybe = decodeJwt(token);

		if (jwtMaybe.isUnsatisfied())
			return jwtMaybe.whyUnsatisfied().asMaybe();

		Jwt jwt = jwtMaybe.get();

		Map<String, Object> claims = jwt.getClaims();

		String userId = (String) claims.get(usernameClaim);

		User user = User.T.create();
		user.setId(userId);
		user.setName(userId);

		Set<Role> roles = user.getRoles();

		Stream.of(defaultRoles, getRolesFromToken(claims)) //
				.flatMap(Collection::stream)//
				.distinct() //
				.map(Roles::roleFromStr) //
				.forEach(roles::add);

		AuthenticatedUser authenticatedUser = AuthenticatedUser.T.create();
		authenticatedUser.setUser(user);

		// transfer properties
		if (!propertiesClaims.isEmpty()) {
			Map<String, String> properties = authenticatedUser.getProperties();

			for (String propClaim : propertiesClaims) {
				Object value = claims.get(propClaim);
				if (value != null) {
					properties.put(propClaim, value.toString());
				}
			}
		}

		// transfer expiry
		Optional.ofNullable(jwt.getExpiresAt()) //
				.map(Date::from) //
				.ifPresent(authenticatedUser::setExpiryDate);

		authenticatedUser.setInvalidateCredentialsOnLogout(invalidateTokenCredentialsOnLogout);

		return Maybe.complete(authenticatedUser);

	}

	protected abstract Maybe<Jwt> decodeJwt(String token);

	protected Collection<String> getRolesFromToken(Map<String, Object> claims) {

		Set<String> allRolesCombined = new HashSet<>();

		for (Map.Entry<String, String> rolesEntry : claimRolesAndPrefixes.entrySet()) {

			String key = rolesEntry.getKey();
			String prefix = Optional.ofNullable(rolesEntry.getValue()).orElse("");

			Object rolesObject = claims.get(key);

			if (rolesObject == null)
				continue;

			Stream<String> rawRoleValues = claimValuesAsStream(rolesObject);

			if (!prefix.isEmpty())
				rawRoleValues = rawRoleValues.map(v -> prefix + v);

			rawRoleValues.forEach(allRolesCombined::add);
		}

		return allRolesCombined;
	}

	private Stream<String> claimValuesAsStream(Object rolesObject) {
		if (rolesObject instanceof Collection) {
			return ((Collection<String>) rolesObject).stream();
		} else if (rolesObject instanceof String) {
			return Stream.of((String) rolesObject);
		} else {
			return Stream.empty();
		}
	}

}
