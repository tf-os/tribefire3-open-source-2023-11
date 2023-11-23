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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.mail.Message.RecipientType;

import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.email.Recipient;

import com.braintribe.model.email.data.Sender;
import com.braintribe.model.email.deployment.connection.SmtpConnector;
import com.braintribe.utils.StringTools;

/**
 * Utility class for setting addresses
 */
public class SmtpUtil {

	private SmtpUtil() {
		// This just pffers static convenience methods
	}

	private static org.simplejavamail.api.email.Recipient transformRecipient(com.braintribe.model.email.data.Recipient source, RecipientType type) {
		String name = source.getName();
		String eMailAddress = source.getEMailAddress();
		Recipient rec = new Recipient(name, eMailAddress, type);
		return rec;
	}

	private static void setRecipient(com.braintribe.model.email.data.Recipient recipient, BiConsumer<String, String> setter) {
		if (recipient == null) {
			return;
		}
		String name = recipient.getName();
		String eMailAddress = recipient.getEMailAddress();
		if (!StringTools.isEmpty(eMailAddress)) {
			setter.accept(name, eMailAddress);
		}
	}

	public static void enrichRecipients(com.braintribe.model.email.data.Email email, EmailPopulatingBuilder resultingEmail,
			SmtpConnector smtpConnection) {
		List<Recipient> toList = new ArrayList<>();
		email.getToList().forEach(r -> toList.add(transformRecipient(r, RecipientType.TO)));
		resultingEmail.to(toList);

		List<Recipient> ccList = new ArrayList<>();
		email.getCcList().forEach(r -> ccList.add(transformRecipient(r, RecipientType.CC)));
		resultingEmail.cc(ccList);

		List<Recipient> bccList = new ArrayList<>();
		email.getBccList().forEach(r -> bccList.add(transformRecipient(r, RecipientType.BCC)));
		resultingEmail.bcc(bccList);

		List<com.braintribe.model.email.data.Recipient> replyToList = email.getReplyToList();
		if (!replyToList.isEmpty()) {
			com.braintribe.model.email.data.Recipient replyToRecipient = replyToList.get(0);
			resultingEmail.withReplyTo(replyToRecipient.getName(), replyToRecipient.getEMailAddress());
		}

		setRecipient(email.getBounceTo(), resultingEmail::withBounceTo);

		setRecipient(email.getDispositionNotificationTo(), resultingEmail::withDispositionNotificationTo);

		setRecipient(email.getReturnReceiptTo(), resultingEmail::withReturnReceiptTo);

		setFrom(email, resultingEmail, smtpConnection);

	}

	private static void setFrom(com.braintribe.model.email.data.Email email, EmailPopulatingBuilder resultingEmail, SmtpConnector smtpConnection) {
		if (!setFromDirectly(email, resultingEmail)) {
			Sender defaultFrom = smtpConnection.getDefaultFrom();
			if (defaultFrom != null) {
				resultingEmail.from(defaultFrom.getName(), defaultFrom.getEMailAddress());
			} else {
				String user = smtpConnection.getUser();
				if (!StringTools.isBlank(user)) {
					resultingEmail.from(user);
				} else {
					String loginUser = smtpConnection.getLoginUser();
					if (!StringTools.isBlank(loginUser)) {
						resultingEmail.from(loginUser);
					} else {
						throw new IllegalArgumentException("Could not determine any FROM address.");
					}
				}
			}
		}
	}

	private static boolean setFromDirectly(com.braintribe.model.email.data.Email email, EmailPopulatingBuilder resultingEmail) {
		List<Sender> fromList = email.getFromList();
		if (!fromList.isEmpty()) {
			Sender sender = fromList.get(0);
			String name = sender.getName();
			String eMailAddress = sender.getEMailAddress();
			if (!StringTools.isEmpty(eMailAddress)) {
				resultingEmail.from(name, eMailAddress);
			}
			return true;
		} else {
			return false;
		}
	}

}
