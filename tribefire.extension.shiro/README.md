# tribefire.extension.shiro

## Building

Run `./tb.sh .` in the `tribefire.extension.shiro` group.

## Setup

Run the following jinni command to setup a conversion server:

`./jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.shiro:shiro-setup#2.0 installationPath=<Your Path>`


### The configuration can be adapted by the following TribefireRuntime properties.

| Property           | Description      | Default      |
| :------------- | :----     | :----      |
| SHIRO_ENABLE_GOOGLE         | Indicates whether the Google authentication client should be configured.     | `true` |
| ~~SHIRO_GOOGLE_CLIENTID~~         | The plaintext Google Client ID. (see https://console.cloud.google.com/apis/credentials). This property has been deprecated as it is plaintext. Please use `SHIRO_GOOGLE_CLIENTID_ENCRYPTED` instead.      | A demo-client ID usable for `localhost`. |
| ~~SHIRO_GOOGLE_SECRET~~         | The plaintext Google Client Secret. This property has been deprecated as it is plaintext. Please use `SHIRO_GOOGLE_SECRET_ENCRYPTED` instead.       | A demo secret |
| SHIRO_GOOGLE_CLIENTID_ENCRYPTED         | The plaintext Google Client ID. (see https://console.cloud.google.com/apis/credentials).       | A demo-client ID usable for `localhost`. |
| SHIRO_GOOGLE_SECRET_ENCRYPTED         | The plaintext Google Client Secret.       | A demo secret |
| SHIRO_ENABLE_TWITTER         | Indicates whether the Twitter authentication client should be configured.      | `true` |
| ~~SHIRO_TWITTER_KEY~~         | The plaintext Twitter Key. This property has been deprecated as it is plaintext. Please use `SHIRO_TWITTER_KEY_ENCRYPTED` instead.      | A demo-key usable for `localhost`. |
| ~~SHIRO_TWITTER_SECRET~~         | The plaintext Twitter Secret. This property has been deprecated as it is plaintext. Please use `SHIRO_TWITTER_SECRET_ENCRYPTED` instead.       | A demo secret |
| SHIRO_TWITTER_KEY_ENCRYPTED         | The plaintext Twitter ID.      | A demo-client ID usable for `localhost`. |
| SHIRO_TWITTER_SECRET_ENCRYPTED         | The plaintext Twitter Secret.       | A demo secret |
| SHIRO_ENABLE_FACEBOOK         | Indicates whether the Facebook authentication client should be configured.       | `true` |
| ~~SHIRO_FACEBOOK_KEY~~         | The plaintext Facebook Key. This property has been deprecated as it is plaintext. Please use `SHIRO_FACEBOOK_KEY_ENCRYPTED` instead.      | A demo-key usable for `localhost`. |
| ~~SHIRO_FACEBOOK_SECRET~~         | The plaintext Facebook Secret. This property has been deprecated as it is plaintext. Please use `SHIRO_FACEBOOK_SECRET_ENCRYPTED` instead.       | A demo secret |
| SHIRO_FACEBOOK_KEY_ENCRYPTED         | The plaintext Facebook Key.    | A demo-client ID usable for `localhost`. |
| SHIRO_FACEBOOK_SECRET_ENCRYPTED         | The plaintext Facebook Secret.       | A demo secret |
| SHIRO_ENABLE_GITHUB         | Indicates whether the GitHub authentication client should be configured.       | `true` |
| ~~SHIRO_GITHUB_KEY~~         | The plaintext GitHub Key. This property has been deprecated as it is plaintext. Please use `SHIRO_GITHUB_KEY_ENCRYPTED` instead.      | A demo-key usable for `localhost`. |
| ~~SHIRO_GITHUB_SECRET~~         | The plaintext GitHub Secret. This property has been deprecated as it is plaintext. Please use `SHIRO_GITHUB_SECRET_ENCRYPTED` instead.       | A demo secret |
| SHIRO_GITHUB_KEY_ENCRYPTED         | The plaintext GitHub Key.      | A demo-key usable for `localhost`. |
| SHIRO_GITHUB_SECRET_ENCRYPTED         | The plaintext GitHub Secret.       | A demo secret |
| SHIRO_LOGIN_USERROLESMAP_FIELD         | Indicates which field of the user profile should be used as a key for lookup in the `MappedNewUserRoleProvider`.    | `email ` |
| SHIRO_LOGIN_USERROLESMAP         | If this is set, a `MappedNewUserRoleProvider` will be created with the configuration in this property. It must contain a `;`-separated list or user to roles assignments. Example: `.*@braintribe.com=tf-admin;.*@gmail.com=tf-user`      | `empty` |
| SHIRO_LOGIN_WHITELIST         | If configured, this contains a comma-separated list of usernames or regular expressions that define allowed user(-patterns). When this is empty, all users will be allowed.       | `empty` |
| SHIRO_LOGIN_BLACKLIST         | If configured, this contains a comma-separated list of usernames or regular expressions that define rejected user(-patterns). When this is empty, no users will be rejected.       | `empty` |
| SHIRO_LOGIN_CREATEUSERS         | If set to true, user who are not registered in the `auth` access will be automatically created. Please take special care that this does not mean that *everybody* is allowed to log in.        | `true` |
| SHIRO_PUBLIC_SERVICES_URL         | The URL of the tribefire services publicly reachable by clients.       | The value defined by `TRIBEFIRE_PUBLIC_SERVICES_URL` |
| SHIRO_CALLBACK_URL         | The publicly available callback URL as used by the external authentication scheme.       | `SHIRO_PUBLIC_SERVICES_URL`+`component/remote-login/auth/callback` |
| SHIRO_UNAUTHORIZED_URL         | The URL that unauthorized users will be redirected to.      | `SHIRO_PUBLIC_SERVICES_URL`+`component/remote-login` |
| SHIRO_UNAUTHENTICATED_URL         | The URL that unauthorized users will be redirected to.      | `empty` |
| SHIRO_REDIRECT_URL         | The URL where an authenticated/authorized user will be redirected to.     | `SHIRO_PUBLIC_SERVICES_URL` |
| SHIRO_LOGIN_DOMAIN         | An optional parameter that can be used to restrict the Google Authentication to a specific domain. Example: `braintribe.com`     | `empty` |
| SHIRO_LOGIN_CUSTOM_PARAMS         | Custom parameters that will be written into the Shiro configuration that are not covered by the standard configuration settings.     | `empty` |
| SHIRO_COOKIE_DOMAIN         | The domain assigned to cookies created by the Shiro Login Terminal.     | The value defined by `TRIBEFIRE_COOKIE_DOMAIN` |
| SHIRO_COOKIE_PATH         | The path of cookies created by the Shiro Login Terminal.     | The value defined by `TRIBEFIRE_COOKIE_PATH` |
| SHIRO_COOKIE_HTTPONLY         | Indicates whether cookies should be restricted to HTTP only. If set to false, Javascript clients may not have access to the cookie.     | The value defined by `TRIBEFIRE_COOKIE_HTTPONLY` (default: false) |


