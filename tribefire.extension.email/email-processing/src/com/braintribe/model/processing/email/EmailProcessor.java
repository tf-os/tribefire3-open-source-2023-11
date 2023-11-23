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
package com.braintribe.model.processing.email;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.email.data.Email;
import com.braintribe.model.email.data.ReceivedEmail;
import com.braintribe.model.email.data.ReceivedImapEmail;
import com.braintribe.model.email.data.Recipient;
import com.braintribe.model.email.data.Sender;
import com.braintribe.model.email.deployment.connection.EmailConnector;
import com.braintribe.model.email.deployment.connection.Pop3Connector;
import com.braintribe.model.email.deployment.connection.RetrieveConnector;
import com.braintribe.model.email.deployment.connection.SendConnector;
import com.braintribe.model.email.deployment.connection.SmtpConnector;
import com.braintribe.model.email.service.CheckConnections;
import com.braintribe.model.email.service.ConnectionCheckResult;
import com.braintribe.model.email.service.ConnectionCheckResultEntry;
import com.braintribe.model.email.service.ConnectionType;
import com.braintribe.model.email.service.DeleteEmail;
import com.braintribe.model.email.service.DeletedEmail;
import com.braintribe.model.email.service.EmailServiceRequest;
import com.braintribe.model.email.service.EmailServiceResult;
import com.braintribe.model.email.service.MarkEmailUnread;
import com.braintribe.model.email.service.MarkedEmailAsUnread;
import com.braintribe.model.email.service.MoveEmailToFolder;
import com.braintribe.model.email.service.MovedEmailToFolder;
import com.braintribe.model.email.service.ReceiveEmails;
import com.braintribe.model.email.service.ReceivedEmailPostProcessing;
import com.braintribe.model.email.service.ReceivedEmails;
import com.braintribe.model.email.service.SendEmail;
import com.braintribe.model.email.service.SentEmail;
import com.braintribe.model.email.service.reason.ConfigurationMissing;
import com.braintribe.model.email.service.reason.DeleteMailFailed;
import com.braintribe.model.email.service.reason.MailNotFound;
import com.braintribe.model.email.service.reason.MailServerConnectionError;
import com.braintribe.model.email.service.reason.MailServerError;
import com.braintribe.model.email.service.reason.MoveMailFailed;
import com.braintribe.model.email.service.reason.PostProcessingError;
import com.braintribe.model.email.service.reason.PrepareOutgoingMailError;
import com.braintribe.model.email.service.reason.RetrieveConnectorMissing;
import com.braintribe.model.email.service.reason.SendConnectorMissing;
import com.braintribe.model.email.service.reason.SetFlagFailed;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.email.cache.MailerCache;
import com.braintribe.model.processing.email.cache.MailerContext;
import com.braintribe.model.processing.email.util.MailboxContext;
import com.braintribe.model.processing.email.util.ResourceDataSource;
import com.braintribe.model.processing.email.util.SearchTermParserTools;
import com.braintribe.model.processing.email.util.SmtpUtil;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.ServiceProcessors;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;
import com.sun.mail.imap.IMAPFolder; //NOSONAR: this is legit as it is part of the JavaMail API
import com.sun.mail.imap.IMAPMessage; //NOSONAR: this is legit as it is part of the JavaMail API

public class EmailProcessor implements ServiceProcessor<EmailServiceRequest, EmailServiceResult> {

	private static final Logger logger = Logger.getLogger(EmailProcessor.class);

	private Supplier<? extends PersistenceGmSession> cortexSessionProvider;
	private ClassLoader moduleClassLoader;

	private MailerCache mailerCache;

	protected StreamPipeFactory pipeStreamFactory;
	private ExecutorService healthCheckExecutor;

	private static FlagTerm unreadFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

	private static final String INBOX_FOLDER = "Inbox";

	private static final String TEXT_PLAIN = "text/plain";
	private static final String TEXT_HTML = "text/html";

	private final ServiceProcessor<ServiceRequest, EmailServiceResult> ddsaDispatcher = ServiceProcessors.dispatcher(config -> {
		config.registerReasoned(SendEmail.T, this::sendEmail);
		config.registerReasoned(ReceiveEmails.T, this::receiveEmails);
		config.registerReasoned(MarkEmailUnread.T, this::markEmailUnread);
		config.registerReasoned(MoveEmailToFolder.T, this::moveEmailToFolder);
		config.registerReasoned(DeleteEmail.T, this::deleteEmail);
		config.registerReasoned(CheckConnections.T, this::checkConnections);
	});

