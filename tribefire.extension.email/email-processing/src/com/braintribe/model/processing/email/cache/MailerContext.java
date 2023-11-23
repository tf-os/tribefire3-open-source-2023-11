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
package com.braintribe.model.processing.email.cache;

import org.simplejavamail.api.mailer.Mailer;

public class MailerContext {

	private Mailer mailer;
	private boolean sendAsync = false;

	public MailerContext(Mailer mailer) {
		this.mailer = mailer;
	}

	public Mailer getMailer() {
		return mailer;
	}

	public boolean getSendAsync() {
		return sendAsync;
	}

	public void setSendAsync(Boolean sendAsync) {
		if (sendAsync != null) {
			this.sendAsync = sendAsync.booleanValue();
		}
	}

}
