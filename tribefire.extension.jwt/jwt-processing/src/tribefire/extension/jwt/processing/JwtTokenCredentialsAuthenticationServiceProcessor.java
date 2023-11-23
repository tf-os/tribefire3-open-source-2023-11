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
package tribefire.extension.jwt.processing;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.impl.AbstractAuthenticateCredentialsServiceProcessor;
import com.braintribe.model.processing.securityservice.impl.Roles;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.AuthenticatedUser;
import com.braintribe.model.securityservice.credentials.JwtTokenCredentials;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.SignatureException;
import tribefire.extension.jwt.deployment.model.JwtTokenCredentialsAuthenticator;
import tribefire.extension.jwt.model.Jwks;
import tribefire.extension.jwt.model.JwksKey;

public class JwtTokenCredentialsAuthenticationServiceProcessor extends AbstractAuthenticateCredentialsServiceProcessor<JwtTokenCredentials> {
	private static final Reason INVALID_CREDENTIALS = Reasons.build(InvalidCredentials.T).text("Invalid Credentials").toReason();

	private static final Logger logger = Logger.getLogger(JwtTokenCredentialsAuthenticationServiceProcessor.class);

	private JwtTokenCredentialsAuthenticator configuration;
	private HttpClientProvider httpClientProvider;
	private CharacterMarshaller jsonMarshaller;
	private PeriodicInitialized<Map<String, Key>> keyMapHolder = new PeriodicInitialized<>(this::getJwksKeys);

	/**
	 * @param ms
	 *            default is 10 minutes
	 */
	@Configurable
	public void setKeyMapReloadIntervalInMs(long ms) {
		keyMapHolder.setUpdateIntervalInMs(ms);
	}

	@Required
	public void setJsonMarshaller(CharacterMarshaller jsonMarshaller) {
		this.jsonMarshaller = jsonMarshaller;
	}

	@Required
	public void setConfiguration(JwtTokenCredentialsAuthenticator configuration) {
		this.configuration = configuration;
	}

	@Required
	@Configurable
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	@Override
	protected Maybe<AuthenticateCredentialsResponse> authenticateCredentials(ServiceRequestContext context, AuthenticateCredentials request,
			JwtTokenCredentials credentials) {

		String token = credentials.getToken();

		Maybe<Map<String, Key>> keyMapMaybe = keyMapHolder.get();

		if (keyMapMaybe.isUnsatisfied()) {
			String tbid = UUID.randomUUID().toString();
			logger.error("Error while retrieving JWKS keys from " + configuration.getJwksUrl() + " (traceback=" + tbid + "): "
					+ keyMapMaybe.whyUnsatisfied().stringify());
			return Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text("Internal Error (traceback=" + tbid + ")").toMaybe();
		}

		Map<String, Key> keyMap = keyMapMaybe.get();

		Maybe<Jwt<?, ?>> jwtMaybe = resolveJwt(token, keyMap);

		if (jwtMaybe.isUnsatisfied())
			return jwtMaybe.whyUnsatisfied().asMaybe();

		Jwt<?, ?> jwt = jwtMaybe.get();

		JwsHeader<?> header = (JwsHeader<?>) jwt.getHeader();

		String keyId = header.getKeyId();

		logger.debug(() -> "Successfully parsed token " + token + " with key " + keyId);

		Claims body = (Claims) jwt.getBody();

		String userId = (String) body.get(configuration.getUsernameClaim());

		User user = User.T.create();
		user.setId(userId);
		user.setName(userId);

		Set<Role> roles = user.getRoles();

		Stream.of(configuration.getDefaultRoles(), getRolesFromToken(body)) //
				.flatMap(Collection::stream)//
				.distinct() //
				.map(Roles::roleFromStr) //
				.forEach(roles::add);

		AuthenticatedUser authenticatedUser = AuthenticatedUser.T.create();
		authenticatedUser.setUser(user);

		// transfer properties
		Set<String> propertiesClaims = configuration.getPropertiesClaims();
		if (!propertiesClaims.isEmpty()) {
			Map<String, String> properties = authenticatedUser.getProperties();

			for (String propClaim : propertiesClaims) {
				Object value = body.get(propClaim);
				if (value != null) {
					properties.put(propClaim, value.toString());
				}
			}
		}

		// transfer expiry
		Optional.ofNullable(body.getExpiration()) //
				.ifPresent(authenticatedUser::setExpiryDate);

		authenticatedUser.setInvalidateCredentialsOnLogout(configuration.getInvalidateTokenCredentialsOnLogout());

		return Maybe.complete(authenticatedUser);
	}