	@Override
	public EmailServiceResult process(ServiceRequestContext requestContext, EmailServiceRequest request) {
		Instant start = NanoClock.INSTANCE.instant();
		EmailServiceResult result = ddsaDispatcher.process(requestContext, request);
		result.setDurationInMs(Duration.between(start, NanoClock.INSTANCE.instant()).toMillis());
		return result;
	}

	private Maybe<ConnectionCheckResult> checkConnections(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			CheckConnections request) {

		PersistenceGmSession cortexSession = cortexSessionProvider.get();
		Set<String> connectorIds = request.getConnectorIds();

		List<RetrieveConnector> retrieveConnectors = (List<RetrieveConnector>) getDeployedConnectors(RetrieveConnector.T, cortexSession,
				connectorIds);
		List<SendConnector> sendConnectors = (List<SendConnector>) getDeployedConnectors(SendConnector.T, cortexSession, connectorIds);

		if (retrieveConnectors.isEmpty() && sendConnectors.isEmpty()) {
			return Reasons.build(ConfigurationMissing.T).text(
					connectorIds.isEmpty() ? "Could not find a single connector to check." : "Could not find a connector with Id(s): " + connectorIds)
					.toMaybe();
		} else {

			ConnectionCheckResult result = ConnectionCheckResult.T.create();

			List<Future<ConnectionCheckResultEntry>> futures = new ArrayList<>();

			sendConnectors.forEach(c -> futures.add(healthCheckExecutor.submit(() -> checkSendConnector(c))));
			retrieveConnectors.forEach(c -> futures.add(healthCheckExecutor.submit(() -> checkRetrieveConnector(c))));

			for (Future<ConnectionCheckResultEntry> f : futures) {
				try {
					ConnectionCheckResultEntry entry = f.get();
					result.getEntries().add(entry);
				} catch (Exception e) {
					logger.error("Error while waiting for check result.", e);
				}
			}

			return Maybe.complete(result);
		}
	}

	private List<? extends EmailConnector> getDeployedConnectors(EntityType<? extends EmailConnector> type, PersistenceGmSession cortexSession,
			Set<String> externalIds) {

		final EntityQuery query;
		if (externalIds.isEmpty()) {
			//@formatter:off
			query = EntityQueryBuilder.from(type)
					.where()
					.property(EmailConnector.deploymentStatus).eq(DeploymentStatus.deployed)
					.done();
			//@formatter:on
		} else {
			//@formatter:off
			query = EntityQueryBuilder.from(type)
					.where()
						.conjunction()
							.property(EmailConnector.deploymentStatus).eq(DeploymentStatus.deployed)
							.property(EmailConnector.externalId).in(externalIds)
						.close()
					.done();
			//@formatter:on			
		}
		List<EmailConnector> list = cortexSession.query().entities(query).list();

		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	private ConnectionCheckResultEntry checkRetrieveConnector(RetrieveConnector connection) {
		ConnectionCheckResultEntry entry = ConnectionCheckResultEntry.T.create();
		entry.setName(connection.getName());
		entry.setExternalId(connection.getExternalId());
		entry.setType(ConnectionType.RECEIVER);

		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(moduleClassLoader);

			MailboxContext mailboxContext = new MailboxContext(connection, "Inbox");
			try {
				mailboxContext.connect(Folder.READ_ONLY);
				entry.setSuccess(true);
				entry.setDetails("Connected to " + connection.getHost());
			} catch (Exception e) {
				logger.error(() -> "Error while checking retrieve connection " + connection, e);
				entry.setSuccess(false);
				entry.setErrorMessage(e.getMessage());
			}
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}

		return entry;
	}

