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
package tribefire.extension.shiro.initializer.wire.contract;

import java.util.List;

import com.braintribe.model.shiro.deployment.FieldEncoding;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

/*
 * For compatibility reasons, this is not using the PropertyDefinitionsContract yet. This will be activated later.
 */
public interface ShiroRuntimePropertiesContract extends PropertyLookupContract {

	@Default("true")
	boolean SHIRO_INITIALIZE_DEFAULTS();

	@Default("true")
	boolean SHIRO_ENABLE_GOOGLE();

	@Default("CMGf5oPzkfmL5kpAtblFiqXufJQo7x0WPYY10AnREk1oJG9gqO6QX4f5Rljnf3U8Tn+gEa5I3KfG9CYTvI7tmqrwEG2ylri3Xf+MP0sgKbTTJFQQI5LFXCBCUTeRGubs4gh7+WG30Imhx8lHH0eaZUMVS2g=")
	@Decrypt
	String SHIRO_GOOGLE_CLIENTID_ENCRYPTED();
	@Default("GRmg4lWW6TDQUBVMLKjUTPddEG8FWoAi3no71iOnIQiRDXFUMaqCFxV044TfM1+VDprHo/GLQx6U052zcXPwN5agy4A=")
	@Decrypt
	String SHIRO_GOOGLE_SECRET_ENCRYPTED();

	@Default("true")
	boolean SHIRO_ENABLE_AZUREAD();

	@Default("BQBP+6s3fzqQM0QnchNK2Gxd6ZOqGWx1kTXAVv6UEKqhKaBpqqBImiDsDFaFLbgYFzyTrJcVr9FI05BpKXIohrQ04fYTkR0P9FPDMjJlxT4CVZpQ")
	@Decrypt
	String SHIRO_AZUREAD_CLIENTID_ENCRYPTED();
	@Default("rzakHvOwv/W833L925UOdAZHYmqk7DeCN+aiTmucPUUhPa/IXVGNEWeb+rYrrqKk8YXP2oxxcDbzvp2UETpTyaMLHNR7BMTXusYBvZzbg3NbfWzY")
	@Decrypt
	String SHIRO_AZUREAD_SECRET_ENCRYPTED();
	@Default("d5550702-41c0-4023-8b0e-0dbe7e5b4900")
	String SHIRO_AZUREAD_TENANT();

	@Default("true")
	boolean SHIRO_ENABLE_TWITTER();

	@Default("t9dvxPIlO3YltuL42bEYFp2D04zKtBr9FPlxKns99htRkwaBHgSsAJSgnhK31R7jJSwFYpVphABTBXuKZ5WA2iunUqw=")
	@Decrypt
	String SHIRO_TWITTER_KEY_ENCRYPTED();
	@Default("ycaGjIzWBtBzJNSQQXmK8aIi+bMBYkE2AnmKwVTuooX+iLpRGYTzITHog5UCeIAZkdhITuTqUqMPVwIe79UrY50NVAqET0TTBvhhSwByiWOtUvOKNPmsmRasO5Mwl1FXjgIGuw==")
	@Decrypt
	String SHIRO_TWITTER_SECRET_ENCRYPTED();

	@Default("true")
	boolean SHIRO_ENABLE_FACEBOOK();

	@Default("LjZTXflo2gVGBlKVv8ZS0ZTczZJY7QXZwqrUfYZ4kDPEKrKKK3XGH6qWh6kZDezQ1n5X5Q==")
	@Decrypt
	String SHIRO_FACEBOOK_KEY_ENCRYPTED();
	@Default("KB6tZE4voAun1XTINrxWeAozn4eFdBhKo4sPTZ3XettWQQrYfAtXoOanu0pUbC+NMVHCQSW33wz9sstj9pPwcrzY7G/FgLWoyPixEj0ffzIlsbZW")
	@Decrypt
	String SHIRO_FACEBOOK_SECRET_ENCRYPTED();

	@Default("true")
	boolean SHIRO_ENABLE_GITHUB();

	@Default("dC4mpndQ87G2UKpdH1zBnKn5BvTxc+ugSLswsRrOY4bjUhzD5nCMqJTh7LO15XMKNYP2GuVhT4cldQNDfl/ndleLlD4=")
	@Decrypt
	String SHIRO_GITHUB_KEY_ENCRYPTED();
	@Default("xHBS59CgAe+8epjU/cuXyD77m04KI4QlBPavFQ60bI2vHovhYxZ7s8oH6js5R2hHR8xCoSoIC0ySQaDlIAb+6yuUsPEDEwYnZz53UV2MoDDaXwxo")
	@Decrypt
	String SHIRO_GITHUB_SECRET_ENCRYPTED();

