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
package com.braintribe.model.processing.email.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import com.braintribe.model.email.deployment.connection.Pop3Connector;
import com.braintribe.model.email.deployment.connection.RetrieveConnector;

public class MailboxContext {

	protected Session session = null;
	protected Store store = null;
	protected Folder folder = null;

	protected Properties serverProperties = null;
	protected String protocol = null;
	protected String host = null;
	protected String port = null;
	protected String folderName = null;
	protected String username = null;
	protected String password = null;

	public MailboxContext(Properties serverProperties, String protocol, String host, String port, String folderName, String username,
			String password) {
		this.serverProperties = serverProperties;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.folderName = folderName;
		this.username = username;
		this.password = password;
	}

	public MailboxContext(RetrieveConnector connection, String folder) {
		if (connection instanceof Pop3Connector) {
			this.protocol = "pop3";
		} else {
			this.protocol = "imap";
		}

		this.serverProperties = createServerProperties(connection, protocol, false);
		this.host = connection.getHost();
		this.port = Integer.toString(connection.getPort());
		this.folderName = folder;
		this.username = connection.getUser();
		this.password = connection.getPassword();
	}

	private Properties createServerProperties(RetrieveConnector connector, String proto, boolean isDebug) {
		String cHost = connector.getHost();
		String cPort = Integer.toString(connector.getPort());

		Properties properties = new Properties();
		// server settings
		properties.put(String.format("mail.%s.host", proto), cHost);
		properties.put(String.format("mail.%s.port", proto), cPort);
		properties.put("mail.store.protocol", proto);
		properties.put(String.format("mail.%s.fetchsize", proto), "4194304"); // 4 mbs
		// SSL settings
		properties.put(String.format("mail.%s.ssl.enable", proto), "true");
		properties.put(String.format("mail.%s.socketFactory.class", proto), "javax.net.ssl.SSLSocketFactory");
		properties.put(String.format("mail.%s.socketFactory.fallback", proto), "false");
		// misc
		properties.put("mail.debug", Boolean.toString(isDebug));
		return properties;
	}
	public void connect() throws MessagingException {
		connect(null);
	}

	public void connect(Integer folderType) throws MessagingException {
		session = Session.getInstance(serverProperties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		store = getSession().getStore(protocol);

		getStore().connect(username, password);
		folder = getStore().getFolder(folderName);
		if (folderType == null) {
			getFolder().open(Folder.READ_ONLY);
		} else {
			getFolder().open(folderType);
		}
	}

	public Message[] searchMessages() throws MessagingException {
		return searchMessages(null);
	}

	public Message[] searchMessages(SearchTerm searchTerm) throws MessagingException {
		Message[] messages = null;
		Folder searchFolder = getFolder();
		if (searchTerm == null) {
			messages = searchFolder.getMessages();
		} else {
			messages = searchFolder.search(searchTerm);
		}
		if (messages == null) {
			messages = new Message[0];
		}
		return messages;
	}

	public void close(boolean expunge) throws MessagingException {
		if (this.getFolder() != null) {
			this.getFolder().close(expunge);
			this.folder = null;
		}
		if (this.getStore() != null) {
			this.getStore().close();
			this.store = null;
		}
		this.session = null;
	}

	public Session getSession() {
		return session;
	}

	public Store getStore() {
		return store;
	}

	public Folder getFolder() {
		return folder;
	}
	public Folder getFolder(String name, boolean createIfNonExistent) throws MessagingException {
		Folder namedFolder = getStore().getFolder(name);
		if (!namedFolder.exists() && createIfNonExistent) {
			namedFolder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
		}
		return namedFolder;
	}

}