	private ConnectionCheckResultEntry checkSendConnector(SendConnector connection) {

		ConnectionCheckResultEntry entry = ConnectionCheckResultEntry.T.create();
		entry.setName(connection.getName());
		entry.setExternalId(connection.getExternalId());
		entry.setType(ConnectionType.SENDER);

		if (!(connection instanceof SmtpConnector)) {
			entry.setSuccess(false);
			entry.setDetails("Unsupported send connector: " + connection.getExternalId());
			return entry;
		}
		SmtpConnector smtpConnector = (SmtpConnector) connection;
		try {
			MailerContext mailerContext = mailerCache.getMailer(smtpConnector);
			Mailer mailer = mailerContext.getMailer();
			mailer.testConnection();

			entry.setSuccess(true);
			entry.setDetails("Connected to " + smtpConnector.getSmtpHostName());

		} catch (Exception e) {
			logger.error(() -> "Error while checking SMTP connection " + connection, e);
			entry.setSuccess(false);
			entry.setErrorMessage("Could not connect to " + smtpConnector.getSmtpHostName() + ": " + e.getMessage());
		}

		return entry;
	}

	private Maybe<DeletedEmail> deleteEmail(@SuppressWarnings("unused") ServiceRequestContext requestContext, DeleteEmail request) {

		return executeWithMailboxContext(request.getConnectorId(), mailboxContext -> {

			DeletedEmail response = DeletedEmail.T.create();

			Message[] messages;
			try {
				messages = mailboxContext.searchMessages(new MessageIDTerm(request.getEmailId()));
			} catch (Exception e) {
				return exceptionToReason(MailNotFound.T, e, "Failed to fetch email with Id " + request.getEmailId() + " from the email server");
			}

			if (messages.length == 0) {
				response.setEmailFound(false);
				response.setEmailDeleted(false);
			} else {
				response.setEmailFound(true);
				try {
					mailboxContext.getFolder().setFlags(messages, new Flags(Flags.Flag.DELETED), true);
					mailboxContext.getFolder().expunge();
					response.setEmailDeleted(true);
				} catch (Exception e) {
					return exceptionToReason(DeleteMailFailed.T, e, "Failed to delete email with Id " + request.getEmailId());
				}
			}

			return Maybe.complete(response);
		});

	}

