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
package com.braintribe.model.notification;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * TODO
 * 
 */

public interface MessageNotification extends Notification {

	EntityType<MessageNotification> T = EntityTypes.T(MessageNotification.class);

	String getMessage();
	void setMessage(String message);

	String getDetails();
	void setDetails(String details);

	Level getLevel();
	void setLevel(Level level);

	boolean getConfirmationRequired();
	void setConfirmationRequired(boolean confirmationRequired);

	boolean getManualClose();
	void setManualClose(boolean manualClose);

	boolean getTextBold();
	void setTextBold(boolean useBold);

	boolean getTextItalic();
	void setTextItalic(boolean useItalic);

	boolean getTextStrikeout();
	void setTextStrikeout(boolean useStrikeout);

	boolean getTextUnderline();
	void setTextUnderline(boolean useUnderline);

	static MessageNotification create(String message) {
		MessageNotification result = MessageNotification.T.create();
		result.setMessage(message);
		return result;
	}

	static MessageNotification create(String message, String details) {
		MessageNotification result = create(message);
		result.setDetails(details);
		return result;
	}

}