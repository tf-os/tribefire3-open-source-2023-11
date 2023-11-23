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
package tribefire.extension.okta.processing.base;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.AuthenticatedUser;
import com.braintribe.model.securityservice.credentials.JwtTokenCredentials;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.util.Sets;

public class OktaTokenAuthenticationTest extends OktaProcessingTestBase {

	private static final String token = "eyJraWQiOiJDdFBqZml2Wl9yU1l5WlJPN1hEdGVrbTM4R2VZTERkSFdjOXVLUzd2ZUdnIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULjRYYU9zYklITkF6MkE5Z3JXWDhkemJnX3JOUEVLYmtNdWRPekpqS2NrR28iLCJpc3MiOiJodHRwczovL2lwdGlxLWFuei5va3RhLmNvbS9vYXV0aDIvZGVmYXVsdCIsImF1ZCI6ImFwaTovL2RlZmF1bHQiLCJpYXQiOjE2NDAyNDgyMTAsImV4cCI6MTY0MDI1NTQxMCwiY2lkIjoiMG9hcGFoejNrVFQ0dTkwSUozbDYiLCJ1aWQiOiIwMHVtZ2V2ZnJmNWdabUlLeTNsNiIsInNjcCI6WyJvcGVuaWQiLCJwcm9maWxlIiwiZW1haWwiXSwic3ViIjoicm9tYW4ua3VybWFub3d5dHNjaEBicmFpbnRyaWJlLmNvbSIsInVzZXJfcm9sZSI6IkNsYWltc0FkanVkaWNhdG9yIiwicGFydG5lciI6WyJNRURJQkFOSyIsIkFITSIsIkZSRUVET00iXSwibGV2ZWwiOjMsImFsbF9ncm91cHMiOlsidGYtYWRtaW4iLCJFdmVyeW9uZSIsIkNsYWltQ29uZmlkZW50aWFsRmlsZXMuRGVsZXRlIiwiQ2xhaW1QYXltZW50cy5VcGRhdGUiLCJDbGFpbUZpbmFuY2lhbHMuUmVhZCIsIkNsYWltUmVxdWlyZW1lbnRzLlJlYWQiLCJDbGFpbVRpbWVsaW5lLlJlYWQiLCJDbGFpbU5vdGVzLlJlYWQiLCJDbGFpbUZpbmFuY2lhbHMuVXBkYXRlIiwiQ2xhaW1Ob3Rlcy5VcGRhdGUiLCJDbGFpbVN0YWtlaG9sZGVyLlVwZGF0ZSIsIkNsYWltUmVxdWlyZW1lbnRzLlVwZGF0ZSIsIkNsYWltTm90ZXMuRGVsZXRlIiwiQ2xhaW1QYXltZW50cy5SZWFkIiwiQ2xhaW1TZXR1cC5DcmVhdGUiLCJDbGFpbUNvbmZpZGVudGlhbEZpbGVzLlVwZGF0ZSIsIkNsYWltQ29uZmlkZW50aWFsRmlsZXMuUmVhZCIsIkNsYWltU3Rha2Vob2xkZXIuUmVhZCJdfQ.OpN8cV6xH5Bq20IpFkhXnVSEupgTp0nsps2pGo3ltB-zTPZmLPxl7gzPJR7YpO8rar8SOmj5fTlCOSB9eY3ull3AV2P0pahV19gvJNWbkGmU113jWQx8WScwhedo2wec4N3lGWyl8JQwP8cBRs6wowpgmsnUGBvVJQEm8P0bj6kty4heJgVapSmylL5fy0vSrUVK3HaIouN4yoh6ZXfLcAOPvvcCt0kM4aEVahpUPLeVJ4hJtfhVN7Tyx4US4lcft74wWSYRaYgqCqe1UBZBQwuyrjomNnYCmLzy6MGtCD6q07LLx6eOteFQ8S5qEChCFsonSXYrmkqKAGnXkPIcyQ";

	@Test
	public void testOktaTokenAuthentication() {
		JwtTokenCredentials credentials = JwtTokenCredentials.of(token);
		AuthenticateCredentials authenticateCredentials = AuthenticateCredentials.T.create();
		authenticateCredentials.setCredentials(credentials);

		Maybe<? extends AuthenticateCredentialsResponse> maybe = authenticateCredentials.eval(evaluator).getReasoned();

		AuthenticatedUser authenticatedUser = (AuthenticatedUser) maybe.get();
		User user = authenticatedUser.getUser();
		Assertions.assertThat(user.getName()).isEqualTo("roman.kurmanowytsch@braintribe.com");
		//@formatter:off
		Assertions.assertThat(user.getRoles().equals(Sets.set( //
				"tf-admin",
			    "Everyone",
			    "ClaimConfidentialFiles.Delete",
			    "ClaimPayments.Update",
			    "ClaimFinancials.Read",
			    "ClaimRequirements.Read",
			    "ClaimTimeline.Read",
			    "ClaimNotes.Read",
			    "ClaimFinancials.Update",
			    "ClaimNotes.Update",
			    "ClaimStakeholder.Update",
			    "ClaimRequirements.Update",
			    "ClaimNotes.Delete",
			    "ClaimPayments.Read",
			    "ClaimSetup.Create",
			    "ClaimConfidentialFiles.Update",
			    "ClaimConfidentialFiles.Read",
			    "ClaimStakeholder.Read",
			    "partner_MEDIBANK",
			    "partner_AHM",
			    "partner_FREEDOM",
			    "ClaimsAdjudicator"
				)));
		//@formatter:on

		Assertions.assertThat(authenticatedUser.getExpiryDate().equals(new Date(1640255410)));

	}
}