	@Default("true")
	boolean SHIRO_ENABLE_COGNITO();

	@Default("GIgZHA5YSZgqiyvg3gISSdpis67yQmlupkPVwAarRsw/t3c1bN9cJE9oHCChpHV9yCf5ujLzlsE+5tJTCDxszNyzDA8=")
	@Decrypt
	String SHIRO_COGNITO_CLIENTID_ENCRYPTED();
	@Default("LulMfuaYC156U5S99yb1f/96GD+1OwF+xBpsXqj5KXWK6J8VFcGrUvOBDCnHMFMqNECIF7yLrtwAxdNtaSG2GuH8HzDO7qTO9N79/Jvln8t3yOKHhbTz2ft7ZPhRR5lirY5y/Q==")
	@Decrypt
	String SHIRO_COGNITO_SECRET_ENCRYPTED();
	@Default("eu-central-1")
	String SHIRO_COGNITO_REGION();
	@Default("eu-central-1_ghqPrcf84")
	String SHIRO_COGNITO_USERPOOL_ID();
	@Default("false")
	boolean SHIRO_COGNITO_EXCLUSIVE_ROLE_PROVIDER();

	@Default("true")
	boolean SHIRO_ENABLE_OKTA();

	@Default("/9HI+ThtTqdpZBjs32czx8uuPxHSOhhRKyL4MVpb5E8rbFG+q3oqzXIMKBvtqXYjcH+DSupRzqTvc8r/7f2pZCmkekE=")
	@Decrypt
	String SHIRO_OKTA_CLIENTID_ENCRYPTED();
	@Default("eUfGQ55rxWObFdkJtQChNMp46I2WEw6c2Pi6RAQPVN+o1hNUFrQIEYTpmBHTw9MmDLLm1Chzk1v2rGaUs0ICi+idfIHbxfMWi4jS+c+obRYyxGRK")
	@Decrypt
	String SHIRO_OKTA_SECRET_ENCRYPTED();
	@Default("https://braintribe.okta.com/.well-known/openid-configuration")
	String SHIRO_OKTA_DISCOVERY_URL();
	@Default("false")
	boolean SHIRO_OKTA_EXCLUSIVE_ROLE_PROVIDER();
	String SHIRO_OKTA_ROLES_FIELD();
	@Default("CSV")
	FieldEncoding SHIRO_OKTA_ROLES_FIELD_ENCODING();

	@Default("true")
	boolean SHIRO_ENABLE_INSTAGRAM();

	@Default("/f+xt43A1FeFN9wsN0ffD04D+LzVY6QHXL2XktWRa6TeCOW3DdCvruDrb4LM15wIr4xy/g==")
	@Decrypt
	String SHIRO_INSTAGRAM_CLIENTID_ENCRYPTED();
	@Default("a3dulWEwFmlb+iTtRSjTuSJVvAdrpYzszcEP0WqxhS+vHXB8KYK6jq4xA0eaB63cFTUxLBiX9RCe073O9wnzIzMJ8rhFOCW9JHccP7wbLAm8h8qS")
	@Decrypt
	String SHIRO_INSTAGRAM_SECRET_ENCRYPTED();

	@Default("email")
	List<String> SHIRO_LOGIN_USERROLESMAP_FIELD();

	String SHIRO_LOGIN_USERROLESMAP();

	String SHIRO_LOGIN_ACCEPTLIST();
	String SHIRO_LOGIN_BLOCKLIST();

	@Default("true")
	boolean SHIRO_LOGIN_CREATEUSERS();

	String SHIRO_PUBLIC_SERVICES_URL(String defaultValue);
	String SHIRO_CALLBACK_URL(String defaultValue);
	String SHIRO_UNAUTHORIZED_URL(String defaultValue);
	String SHIRO_UNAUTHENTICATED_URL();
	String SHIRO_REDIRECT_URL(String defaultValue);
	@Default("false")
	boolean SHIRO_ADD_SESSION_PARAMETER_ON_REDIRECT();

	String SHIRO_LOGIN_DOMAIN();
	String SHIRO_LOGIN_CUSTOM_PARAMS();

	@Default("true")
	boolean SHIRO_SHOW_STANDARD_LOGIN_FORM();
	@Default("false")
	boolean SHIRO_SHOW_TEXT_LINKS();

	@Default("true")
	boolean SHIRO_OBFUSCATE_LOG_OUTPUT();

}