	private <T extends EmailServiceResult> Maybe<T> executeWithMailboxContext(String connectorId, Function<MailboxContext, Maybe<T>> runnable) {

		Maybe<RetrieveConnector> connectorMaybe = getSystemSessionBasedConnector(RetrieveConnector.T, connectorId);
		if (!connectorMaybe.isSatisfied()) {
			return connectorMaybe.whyUnsatisfied().asMaybe();
		}
		RetrieveConnector connector = connectorMaybe.get();

		MailboxContext mailboxContext = null;
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(moduleClassLoader);
			mailboxContext = new MailboxContext(connector, INBOX_FOLDER);
			try {
				mailboxContext.connect(Folder.READ_WRITE);
			} catch (Exception e) {
				return exceptionToReason(MailServerConnectionError.T, e, "Failed to connect to the email server with connection: " + connector);
			}

			return runnable.apply(mailboxContext);

		} finally {
			if (mailboxContext != null) {
				try {
					mailboxContext.close(false);
				} catch (Exception e) {
					logger.error("Failed to close the connection to the email server", e);
				}
			}

			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	private Maybe<MarkedEmailAsUnread> markEmailUnread(@SuppressWarnings("unused") ServiceRequestContext requestContext, MarkEmailUnread request) {

		return executeWithMailboxContext(request.getConnectorId(), mailboxContext -> {

			MarkedEmailAsUnread response = MarkedEmailAsUnread.T.create();

			Message[] messages;
			try {
				messages = mailboxContext.searchMessages(new MessageIDTerm(request.getEmailId()));
			} catch (Exception e) {
				return exceptionToReason(MailNotFound.T, e, "Failed to fetch email with Id " + request.getEmailId() + " from the email server");
			}

			if (messages.length == 0) {
				response.setEmailFound(false);
				response.setEmailMarkedUnread(false);
			} else {
				response.setEmailFound(true);
				try {
					mailboxContext.getFolder().setFlags(messages, new Flags(Flags.Flag.SEEN), false);
					response.setEmailMarkedUnread(true);
				} catch (Exception e) {
					return exceptionToReason(SetFlagFailed.T, e, "Failed to mark email with ID " + request.getEmailId() + " as unread");
				}
			}

			return Maybe.complete(response);
		});

	}

	private Maybe<MovedEmailToFolder> moveEmailToFolder(@SuppressWarnings("unused") ServiceRequestContext requestContext, MoveEmailToFolder request) {

		return executeWithMailboxContext(request.getConnectorId(), mailboxContext -> {

			MovedEmailToFolder response = MovedEmailToFolder.T.create();

			Message[] messages;
			try {
				messages = mailboxContext.searchMessages(new MessageIDTerm(request.getEmailId()));
			} catch (Exception e) {
				return exceptionToReason(MailNotFound.T, e, "Failed to fetch email with Id " + request.getEmailId() + " from the email server");
			}

			if (messages.length == 0) {
				response.setEmailFound(false);
				response.setEmailMoved(false);
			} else {
				response.setEmailFound(true);
				try {
					Folder folder = mailboxContext.getFolder();
					if (folder instanceof IMAPFolder) {
						IMAPFolder imapFolder = (IMAPFolder) folder;

						Folder targetFolder = mailboxContext.getFolder(request.getTargetFolder(), true);

						imapFolder.moveMessages(messages, targetFolder);
						response.setEmailMoved(true);
					} else {
						response.setEmailMoved(false);
					}
				} catch (Exception e) {
					return exceptionToReason(MoveMailFailed.T, e,
							"Failed to move email with Id " + request.getEmailId() + " to folder " + request.getTargetFolder());
				}
			}

			return Maybe.complete(response);
		});

	}

	protected Maybe<SentEmail> sendEmail(@SuppressWarnings("unused") ServiceRequestContext requestContext, SendEmail request) {

		Email email = request.getEmail();
		SentEmail result = SentEmail.T.create();

		// to get the password we need to query the connection again with system session...
		Maybe<SendConnector> connectorMaybe = getSystemSessionBasedConnector(SendConnector.T, request.getConnectorId());
		if (!connectorMaybe.isSatisfied()) {
			return connectorMaybe.whyUnsatisfied().asMaybe();
		}
		SendConnector emailTransmissionConnectorSystem = connectorMaybe.get();

		String connectorId = emailTransmissionConnectorSystem.getExternalId();

		if (emailTransmissionConnectorSystem instanceof SmtpConnector) {
			SmtpConnector emailSmtpConnectorSystem = (SmtpConnector) emailTransmissionConnectorSystem;

			final org.simplejavamail.api.email.Email resultingEmail;
			try {
				resultingEmail = generateOutgoingMail(email, emailSmtpConnectorSystem);
			} catch (Exception e) {
				return exceptionToReason(PrepareOutgoingMailError.T, e, "Could not prepare outgoing email.");
			}

			try {
				String messageId = sendEmail(resultingEmail, emailSmtpConnectorSystem);
				result.setMessageId(messageId);
			} catch (Exception e) {
				return exceptionToReason(MailServerError.T, e, "Could not send eMail " + email + " to: " + email.getToList() + " cc: "
						+ email.getCcList() + " bcc: " + email.getBccList() + " connection: " + connectorId);
			}
		} else {
			return Reasons.build(UnsupportedOperation.T).text("EmailTransmissionConnector: " + connectorId + " not supported!").toMaybe();
		}

		return Maybe.complete(result);
	}

	private org.simplejavamail.api.email.Email generateOutgoingMail(Email email, SmtpConnector smtpConnection) {

		boolean sendAsync = smtpConnection.getSendAsync() != null ? smtpConnection.getSendAsync().booleanValue() : false;

		List<Resource> attachments = email.getAttachments();
		List<Resource> inlineAttachments = email.getInlineAttachments();

		String htmlBody = email.getHtmlBody();
		String textBody = email.getTextBody();

		EmailPopulatingBuilder emailBuilder = EmailBuilder.startingBlank();
		SmtpUtil.enrichRecipients(email, emailBuilder, smtpConnection);
		emailBuilder.withSubject(email.getSubject());
		if (sendAsync) {
			emailBuilder.fixingMessageId("<prov." + RandomTools.newStandardUuid() + "@localhost>");
		}
		if (textBody != null) {
			emailBuilder.withPlainText(textBody);
		}
		if (htmlBody != null) {
			emailBuilder.withHTMLText(htmlBody);
		}

		addAttachments(emailBuilder, attachments, sendAsync);
		addInlineAttachments(emailBuilder, inlineAttachments, sendAsync);

		org.simplejavamail.api.email.Email resultingEmail = emailBuilder.buildEmail();

		return resultingEmail;
	}

	private void addAttachments(EmailPopulatingBuilder emailBuilder, List<Resource> attachments, boolean asyncSend) {
		attachments.forEach(resource -> {
			ResourceDataSource dataSource = asyncSend ? new ResourceDataSource(pipeStreamFactory, resource) : new ResourceDataSource(resource);
			emailBuilder.withAttachment(resource.getName(), dataSource);
		});
	}
	private void addInlineAttachments(EmailPopulatingBuilder emailBuilder, List<Resource> attachments, boolean asyncSend) {
		attachments.forEach(resource -> {
			ResourceDataSource dataSource = asyncSend ? new ResourceDataSource(pipeStreamFactory, resource) : new ResourceDataSource(resource);
			emailBuilder.withEmbeddedImage(resource.getName(), dataSource);
		});
	}

	private String sendEmail(final org.simplejavamail.api.email.Email email, SmtpConnector connection) {

		try {
			MailerContext mailerContext = mailerCache.getMailer(connection);

			if (mailerContext.getSendAsync()) {
				AsyncResponse asyncResponse = mailerContext.getMailer().sendMail(email, true);
				asyncResponse.onException(e -> {
					logger.error("Error while trying to send email " + email + " asynchronously.", e);
				});
				asyncResponse.onSuccess(() -> {
					logger.debug(() -> "Successfully sent email " + email.getRecipients());
				});
			} else {
				mailerContext.getMailer().sendMail(email);
			}

			return email.getId();

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not send message via connection " + connection.getExternalId());
		}

	}

	private Maybe<ReceivedEmails> receiveEmails(@SuppressWarnings("unused") ServiceRequestContext requestContext, ReceiveEmails request) {

		return executeWithMailboxContext(request.getConnectorId(), mailboxContext -> {

			Maybe<RetrieveConnector> connectorMaybe = getSystemSessionBasedConnector(RetrieveConnector.T, request.getConnectorId());
			if (!connectorMaybe.isSatisfied()) {
				return connectorMaybe.whyUnsatisfied().asMaybe();
			}
			RetrieveConnector connector = connectorMaybe.get();

			ReceivedEmails response = ReceivedEmails.T.create();

			SearchTerm searchTerm = createSearchTerm(request);
			Message[] messages;
			try {
				messages = mailboxContext.searchMessages(searchTerm);
			} catch (Exception e) {
				return exceptionToReason(MailServerError.T, e, "Failed to fetch emails from the email server");
			}

			int messagesProcessed = Math.min(request.getMaxEmailCount() != null ? request.getMaxEmailCount() : messages.length, messages.length);
			List<Message> receivedMessages = new ArrayList<>();

			for (int i = 0; i < messagesProcessed; i++) {
				Message msg = messages[i];
				try {
					ReceivedEmail email = processMessage(msg);
					response.getReceivedEmails().add(email);

					receivedMessages.add(msg);

				} catch (MessagingException | IOException e) {
					try {
						mailboxContext.getFolder().setFlags(messages, new Flags(Flags.Flag.SEEN), false);
					} catch (MessagingException e1) {
						e.addSuppressed(e1);
					}
					return exceptionToIncomplete(MailServerError.T, response, e, "Error while trying to receive emails.");
				}
			}
			try {
				postProcess(request, mailboxContext, receivedMessages, connector);
			} catch (Exception e) {
				return exceptionToIncomplete(PostProcessingError.T, response, e, "Error during post-processing after receiving emails.");
			}

			return Maybe.complete(response);
		});

	}

	private SearchTerm createSearchTerm(ReceiveEmails request) {
		String expr = request.getSearchExpression();
		if (StringTools.isBlank(expr)) {
			if (request.getUnreadOnly()) {
				return unreadFlagTerm;
			} else {
				return null;
			}
		}
		logger.debug(() -> "Parsing search expression: " + expr);

		SearchTerm searchTerm = SearchTermParserTools.parseSearchTerm("(" + expr + ")");

		if (request.getUnreadOnly()) {

			if (searchTerm != null) {
				SearchTerm[] subs = new SearchTerm[2];
				subs[0] = unreadFlagTerm;
				subs[1] = searchTerm;
				AndTerm and = new AndTerm(subs);
				return and;
			} else {
				return unreadFlagTerm;
			}
		}

		return searchTerm;
	}

	private void postProcess(ReceiveEmails request, MailboxContext mailboxContext, List<Message> receivedMessages, RetrieveConnector connector)
			throws MessagingException {

		String protocol = null;
		if (connector instanceof Pop3Connector) {
			protocol = "pop3";
		} else {
			protocol = "imap";
		}

		final ReceivedEmailPostProcessing postProcessing = request.getPostProcessing();
		if (postProcessing == null || postProcessing == ReceivedEmailPostProcessing.NOP) {
			return;
		}
		switch (postProcessing) {
			case DELETE:
				for (Message msg : receivedMessages) {
					msg.setFlag(Flags.Flag.DELETED, true);
				}
				if (protocol.equals("imap")) {
					mailboxContext.getFolder().expunge();
				}
				break;
			case MARK_READ:
				for (Message msg : receivedMessages) {
					msg.setFlag(Flags.Flag.SEEN, true);
				}
				break;
			default:
				break;
		}
	}

	private ReceivedEmail processMessage(Message msg) throws MessagingException, IOException {
		final ReceivedEmail email;
		if (msg instanceof IMAPMessage) {
			ReceivedImapEmail imapMessage = ReceivedImapEmail.T.create();
			email = imapMessage;

			IMAPMessage imapMsg = (IMAPMessage) msg;
			Folder folder = imapMsg.getFolder();
			if (folder != null) {
				imapMessage.setImapFolder(folder.getFullName());
			}
		} else {
			email = ReceivedEmail.T.create();
		}
		if (msg instanceof MimeMessage) {
			MimeMessage mimeMessage = (MimeMessage) msg;
			email.setId(mimeMessage.getMessageID());
		} else {
			String[] header = msg.getHeader("Message-ID");
			if (header != null && header.length == 1) {
				email.setId(header[0]);
			}
		}

		email.setEml(createEml(msg));
		email.setSubject(msg.getSubject());
		email.setFromList(getSenders(msg));
		email.setToList(getRecipients(msg, RecipientType.TO));
		email.setCcList(getRecipients(msg, RecipientType.CC));
		email.setBccList(getRecipients(msg, RecipientType.BCC));
		email.setReceivedDate(msg.getReceivedDate());
		acquireMailBody(email, msg);
		Object content = msg.getContent();
		email.setAttachments(getAttachments(content, Part.ATTACHMENT));
		email.setInlineAttachments(getAttachments(content, Part.INLINE));
		email.setSentDate(msg.getSentDate());
		email.setReplyToList(getRecipients(msg.getReplyTo()));

		return email;
	}

	private void acquireMailBody(ReceivedEmail email, Message msg) throws IOException, MessagingException {
		Object content = msg.getContent();

		if (msg.isMimeType(TEXT_PLAIN)) {
			email.setTextBody(content.toString());
		} else if (msg.isMimeType(TEXT_HTML)) {
			email.setHtmlBody(content.toString());
		} else if (msg.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) content;
			acquireMailBodyFromMultiPart(email, mimeMultipart);
		} else {
			throw new IllegalArgumentException("Unable to parse email body: " + content);
		}

		if (email.getTextBody() == null && email.getHtmlBody() != null) {
			try {
				String text = org.jsoup.Jsoup.parse(email.getHtmlBody()).text();
				email.setTextBody(text);
			} catch (Exception e) {
				logger.debug(() -> "Could not extract text from html body.", e);
			}
		}

	}

