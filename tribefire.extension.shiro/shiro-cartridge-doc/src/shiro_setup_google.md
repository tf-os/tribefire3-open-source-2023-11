# Setup Google Authentication

Setting up Google authentication using the Shiro cartridge requires you to configure tribefire and Google credentials.

## General

Using the Shiro cartridge allows you to integrate and use a third-party authentication service like Google to login to tribefire or applications built on tribefire. This enables you to use a third-party account to successfully create and maintain a tribefire session.

You will configure two endpoints in this procedure: tribefire and Google credentials.
> For Google Cloud documentation, see [https://cloud.google.com/docs/](https://cloud.google.com/docs/)

## Initial Setup

In this procedure, we assume you already have a running local instance of tribefire. We will integrate a Google login to your local instance. 

[](asset://tribefire.cortex.documentation:includes-doc/tribefire_quick_installation.md?INCLUDE)


1. Download the Shiro cartridge.
[](asset://tribefire.cortex.documentation:includes-doc/downloads.md?INCLUDE)
2. Open Control Center and synchronize the cartridge with your tribefire instance. 
> For more information, see [Using Control Center](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_control_center.md)
3. In Control Center, navigate to **Cartridges -> Show All** and expand the **shiro.cartridge** entry.
4. Expand the **roles** property and add the `tf-admin` property to the list. 
5. Commit your changes and synchronize your cartridge again.

## Google Authentication Setup

1. Login to `https://console.developers.google.com` and create a new project if you don't have one already.
2. Add new OAuth credentials to your project. Take a note of the `clientID` and `secret` as you will need them later.
3. Click you new project's client ID and inspect the **Restrictions** section. 
4. In the **Authorized JavaScript origins** section, add `http://localhost:8080`.
5. In the **Authorized redirect URIs** section, add `http://localhost:8080/tribefire-services/component/remote-login/auth/callback?client_name=Google`.
6. Save your changes.

> If your tribefire (or application built on tribefire) is not running on your local machine, make sure to change the `localhost:8080` part of the URLs.

## Remote Terminal Configuration

1. In Control Center, navigate to **Apps** and expand the **Remote Login Terminal** entry.
2. Mark the **createUsers** checkbox. 
3. Depending on what you want to achieve, add items to **userWhitelist** or **userBlacklist**. For example, if you want to add all users who have `braintribe.com` in their Google login, you can use the following RegEx: `^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(braintribe)\.com$)`.
4. Expand the **configuration** extry and set the value of the `unauthenticatedUrl` property. When user authentication fails, this is the URL the user is redirected to.
> User authentication is considered failed if you provide the wrong credentials or close the authentication popup.
5. If the `callbackUrl` and `unauthorizedUrl` properties are empty, set them to `https://host:port/tribefire-services/component/remote-login/auth/callback` and `https://host:port/tribefire-services/component/remote-login` respectively. 
6. Still in **configuration**, expand the `clients` entry and click `ShiroOidcGoogleClient`. 
7. Set the values of `clientId` and `secret` to the same values as in the Google console and commit your changes.
8. Navigate to **Custom Deployables** and deploy the authentication components in the following order:
    1. `Shiro Service Processor`
    2. `Fixed Roles for New Users Provider`
    3. `Remote Login Terminal`
    4. `Shiro Bootstrapping Worker`
9. In your browser, navigate to `http://localhost:8080/tribefire-services/component/remote-login` and select the **Google** link. This displays a Google authentication screen where you can log in using your Google credentials.
> Note that you can still log in to tribefire without using Google.