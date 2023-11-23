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
package com.braintribe.codec.marshaller.json.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PerformanceCertificate extends GenericEntity {

	EntityType<PerformanceCertificate> T = EntityTypes.T(PerformanceCertificate.class);
	
	String lnNummer = "lnNummer";
	String kundeMail = "kundeMail";
	String kundeMailOptional = "kundeMailOptional";
	String kundenfeedback = "kundenfeedback";
	String auftragsnummer = "auftragsnummer";
	String tatigkeitsnummer = "tatigkeitsnummer";
	String leistungsdatum = "leistungsdatum";
	String durchgefuhrteArbeit = "durchgefuhrteArbeit";
	String standort = "standort";
	String abteilung = "abteilung";
	String fahrerName = "fahrerName";
	String fahrerEmail = "fahrerEmail";
	String naechtigung = "naechtigung";
	String uhrzeitVerlassen = "uhrzeitVerlassen";
	String zielland = "zielland";
	String zeitRueckkehr = "zeitRueckkehr";
	String dokument = "dokument";
	
	//lnNumber
	String getLnNummer();
	void setLnNummer(String lnNummer);
	
	//customerMail
	String getKundeMail();
	void setKundeMail(String kundeMail);
	
	//customerMailOptional
	String getKundeMailOptional();
	void setKundeMailOptional(String kundeMailOptional);
	
	//customerFeedback
	Integer getKundenfeedback();
	void setKundenfeedback(Integer kundenfeedback);
	
	//orderNumber
	String getAuftragsnummer();
	void setAuftragsnummer(String auftragsnummer);
	
	//actionNumber
	String getTatigkeitsnummer();
	void setTatigkeitsnummer(String tatigkeitsnummer);
	
	//serviceDate
	String getLeistungsdatum();
	void setLeistungsdatum(String leistungsdatum);
	
	//workDone
	String getDurchgefuhrteArbeit();
	void setDurchgefuhrteArbeit(String durchgefuhrteArbeit);
	
	//location
	String getStandort();
	void setStandort(String standort);
	
	//department
	String getAbteilung();
	void setAbteilung(String abteilung);
	
	//driverName
	String getFahrerName();
	void setFahrerName(String fahrerName);
	
	//driverEmail
	String getFahrerEmail();
	void setFahrerEmail(String fahrerEmail);
	
	//overnightStay
	String getNaechtigung(); 
	void setNaechtigung(String naechtigung); 
	
	//timeLeave
	String getUhrzeitVerlassen();
	void setUhrzeitVerlassen(String uhrzeitVerlassen);
	
	//destinationCountry
	String getZielland();
	void setZielland(String zielland);
	
	//timeReturn
	String getZeitRueckkehr();
	void setZeitRueckkehr(String zeitRueckkehr);
	
	//document
	String getDokument();
	void setDokument(String dokument);
}

