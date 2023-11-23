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
package com.braintribe.model.asset.natures;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.tomcat.platform.TomcatAuthenticationRealm;
import com.braintribe.model.tomcat.platform.TomcatServiceDescriptor;

public interface LocalSetupTomcatConfig extends LocalSetupConfig {

	EntityType<LocalSetupTomcatConfig> T = EntityTypes.T(LocalSetupTomcatConfig.class);

	String javaHome = "javaHome";
	String jreHome = "jreHome";

	@Description("The Tomcat server port (e.g. for startup/shutdown commands)")
	@Initializer("8005")
	Integer getServerPort();
	void setServerPort(Integer serverPort);

	@Description("Enables the AJP connector. See official tomcat documentation for AJP.")
	@Initializer("false")
	Boolean getEnableAjpConnector();
	void setEnableAjpConnector(Boolean enableAjpConnector);

	@Description("The AJP connector port. See official tomcat documentation for AJP.")
	@Initializer("8009")
	Integer getAjpPort();
	void setAjpPort(Integer ajpPort);

	@Description("The AJP connector secret. If not set, 'secretRequired' will be set to false."
			+ " For more information see official tomcat documentation for AJP.")
	String getAjpSecret();
	void setAjpSecret(String ajpPort);

	@Description("Activates redirect of HTTP port to HTTPS port. See official tomcat documentation for redirectPort.")
	@Initializer("false")
	Boolean getEnforceHttps();
	void setEnforceHttps(Boolean enforceHttps);

	@Description("Activates acceptance of SSL certificates. See official tomcat documentation for acceptSslCertificates.")
	@Initializer("true")
	Boolean getAcceptSslCertificates();
	void setAcceptSslCertificates(Boolean acceptSslCertificates);

	@Description("The path to the SSL keystore file in PKCS 12 format."
			+ " If not set, the default keystore with a (self-signed) certificate will be used.")
	Resource getSslKeystoreFile();
	void setSslKeystoreFile(Resource sslKeystoreFile);

	@Description("The password for the 'sslKeystoreFile'. By default, an encrypted password is expected. If the 'sslKeystoreFile' was generated without a password, set 'sslKeystorePassword' to empty string.")
	String getSslKeystorePassword();
	void setSslKeystorePassword(String sslKeystorePassword);

	@Description("The initial heap size of the JVM that runs tomcat.")
	@Initializer("'512m'")
	String getInitialHeapSize();
	void setInitialHeapSize(String initialHeapSize);

	@Description("The maximum heap size of the JVM that runs tomcat.")
	@Initializer("'4096m'")
	String getMaxHeapSize();
	void setMaxHeapSize(String maxHeapSize);

	@Description("Sets JAVA_HOME within the tomcat environment.")
	String getJavaHome();
	void setJavaHome(String javaHome);

	@Description("Sets JRE_HOME within the tomcat environment. Defaults to 'javaHome'.")
	String getJreHome();
	void setJreHome(String jreHome);

	@Initializer("false")
	Boolean getManagePlatformLifecycle();
	void setManagePlatformLifecycle(Boolean managePlatformLifecycle);

	@Description("The accept count. See official tomcat documentation for acceptCount.")
	Integer getAcceptCount();
	void setAcceptCount(Integer acceptCount);

	@Description("The acceptor thread count. See official tomcat documentation for acceptorThreadCount.")
	Integer getAcceptorThreadCount();
	void setAcceptorThreadCount(Integer acceptorThreadCount);

	@Description("The connection timeout in ms. See official tomcat documentation for connectionTimeout.")
	@Initializer("120000l")
	Long getConnectionTimeout();
	void setConnectionTimeout(Long connectionTimeout);

	@Description("The timeout for the upload connection in ms. See official tomcat documentation for connectionUploadTimeout.")
	Long getConnectionUploadTimeout();
	void setConnectionUploadTimeout(Long connectionUploadTimeout);

	@Description("The keep alive timeout in ms. See official tomcat documentation for keepAliveTimeout.")
	Long getKeepAliveTimeout();
	void setKeepAliveTimeout(Long keepAliveTimeout);

	@Description("The maximum number of connections. See official tomcat documentation for maxConnections.")
	Integer getMaxConnections();
	void setMaxConnections(Integer maxConnections);

	@Description("The maximum number of request worker threads. See official tomcat documentation for maxThreads.")
	@Initializer("150")
	int getMaxThreads();
	void setMaxThreads(int maxThreads);

	@Description("The shutdown command. See official tomcat documentation for shutdownCommand.")
	@Initializer("uuid()")
	String getShutdownCommand();
	void setShutdownCommand(String shutdownCommand);

	@Description("The ciphers. See official tomcat documentation for ciphers.")
	String getCiphers();
	void setCiphers(String ciphers);

	@Description("Activates headless (non-ui) mode of the JVM that runs the tomcat.")
	@Initializer("true")
	boolean getHeadless();
	void setHeadless(boolean headless);

	@Description("Defines options of the JVM that runs tomcat. Options defined here must be OS independent. Use 'windowsJvmOptions' or 'unixJvmOptions' for further OS related options.")
	String getJvmOptions();
	void setJvmOptions(String jvmOptions);

	@Description("Defines options of the JVM that runs tomcat on a Windows system. Following the 'last one wins' rule this options are added at the end of the JVM options list.")
	String getWindowsJvmOptions();
	void setWindowsJvmOptions(String windowsJvmOptions);

	@Description("Defines options of the JVM that runs tomcat on a Unix system. Following the 'last one wins' rule this options are added at the end of the JVM options list.")
	String getUnixJvmOptions();
	void setUnixJvmOptions(String unixJvmOptions);

	@Description("Configures the realm that handles tomcat authentications. See official tomcat documentation for realm. "
			+ "In case you configure a com.braintribe.model.tomcat.platform.TfRestRealm the default for 'tfsUrl' property "
			+ "is a calculated url that will use the installed tribefire as authentication delegate.")
	TomcatAuthenticationRealm getTomcatAuthenticationRealm();
	void setTomcatAuthenticationRealm(TomcatAuthenticationRealm tomcatAuthenticationRealm);

	@Override
	@Description("The directory for temporary files of the JVM (controlled by system property java.io.tmpdir). "
			+ "If set, it will be used as the default for runtime property TRIBEFIRE_TMP_DIR.")
	String getTempDir();

	@Description("A list of additional libraries to be added to the Tomcat libraries during the setup process."
			+ " This is used to inject environment specific drivers, e.g. JDBC drivers for the respective database.")
	List<Resource> getAdditionalLibraries();
	void setAdditionalLibraries(List<Resource> additionalLibraries);

	@Description("Configures the tomcat service.")
	TomcatServiceDescriptor getTomcatServiceDescriptor();
	void setTomcatServiceDescriptor(TomcatServiceDescriptor tomcatServiceDescriptor);

	@Description("The path to the rewrite configuration file. In case a file is already given, it will be overwritten. "
			+ "See official tomcat documentation for rewrite valve.")
	Resource getUrlRewriteConfigFile();
	void setUrlRewriteConfigFile(Resource urlRewriteConfigFile);

	@Initializer("false")
	boolean getEnableVirtualThreads();
	void setEnableVirtualThreads(boolean enableVirtualThreads);

}