	private void acquireMailBodyFromMultiPart(ReceivedEmail email, MimeMultipart mimeMultipart) throws MessagingException, IOException {
		if (mimeMultipart == null) {
			return;
		}
		int count = mimeMultipart.getCount();
		if (count == 0) {
			throw new IllegalArgumentException("Multipart with no body parts not supported.");
		}
		boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
		if (multipartAlt) {
			// alternatives appear in an order of increasing
			// faithfulness to the original content. Customize as req'd.
			for (int i = count - 1; i >= 0; --i) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				acquireMailBodyFromPart(email, bodyPart);
			}
		}

		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			acquireMailBodyFromPart(email, bodyPart);
		}
	}

	private void acquireMailBodyFromPart(ReceivedEmail email, BodyPart bodyPart) throws MessagingException, IOException {
		if (bodyPart == null) {
			return;
		}
		if (bodyPart.isMimeType(TEXT_PLAIN) && email.getTextBody() == null) {
			String textBody = (String) bodyPart.getContent();
			email.setTextBody(textBody);
		} else if (bodyPart.isMimeType(TEXT_HTML) && email.getHtmlBody() == null) {
			String htmlBody = (String) bodyPart.getContent();
			email.setHtmlBody(htmlBody);
		} else if (bodyPart.getContent() instanceof MimeMultipart) {
			acquireMailBodyFromMultiPart(email, (MimeMultipart) bodyPart.getContent());
		}
	}

	private String getTextFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
		String result = "";
		if (bodyPart.isMimeType(TEXT_PLAIN)) {
			result = (String) bodyPart.getContent();
		} else if (bodyPart.isMimeType(TEXT_HTML)) {
			String html = (String) bodyPart.getContent();
			result = org.jsoup.Jsoup.parse(html).text();
		} else if (bodyPart.getContent() instanceof MimeMultipart) {
			result = getTextFromMultipart((MimeMultipart) bodyPart.getContent());
		}
		return result;
	}

	private <T extends EmailConnector> Maybe<T> getSystemSessionBasedConnector(EntityType<? extends EmailConnector> type, String connectorId) {
		PersistenceGmSession session = cortexSessionProvider.get();

		final EntityQuery query;
		if (StringTools.isBlank(connectorId)) {
			//@formatter:off
			query = EntityQueryBuilder.from(type)
					.where()
						.property(EmailConnector.deploymentStatus).eq(DeploymentStatus.deployed)
					.done();
			//@formatter:on
		} else {
			//@formatter:off
			query = EntityQueryBuilder.from(type)
					.where()
						.conjunction()
							.property(EmailConnector.deploymentStatus).eq(DeploymentStatus.deployed)
							.property(EmailConnector.externalId).eq(connectorId)
						.close()
					.done();
			//@formatter:on
		}
		T c = session.query().entities(query).first();

		if (c == null || c.getDeploymentStatus() != DeploymentStatus.deployed) {
			final String kind;
			final EntityType<? extends Reason> reasonType;
			if (type.isAssignableFrom(RetrieveConnector.T)) {
				kind = "retrieve";
				reasonType = RetrieveConnectorMissing.T;
			} else {
				kind = "send";
				reasonType = SendConnectorMissing.T;
			}
			return Reasons.build(reasonType).text(StringTools.isBlank(connectorId) ? "There exists no deployed " + kind + " connector."
					: "The " + kind + " connector " + connectorId + " does not exist or is not deployed.").toMaybe();
		}
		return Maybe.complete(c);

	}

	// #HELPERS

	private Resource createEml(Message msg) {
		Exception suppressToException = new RuntimeException();
		Resource msgRes = null;
		Pair<InputStreamProvider, Long> serializeMessagePair = null;
		try {
			serializeMessagePair = serializeMessage(msg);
			msgRes = Resource.createTransient(serializeMessagePair.first);
		} catch (Exception e) {
			throw Exceptions.unchecked(suppressToException.getSuppressed()[0], "Failed to write email resource");
		}

		msgRes.setFileSize(serializeMessagePair.second);
		msgRes.setMimeType("message/rfc822");
		msgRes.setName("mail-" + RandomTools.newStandardUuid() + ".eml");

		return msgRes;
	}

	private Pair<InputStreamProvider, Long> serializeMessage(Message msg) throws IOException, MessagingException {
		StreamPipeFactory pipeFactory = getPipeStreamFactory();
		StreamPipe pipe = pipeFactory.newPipe("mail-serialization");

		long size = 0L;
		try (CountingOutputStream outputStream = new CountingOutputStream(pipe.acquireOutputStream())) {
			msg.writeTo(outputStream);
			size = outputStream.getCount();
		}

		return new Pair<>(pipe::openInputStream, size);
	}

	private String getTextFromMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
		int count = mimeMultipart.getCount();
		if (count == 0)
			throw new MessagingException("Multipart with no body parts not supported.");
		boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
		if (multipartAlt)
			// alternatives appear in an order of increasing
			// faithfulness to the original content. Customize as req'd.
			return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
		String result = "";
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			result += getTextFromBodyPart(bodyPart);
		}
		return result;
	}

	private List<Sender> getSenders(Message msg) throws MessagingException {
		Address[] senderAddresses = msg.getFrom();
		if (senderAddresses == null) {
			return newList();
		}
		List<Sender> senders = newList(senderAddresses.length);
		for (int i = 0; i < senderAddresses.length; i++) {
			InternetAddress inAdr = new InternetAddress(senderAddresses[i].toString());
			Sender sender = Sender.T.create();
			sender.setEMailAddress(inAdr.getAddress());
			sender.setName(inAdr.getPersonal());
			senders.add(sender);
		}

		return senders;
	}

	private List<Recipient> getRecipients(Message msg, RecipientType type) throws MessagingException {
		Address[] recepientAddresses = msg.getRecipients(type);
		return getRecipients(recepientAddresses);
	}

	private List<Recipient> getRecipients(Address[] addresses) throws MessagingException {
		if (addresses == null) {
			return newList();
		}
		List<Recipient> recipients = newList(addresses.length);
		for (int i = 0; i < addresses.length; i++) {
			InternetAddress inAdr = new InternetAddress(addresses[i].toString());
			Recipient recipient = Recipient.T.create();
			recipient.setEMailAddress(inAdr.getAddress());
			recipient.setName(inAdr.getPersonal());
			recipients.add(recipient);
		}

		return recipients;
	}

	private List<Resource> getAttachments(Object content, String contentDisposition) throws IOException, MessagingException {
		if (content instanceof String)
			return newList();

		if (content instanceof Multipart) {
			Multipart multipart = (Multipart) content;
			List<Resource> result = newList();

			for (int i = 0; i < multipart.getCount(); i++) {
				result.addAll(getAttachments(multipart.getBodyPart(i), contentDisposition));
			}
			return result;

		}
		return newList();
	}

	private List<Resource> getAttachments(BodyPart part, String contentDisposition) throws MessagingException, IOException {
		List<Resource> result = newList();
		Object content = part.getContent();
		if (content instanceof InputStream || content instanceof String) {

			String disposition = part.getDisposition();
			if (contentDisposition.equalsIgnoreCase(disposition) || (contentDisposition.equals(Part.ATTACHMENT) && disposition == null)) {

				String fileName = part.getFileName();
				if (fileName != null) {
					StreamPipeFactory pipeFactory = getPipeStreamFactory();
					StreamPipe pipe = pipeFactory.newPipe("attachment-serialization");
					long size = 0L;
					try (CountingOutputStream outputStream = new CountingOutputStream(pipe.acquireOutputStream());
							InputStream in = part.getInputStream()) {
						IOTools.pump(in, outputStream);
						size = outputStream.getCount();
					}

					Resource res = Resource.createTransient(pipe::openInputStream);
					res.setName(part.getFileName());
					String mimeType = HttpTools.getMimeTypeFromContentType(part.getContentType(), true);
					res.setMimeType(mimeType);
					res.setFileSize(size);
					result.add(res);
					return result;
				}
			} else {
				return newList();
			}
		}

		if (content instanceof Multipart) {
			Multipart multipart = (Multipart) content;
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				result.addAll(getAttachments(bodyPart, contentDisposition));
			}
		}
		return result;
	}

	private static <T> Maybe<T> exceptionToReason(EntityType<? extends Reason> type, Throwable e, String text) {
		String errorMessage = "TBID:" + RandomTools.newStandardUuid() + ": " + text;
		logger.error(errorMessage, e);
		return Reasons.build(type).text(errorMessage).cause(InternalError.from(e)).toMaybe();
	}
	private static <S extends EmailServiceResult> Maybe<S> exceptionToIncomplete(EntityType<? extends Reason> type, S partial, Throwable e,
			String text) {
		String errorMessage = "TBID:" + RandomTools.newStandardUuid() + ": " + text;
		logger.error(errorMessage, e);
		Reason reason = Reasons.build(type).text(errorMessage).cause(InternalError.from(e)).toReason();
		return Maybe.incomplete(partial, reason);
	}

	@Configurable
	@Required
	public void setCortexSessionProvider(Supplier<? extends PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	@Configurable
	@Required
	public void setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
	}

	@Configurable
	@Required
	public void setMailerCache(MailerCache mailerCache) {
		this.mailerCache = mailerCache;
	}

	public StreamPipeFactory getPipeStreamFactory() {
		if (pipeStreamFactory == null) {
			pipeStreamFactory = StreamPipes.simpleFactory();
		}
		return pipeStreamFactory;
	}

	@Configurable
	public void setPipeStreamFactory(StreamPipeFactory pipeStreamFactory) {
		this.pipeStreamFactory = pipeStreamFactory;
	}
	@Configurable
	@Required
	public void setHealthCheckExecutor(ExecutorService healthCheckExecutor) {
		this.healthCheckExecutor = healthCheckExecutor;
	}

}
