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
package com.braintribe.xml.parser.experts;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;

/**
 * helper to support the two xsd date related types 
 * xsd:dateTime
 * xsd:date
 * xsd:time
 * 
 * @author pit
 *
 */
public class DateInterpreter {
	
	private static Logger log = Logger.getLogger(DateInterpreter.class);	
	
	public DateInterpreter() {
	
	}
	
	public Date parseDateTime( String dateAsString) throws CodecException {
		try {
			DatatypeFactory df = DatatypeFactory.newInstance();
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(dateAsString);			
			return dateTime.toGregorianCalendar().getTime();
		} catch (Exception e) {
			String msg = "cannot parse datetime from [" + dateAsString + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	public String formatDateTime( Date date) throws CodecException {
		try {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			DatatypeFactory df = DatatypeFactory.newInstance();
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(calendar);
			return dateTime.toString();
			
		} catch (Exception e) {
			String msg = "cannot print dateTime from [" + date + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	public Date parseDate( String dateAsString) throws CodecException {
		try {
			DatatypeFactory df = DatatypeFactory.newInstance();
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(dateAsString);			
			return dateTime.toGregorianCalendar().getTime();
		} catch (Exception e) {
			String msg = "cannot parse date from [" + dateAsString + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	public String formatDate( Date date) throws CodecException {
		try {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			DatatypeFactory df = DatatypeFactory.newInstance();
			
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendarDate(calendar.get( Calendar.YEAR), calendar.get( Calendar.MONDAY) + 1, calendar.get( Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
			return dateTime.toString();
		} catch (Exception e) {
			String msg = "cannot print date from [" + date + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	public Date parseTime( String timeAsString) throws CodecException {
		try {
			DatatypeFactory df = DatatypeFactory.newInstance();
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendar(timeAsString);			
			return dateTime.toGregorianCalendar().getTime();
		} catch (Exception e) {
			String msg = "cannot parse date from [" + timeAsString + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	public String formatTime( Date date) throws CodecException {
		try {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			DatatypeFactory df = DatatypeFactory.newInstance();
			
			XMLGregorianCalendar dateTime = df.newXMLGregorianCalendarTime(calendar.get( Calendar.HOUR), calendar.get( Calendar.MINUTE), calendar.get( Calendar.SECOND), DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
			return dateTime.toString();
		} catch (Exception e) {
			String msg = "cannot print date from [" + date + "]";
			log.error( msg, e);
			throw new CodecException( msg, e);
		}
	}
	
	
	
}