	private Maybe<Jwt<?, ?>> resolveJwt(String token, Map<String, Key> keyMap) {
		JwtParser parser = Jwts.parserBuilder().setSigningKeyResolver(new SigningKeyResolverAdapter() {
			@Override
			public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
				// null may be returned here from the map which leads to an expected IllegalArgumentException
				return keyMap.get(jwsHeader.getKeyId());
			}
		}).build();
		try {
			return Maybe.complete(parser.parse(token));
		} catch (SignatureException | ExpiredJwtException | MalformedJwtException | IllegalArgumentException e) {
			if (logger.isDebugEnabled())
				logger.debug("Token " + token + " + could not be verified", e);
			else
				logger.info("Token could not be verified", e);
			return INVALID_CREDENTIALS.asMaybe();
		}
	}

	private Maybe<Map<String, Key>> getJwksKeys() {

		String url = configuration.getJwksUrl();
		Map<String, Key> keyMap = new LinkedHashMap<>();

		logger.debug(() -> "Trying to get JWKS from: " + url);
		try (CloseableHttpClient client = httpClientProvider.provideHttpClient()) {

			CloseableHttpResponse response = null;
			try {
				HttpGet get = new HttpGet(url);
				response = client.execute(get);
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return Reasons.build(CommunicationError.T)
							.text(url + " unexpectedly responded with status code " + response.getStatusLine().getStatusCode()).toMaybe();
				} else {
					try (InputStream is = new ResponseEntityInputStream(response)) {

						Jwks jwks = (Jwks) jsonMarshaller.unmarshall(is,
								GmDeserializationOptions.deriveDefaults().setInferredRootType(Jwks.T).build());
						logger.debug(() -> "Parsed from " + url + ": " + jwks);
						List<JwksKey> keys = jwks.getKeys();
						for (JwksKey key : keys) {
							String keyId = key.getKid();
							String mod = key.getN();
							String exp = key.getE();
							byte[] modBytes = Base64.getUrlDecoder().decode(mod.getBytes());
							byte[] expBytes = Base64.getUrlDecoder().decode(exp.getBytes());
							BigInteger modulus = new BigInteger(1, modBytes);
							BigInteger exponent = new BigInteger(1, expBytes);
							RSAPublicKeySpec pubKeySpecification = new RSAPublicKeySpec(modulus, exponent);
							KeyFactory keyFac = KeyFactory.getInstance("RSA");
							RSAPublicKey rsaPub = (RSAPublicKey) keyFac.generatePublic(pubKeySpecification);

							if (logger.isDebugEnabled()) {
								final X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPub.getEncoded());
								logger.debug("Base64 encoded key for key Id " + keyId + ": "
										+ new String(Base64.getEncoder().encode(x509EncodedKeySpec.getEncoded())));
							}

							keyMap.put(keyId, rsaPub);
						}
					}
				}
			} finally {
				HttpTools.consumeResponse(url, response);
				if (response != null) {
					try {
						response.close();
					} catch (Exception e) {
						// Ignore
					}
				}
			}
		} catch (IOException ioe) {
			String msg = "Error while reading JWKS keys from " + configuration.getJwksUrl();
			logger.error(msg, ioe);
			return Reasons.build(IoError.T).text(msg).toMaybe();
		} catch (Exception e) {
			String msg = "Error while reading JWKS keys from " + configuration.getJwksUrl();
			logger.error(msg, e);
			return Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text(msg).toMaybe();
		}

		return Maybe.complete(keyMap);
	}

	protected Collection<String> getRolesFromToken(Map<String, Object> claims) {

		Set<String> allRolesCombined = new HashSet<>();

		for (Map.Entry<String, String> rolesEntry : configuration.getClaimRolesAndPrefixes().entrySet()) {

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
