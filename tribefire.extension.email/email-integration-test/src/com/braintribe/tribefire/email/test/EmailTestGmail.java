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
package com.braintribe.tribefire.email.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.email.data.Email;
import com.braintribe.model.email.data.ReceivedEmail;
import com.braintribe.model.email.data.Recipient;
import com.braintribe.model.email.data.Sender;
import com.braintribe.model.email.deployment.connection.GmailImapConnector;
import com.braintribe.model.email.deployment.connection.GmailSmtpConnector;
import com.braintribe.model.email.deployment.connection.Pop3Connector;
import com.braintribe.model.email.service.DeleteEmail;
import com.braintribe.model.email.service.DeletedEmail;
import com.braintribe.model.email.service.MarkEmailUnread;
import com.braintribe.model.email.service.MarkedEmailAsUnread;
import com.braintribe.model.email.service.ReceiveEmails;
import com.braintribe.model.email.service.ReceivedEmailPostProcessing;
import com.braintribe.model.email.service.ReceivedEmails;
import com.braintribe.model.email.service.SendEmail;
import com.braintribe.model.email.service.SentEmail;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class EmailTestGmail extends AbstractEmailTest {

	private boolean initialized = false;

	private Random rnd = new Random();// NOSONAR: it is just a test

	// -----------------------------------------------------------------------
	// SETUP & TEARDOWN
	// -----------------------------------------------------------------------

	@Override
	@Before
	public void before() throws Exception {
		super.before();
		if (!initialized) {
			initialized = true;
			connectorGmailSmtp();
			connectorGmailImap();
			// Works as with Imap, except for testSendAndReceiveTwice
			// connectorGmailPop3();
		}
		drainInbox();
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	@Ignore // GMail does not return the receipt to the same sender; but the header was there
	public void testSendAndReceiveWithReceipts() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Receipt Test " + RandomTools.newStandardUuid();
		String subject = "Receipt Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setDispositionNotificationTo(to);
		email.setReturnReceiptTo(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(2);

		System.out.println("testSendAndReceiveWithReceipts: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveWithReplyTo() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());
		Recipient replyTo = Recipient.create("hello@world.com");
		replyTo.setName("Hello World");
		// Note: bounceTo is not supported by the receiver; thus checking this manually in the Gmail account
		Recipient bounceTo = Recipient.create("bounce@world.com");

		String bodyText = "ReplyTo Test " + RandomTools.newStandardUuid();
		String subject = "ReplyTo Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.getReplyToList().add(replyTo);
		email.setBounceTo(bounceTo);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();
		List<Recipient> replyToList = rm.getReplyToList();
		assertThat(replyToList).hasSize(1);
		Recipient recReplyTo = replyToList.get(0);
		assertThat(recReplyTo.getEMailAddress()).isEqualTo("hello@world.com");
		assertThat(recReplyTo.getName()).isEqualTo("Hello World");

		System.out.println("testSendAndReceiveWithReplyTo: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveEml() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "EML Test " + RandomTools.newStandardUuid();
		String subject = "EML TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();
		Resource eml = rm.getEml();
		assertThat(eml).isNotNull();
		assertThat(eml.getFileSize()).isGreaterThan(0L);
		assertThat(eml.getMimeType()).isEqualTo("message/rfc822");

		System.out.println("testSendAndReceiveEml: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithFrom() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());
		Sender from = Sender.create("hello@world.com");
		from.setName("Custom From");

		String bodyText = "FROM Test " + RandomTools.newStandardUuid();
		String subject = "FROM TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.getFromList().add(from);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		List<Sender> fromList = rm.getFromList();
		assertThat(fromList).hasSize(1);
		Sender sender = fromList.get(0);
		assertThat(sender.getName()).isEqualTo("Custom From");

		System.out.println("testSendAndReceiveTextWithFrom: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithBCC() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "BCC Test " + RandomTools.newStandardUuid();
		String subject = "BCC TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getBccList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();
		assertThat(rm.getToList()).isEmpty();
		assertThat(rm.getCcList()).isEmpty();

		System.out.println("testSendAndReceiveTextWithBCC: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithCC() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "CC Test " + RandomTools.newStandardUuid();
		String subject = "CC TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getCcList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();
		List<Recipient> ccList = rm.getCcList();
		assertThat(ccList).hasSize(1);
		Recipient cc = ccList.get(0);
		assertThat(cc.getEMailAddress()).isEqualTo(GmailCredentials.getEmail());

		System.out.println("testSendAndReceiveTextWithCC: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	// RKU: Note: I manually verified that the mail looks ok
	@Test
	public void testSendAndReceiveWithInlineAttachment() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject("INLINE Test at " + DateTools.getCurrentDateString());
		email.setHtmlBody("<body>Have a look at this picture: <img src=\"cid:AbcXyz123\" /></body>");

		byte[] imageBytes = IOTools.inputStreamToByteArray(new FileInputStream("res/image1.png"));
		Resource inlineResource = Resource.createTransient(() -> new ByteArrayInputStream(imageBytes));
		inlineResource.setMimeType("images/png");
		inlineResource.setName("image1.png");
		inlineResource.setId("AbcXyz123");
		email.getInlineAttachments().add(inlineResource);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm.getTextBody().trim()).contains("Have a look at this picture");
		List<Resource> inlineAttachments = rm.getInlineAttachments();
		assertThat(inlineAttachments).hasSize(1);
		Resource inlineAtt = inlineAttachments.get(0);
		assertThat(inlineAtt.getName()).isEqualTo("image1.png");

		System.out.println("testSendAndReceiveWithInlineAttachment: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveText() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Test " + RandomTools.newStandardUuid();
		String subject = "TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		System.out.println("testSendAndReceiveText: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveHtml() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyHtml = "<body>Test " + RandomTools.newStandardUuid() + "</body>";
		String subject = "HTML Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setHtmlBody(bodyHtml);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getHtmlBody().trim()).isEqualTo(bodyHtml);

		System.out.println("testSendAndReceiveHtml: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveHtmlAndText() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String uuid = RandomTools.newStandardUuid();
		String bodyHtml = "<body>HTML Test " + uuid + "</body>";
		String bodyText = "TEXT Test " + uuid;
		String subject = "HTML&TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setHtmlBody(bodyHtml);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getHtmlBody().trim()).isEqualTo(bodyHtml);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);

		System.out.println("testSendAndReceiveHtmlAndText: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithTextAttachment() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Test w/ Text Attachment " + RandomTools.newStandardUuid();
		String subject = "TEXT w/ Text Attachment Test at " + DateTools.getCurrentDateString();

		String attText = "hello, world";
		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(attText.getBytes("UTF-8")));
		resource.setName("test.txt");
		resource.setMimeType("text/plain");

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);
		email.getAttachments().add(resource);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		List<Resource> attList = rm.getAttachments();
		assertThat(attList).hasSize(1);
		Resource recResource = attList.get(0);
		assertThat(recResource.getName()).isEqualTo("test.txt");
		try (InputStream in = recResource.openStream()) {
			String attContent = IOTools.slurp(in, "UTF-8");
			assertThat(attContent).isEqualTo(attText);
		}

		System.out.println("testSendAndReceiveTextWithTextAttachment: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithMultipleTextAttachments() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Test w/ Multiple Text Attachments " + RandomTools.newStandardUuid();
		String subject = "TEXT w/ Multiple Text Attachments Test at " + DateTools.getCurrentDateString();

		String attText1 = "hello, world 1";
		Resource resource1 = Resource.createTransient(() -> new ByteArrayInputStream(attText1.getBytes("UTF-8")));
		resource1.setName("test1.txt");
		resource1.setMimeType("text/plain");

		String attText2 = "hello, world 2";
		Resource resource2 = Resource.createTransient(() -> new ByteArrayInputStream(attText2.getBytes("UTF-8")));
		resource2.setName("test2.txt");
		resource2.setMimeType("text/plain");

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);
		email.getAttachments().add(resource1);
		email.getAttachments().add(resource2);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		List<Resource> attList = rm.getAttachments();
		assertThat(attList).hasSize(2);
		boolean found1 = false;
		boolean found2 = false;
		for (Resource r : attList) {
			String expected = null;
			if (r.getName().equals("test1.txt")) {
				found1 = true;
				expected = attText1;
			} else if (r.getName().equals("test2.txt")) {
				found2 = true;
				expected = attText2;
			} else {
				throw new Exception("Unexpected filename " + r);
			}
			try (InputStream in = r.openStream()) {
				String attContent = IOTools.slurp(in, "UTF-8");
				assertThat(attContent).isEqualTo(expected);
			}
		}
		if (!found1 || !found2) {
			throw new Exception("Could not find 1 or 2 in: " + attList);
		}

		System.out.println("testSendAndReceiveTextWithMultipleTextAttachments: Sending email: " + sentEmail.getDurationInMs()
				+ " ms, receiving email: " + receivedEmails.getDurationInMs() + " ms");
	}
	@Test
	public void testSendAndReceiveTextWithUnicodeAttachment() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Test w/ Unicode Attachment " + RandomTools.newStandardUuid();
		String subject = "TEXT w/ Unicode Attachment Test at " + DateTools.getCurrentDateString();

		String attText = "hello, \u0ca1\u0ca4 world";
		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(attText.getBytes("UTF-8")));
		resource.setName("test.txt");
		resource.setMimeType("text/plain");

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);
		email.getAttachments().add(resource);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		List<Resource> attList = rm.getAttachments();
		assertThat(attList).hasSize(1);
		Resource recResource = attList.get(0);
		assertThat(recResource.getName()).isEqualTo("test.txt");
		try (InputStream in = recResource.openStream()) {
			String attContent = IOTools.slurp(in, "UTF-8");
			assertThat(attContent).isEqualTo(attText);
		}

		System.out.println("testSendAndReceiveTextWithUnicodeAttachment: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTextWithBinaryAttachment() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Test w/ Binary Attachment " + RandomTools.newStandardUuid();
		String subject = "TEXT w/ Binary Attachment Test at " + DateTools.getCurrentDateString();

		byte[] content = new byte[1024];
		rnd.nextBytes(content);
		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(content));
		resource.setName("test.bin");
		resource.setMimeType("application/octet-stream");

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);
		email.getAttachments().add(resource);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		List<Resource> attList = rm.getAttachments();
		assertThat(attList).hasSize(1);
		Resource recResource = attList.get(0);
		assertThat(recResource.getName()).isEqualTo("test.bin");
		try (InputStream in = recResource.openStream()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOTools.pump(in, baos);
			byte[] actual = baos.toByteArray();
			assertThat(content).isEqualTo(actual);
		}

		System.out.println("testSendAndReceiveTextWithBinaryAttachment: Sending email: " + sentEmail.getDurationInMs() + " ms, receiving email: "
				+ receivedEmails.getDurationInMs() + " ms");
	}

	@Test
	public void testSendAndReceiveTwice() throws Exception {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String bodyText = "Dual test " + RandomTools.newStandardUuid();
		String subject = "Dual TEXT Test at " + DateTools.getCurrentDateString();

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		// Send and receive email
		// Do not delete email when retrieving

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.MARK_READ);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();

		// We expect no mail to be retrieved (it is marked already as read)

		recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.MARK_READ);
		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(0);

		// Now we want this mail again

		recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setUnreadOnly(false);
		recReq.setPostProcessing(ReceivedEmailPostProcessing.MARK_READ);
		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
		assertThat(rm.getHtmlBody()).isNull();
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected void drainInbox() {
		Instant start = NanoClock.INSTANCE.instant();

		ReceiveEmails req = ReceiveEmails.T.create();
		req.setMaxEmailCount(50);
		req.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		req.setUnreadOnly(false);
		int iterations = 0;
		int deleted = 0;
		do {
			iterations++;
			ReceivedEmails receivedEmails = req.eval(cortexSession).get();
			List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
			if (list.size() == 0) {
				break;
			} else {
				deleted += list.size();
			}
		} while (iterations < 20);
		if (iterations >= 20) {
			throw new RuntimeException("Could not drain Inbox");
		}
		System.out.println("Deleted " + deleted + " emails prior to testing in " + StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
	}

	protected GmailSmtpConnector connectorGmailSmtp() {

		EntityQuery query = EntityQueryBuilder.from(GmailSmtpConnector.T).where().property(GmailSmtpConnector.externalId)
				.eq(TEST_EXTERNAL_ID_SMTP_CONNECTION_GMAIL_GMAIL).done();
		GmailSmtpConnector existing = cortexSession.query().entities(query).first();
		if (existing != null) {
			return existing;
		}

		GmailSmtpConnector emailGmailSmtpConnector = cortexSession.create(GmailSmtpConnector.T);
		String externalId = TEST_EXTERNAL_ID_SMTP_CONNECTION_GMAIL_GMAIL;
		emailGmailSmtpConnector.setExternalId(externalId);
		emailGmailSmtpConnector.setName("Email Gmail Transmission Connection");
		emailGmailSmtpConnector.setGlobalId(GLOBAL_ID_CONNECTION_PREFIX + externalId);
		emailGmailSmtpConnector.setAutoDeploy(true);

		emailGmailSmtpConnector.setUser(GmailCredentials.getEmail());
		emailGmailSmtpConnector.setPassword(GmailCredentials.getPassword());

		logger.info(() -> "Creating SMTP connection to " + emailGmailSmtpConnector.getSmtpHostName() + " with credentials "
				+ emailGmailSmtpConnector.getUser() + "/" + emailGmailSmtpConnector.getPassword());

		deployDeployable(emailGmailSmtpConnector);

		return emailGmailSmtpConnector;
	}

	protected GmailImapConnector connectorGmailImap() {

		EntityQuery query = EntityQueryBuilder.from(GmailImapConnector.T).where().property(GmailSmtpConnector.externalId)
				.eq(TEST_EXTERNAL_ID_IMAP_CONNECTION_GMAIL_GMAIL).done();
		GmailImapConnector existing = cortexSession.query().entities(query).first();
		if (existing != null) {
			return existing;
		}

		GmailImapConnector emailGmailImapConnector = cortexSession.create(GmailImapConnector.T);

		String externalId = TEST_EXTERNAL_ID_IMAP_CONNECTION_GMAIL_GMAIL;
		emailGmailImapConnector.setExternalId(externalId);
		emailGmailImapConnector.setName("Email Gmail Retrieve Connection");
		emailGmailImapConnector.setGlobalId(GLOBAL_ID_CONNECTION_PREFIX + externalId);
		emailGmailImapConnector.setAutoDeploy(true);

		emailGmailImapConnector.setUser(GmailCredentials.getEmail());
		emailGmailImapConnector.setPassword(GmailCredentials.getPassword());

		logger.info(() -> "Creating IMAP connection to " + emailGmailImapConnector.getHost() + " with credentials "
				+ emailGmailImapConnector.getUser() + "/" + emailGmailImapConnector.getPassword());

		deployDeployable(emailGmailImapConnector);

		return emailGmailImapConnector;
	}

	protected Pop3Connector connectorGmailPop3() {

		EntityQuery query = EntityQueryBuilder.from(Pop3Connector.T).where().property(Pop3Connector.externalId)
				.eq(TEST_EXTERNAL_ID_POP3_CONNECTION_GMAIL_GMAIL).done();
		Pop3Connector existing = cortexSession.query().entities(query).first();
		if (existing != null) {
			return existing;
		}

		Pop3Connector emailGmailPop3Connector = cortexSession.create(Pop3Connector.T);
		String externalId = TEST_EXTERNAL_ID_POP3_CONNECTION_GMAIL_GMAIL;
		emailGmailPop3Connector.setExternalId(externalId);
		emailGmailPop3Connector.setName("Email Gmail POP3 Retrieve Connection");
		emailGmailPop3Connector.setGlobalId(GLOBAL_ID_CONNECTION_PREFIX + externalId);
		emailGmailPop3Connector.setAutoDeploy(true);

		emailGmailPop3Connector.setUser(GmailCredentials.getEmail());
		emailGmailPop3Connector.setPassword(GmailCredentials.getPassword());
		emailGmailPop3Connector.setHost("pop.gmail.com");
		emailGmailPop3Connector.setPort(995);

		logger.info(() -> "Creating POP3 connection to " + emailGmailPop3Connector.getHost() + " with credentials "
				+ emailGmailPop3Connector.getUser() + "/" + emailGmailPop3Connector.getPassword());

		deployDeployable(emailGmailPop3Connector);

		return emailGmailPop3Connector;
	}

	@Test
	public void testSearchBySubject() {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String id = RandomTools.newStandardUuid();

		String bodyText = "Subject Search Test " + id;
		String subject = bodyText;

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		// First search for some other stuff; expect 0 results
		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setSearchExpression("subject = \"undef\"");
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(0);

		// Now search for the correct subject part
		recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setSearchExpression("subject = \"" + id + "\"");
		recReq.setPostProcessing(ReceivedEmailPostProcessing.DELETE);
		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);
	}

	@Test
	public void testDelete() {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String id = RandomTools.newStandardUuid();

		String bodyText = "Delete Test " + id;
		String subject = bodyText;

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setSearchExpression("subject = \"" + id + "\"");
		recReq.setPostProcessing(ReceivedEmailPostProcessing.NOP);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();

		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);

		DeleteEmail deleteReq = DeleteEmail.T.create();
		deleteReq.setEmailId(rm.getId());
		DeletedEmail deletedEmail = deleteReq.eval(cortexSession).get();
		assertThat(deletedEmail.getEmailFound()).isTrue();
		assertThat(deletedEmail.getEmailDeleted()).isTrue();

		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(0);
	}

	@Test
	public void testMarkUnread() {

		Recipient to = Recipient.create(GmailCredentials.getEmail());

		String id = RandomTools.newStandardUuid();

		String bodyText = "Mark Unread Test " + id;
		String subject = bodyText;

		Email email = Email.T.create();
		email.getToList().add(to);
		email.setSubject(subject);
		email.setTextBody(bodyText);

		SendEmail req = SendEmail.T.create();
		req.setEmail(email);
		EvalContext<? extends SentEmail> evalContext = req.eval(cortexSession);
		SentEmail sentEmail = evalContext.get();
		System.out.println("Sent message: " + sentEmail.getMessageId());

		ReceiveEmails recReq = ReceiveEmails.T.create();
		recReq.setMaxEmailCount(10);
		recReq.setUnreadOnly(true);
		recReq.setSearchExpression("subject = \"" + id + "\"");
		recReq.setPostProcessing(ReceivedEmailPostProcessing.MARK_READ);
		ReceivedEmails receivedEmails = recReq.eval(cortexSession).get();
		List<ReceivedEmail> list = receivedEmails.getReceivedEmails();

		assertThat(list).hasSize(1);

		ReceivedEmail rm = list.get(0);
		assertThat(rm).isNotNull();
		assertThat(rm.getSubject()).isEqualTo(subject);
		assertThat(rm.getTextBody().trim()).isEqualTo(bodyText);

		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(0);

		MarkEmailUnread markUnreadReq = MarkEmailUnread.T.create();
		markUnreadReq.setEmailId(rm.getId());
		MarkedEmailAsUnread markedEmailAsUnread = markUnreadReq.eval(cortexSession).get();

		assertThat(markedEmailAsUnread.getEmailFound()).isTrue();
		assertThat(markedEmailAsUnread.getEmailMarkedUnread()).isTrue();

		receivedEmails = recReq.eval(cortexSession).get();
		list = receivedEmails.getReceivedEmails();
		assertThat(list).hasSize(1);

	}

}
