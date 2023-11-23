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
package com.braintribe.model.processing.shiro.bootstrapping.ini;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ObfuscationTest {

	@Test
	public void testObfuscation() {

		//@formatter:off
		String input = "[main]\n"
				+ "# # CONFIG:\n"
				+ "#\n"
				+ "sessionManager = org.apache.shiro.web.session.mgt.DefaultWebSessionManager\n"
				+ "securityManager.sessionManager = $sessionManager\n"
				+ "sessionDAO = com.braintribe.model.processing.shiro.bootstrapping.MulticastSessionDao\n"
				+ "securityManager.sessionManager.sessionDAO = $sessionDAO\n"
				+ "sessionIdGenerator = com.braintribe.model.processing.shiro.bootstrapping.NodeSessionIdGenerator\n"
				+ "securityManager.sessionManager.sessionDAO.sessionIdGenerator = $sessionIdGenerator\n"
				+ "pathUrlResolver = org.pac4j.core.http.callback.PathParameterCallbackUrlResolver\n"
				+ "oidcConfig_Google = org.pac4j.oidc.config.OidcConfiguration\n"
				+ "oidcConfig_Google.clientId = 143716423449-tqprci6h6q8b4pi5minntgctsmc3va6t.apps.googleusercontent.com\n"
				+ "oidcConfig_Google.secret = fQuwFyE1BKYTDTVyWHOsmH14\n"
				+ "oidcConfig_Google.useNonce = true\n"
				+ "Google = org.pac4j.oidc.client.GoogleOidcClient\n"
				+ "Google.configuration = $oidcConfig_Google\n"
				+ "Google.name = Google\n"
				+ "Facebook = org.pac4j.oauth.client.FacebookClient\n"
				+ "Facebook.name = Facebook\n"
				+ "Facebook.key = 283681165473327\n"
				+ "Facebook.secret = ea09a1cea323dbf3eebb3c68a7c2fd90\n"
				+ "Facebook.scope = public_profile,email\n"
				+ "Twitter = org.pac4j.oauth.client.TwitterClient\n"
				+ "Twitter.name = Twitter\n"
				+ "Twitter.key = 2NPdXfrP3R1UHghIbvrjgRbxP\n"
				+ "Twitter.secret = t71VeNzmOKCkNayJHlaEbqSwp5TTTdndJUbZiSXjKB8wesw1Ts\n"
				+ "Github = org.pac4j.oauth.client.GitHubClient\n"
				+ "Github.name = Github\n"
				+ "Github.key = 3652ba3753b1715fec1a\n"
				+ "Github.secret = cf7c2db487c0a4773b3e7861816363b095a02a5a\n"
				+ "Github.scope = user, user:email\n"
				+ "oidcConfig_AzureAd = org.pac4j.oidc.config.AzureAdOidcConfiguration\n"
				+ "oidcConfig_AzureAd.tenant = d5550702-41c0-4023-8b0e-0dbe7e5b4900\n"
				+ "oidcConfig_AzureAd.clientId = 18c8e11e-ddc0-421b-920c-1622ccf7632b\n"
				+ "oidcConfig_AzureAd.secret = GnY+jsfoQhUOevShxakCfT8Fx0lrdBqXFE0h4Bi6khY=\n"
				+ "oidcConfig_AzureAd.useNonce = true\n"
				+ "AzureAd = org.pac4j.oidc.client.AzureAdClient\n"
				+ "AzureAd.configuration = $oidcConfig_AzureAd\n"
				+ "AzureAd.name = AzureAd\n"
				+ "oidcConfig_Cognito = org.pac4j.oidc.config.OidcConfiguration\n"
				+ "oidcConfig_Cognito.clientId = 5mjokea5a6h1medqin0pnp96sn\n"
				+ "oidcConfig_Cognito.secret = ra1a7tdq4t1d9oiu7vf6qk5s3gda3vctm4hgpfjhei7t3ul1680\n"
				+ "oidcConfig_Cognito.useNonce = true\n"
				+ "oidcConfig_Cognito.discoveryURI = https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_ghqPrcf84/.well-known/openid-configuration\n"
				+ "Cognito = org.pac4j.oidc.client.OidcClient\n"
				+ "Cognito.configuration = $oidcConfig_Cognito\n"
				+ "Cognito.name = Cognito\n"
				+ "oidcConfig_Okta = org.pac4j.oidc.config.OidcConfiguration\n"
				+ "oidcConfig_Okta.clientId = 0oaeeng2pqYZZFdGS416\n"
				+ "oidcConfig_Okta.secret = DBKJv4i1_GmsQAE1MJAAenu2q10zUtFrcowhnXZq\n"
				+ "oidcConfig_Okta.useNonce = true\n"
				+ "oidcConfig_Okta.discoveryURI = https://sso.lh.iptiq.com/oauth2/aus73wdfqrNlyabYv416/.well-known/openid-configuration\n"
				+ "Okta = org.pac4j.oidc.client.OidcClient\n"
				+ "Okta.configuration = $oidcConfig_Okta\n"
				+ "Okta.name = Okta\n"
				+ "Instagram = org.pac4j.oauth.client.GenericOAuth20Client\n"
				+ "Instagram.name = Instagram\n"
				+ "Instagram.key = 702327097033467\n"
				+ "Instagram.secret = 1cf0843850bb8c221adf9754e99dc293\n"
				+ "Instagram.authUrl = https://api.instagram.com/oauth/authorize\n"
				+ "Instagram.tokenUrl = https://api.instagram.com/oauth/access_token\n"
				+ "Instagram.profileUrl = https://graph.instagram.com\n"
				+ "Instagram.callbackUrlResolver = $pathUrlResolver\n"
				+ "Instagram.clientAuthenticationMethod = requestBody\n"
				+ "Instagram.customParams = scope:user_profile\n"
				+ "clients.callbackUrl = https://localhost:8443/tribefire-services/component/remote-login/auth/callback\n"
				+ "clients.clients = $Google,$Facebook,$Twitter,$Github,$AzureAd,$Cognito,$Okta,$Instagram\n"
				+ "# # REALM & FILTERS:\n"
				+ "#\n"
				+ "Google_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Google_Filter.config = $config\n"
				+ "Google_Filter.clients = Google\n"
				+ "Facebook_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Facebook_Filter.config = $config\n"
				+ "Facebook_Filter.clients = Facebook\n"
				+ "Twitter_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Twitter_Filter.config = $config\n"
				+ "Twitter_Filter.clients = Twitter\n"
				+ "Github_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Github_Filter.config = $config\n"
				+ "Github_Filter.clients = Github\n"
				+ "AzureAd_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "AzureAd_Filter.config = $config\n"
				+ "AzureAd_Filter.clients = AzureAd\n"
				+ "Cognito_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Cognito_Filter.config = $config\n"
				+ "Cognito_Filter.clients = Cognito\n"
				+ "Okta_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Okta_Filter.config = $config\n"
				+ "Okta_Filter.clients = Okta\n"
				+ "Instagram_Filter = io.buji.pac4j.filter.SecurityFilter\n"
				+ "Instagram_Filter.config = $config\n"
				+ "Instagram_Filter.clients = Instagram\n"
				+ "[urls]\n"
				+ "/component/remote-login/auth/callback = callbackFilter\n"
				+ "/component/remote-login/auth/callback/AzureAd = callbackFilter\n"
				+ "/component/remote-login/auth/callback/Instagram = callbackFilter\n"
				+ "/component/remote-login/auth/google/** = Google_Filter\n"
				+ "/component/remote-login/auth/facebook/** = Facebook_Filter\n"
				+ "/component/remote-login/auth/twitter/** = Twitter_Filter\n"
				+ "/component/remote-login/auth/github/** = Github_Filter\n"
				+ "/component/remote-login/auth/azuread/** = AzureAd_Filter\n"
				+ "/component/remote-login/auth/cognito/** = Cognito_Filter\n"
				+ "/component/remote-login/auth/okta/** = Okta_Filter\n"
				+ "/component/remote-login/auth/instagram/** = Instagram_Filter\n"
				+ "/** = anon";
		//@formatter:on

		String output = ShiroIniFactory.obfuscateSecrets(input);
		assertThat(output).contains("oidcConfig_Google.clientId = 14**********************************************************************");
	}

}
