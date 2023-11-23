<?xml version="1.0" encoding="UTF-8"?>
<!-- Version 1.1 -->
<!-- Author: Walter Burkhard, West Informatik AG -->
<!-- Modified: 04.07.2016 -->
<!-- Achtung: ISO Namespace verwendet, weil ICC den SIX Namespace durch den ISO Namespace ersetzt! -->

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:px="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03"
    exclude-result-prefixes="px xsi xsl ">
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  <xsl:template match="/">
    <px:PayOrder xmlns="urn:FIDES">
      <MessageIdentification>
        <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:MsgId"/>
      </MessageIdentification>
      <MessageNameIdentification>pain.001.001.03</MessageNameIdentification>      
      <CreationDateTime>
        <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:CreDtTm"/>
      </CreationDateTime>
      <AuthorisationCode/>
      <AuthorisationProprietary/>
      <NumberOfTransactions>
        <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:NbOfTxs"/>
      </NumberOfTransactions>
      <xsl:if test="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:CtrlSum">
        <ControlSum>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:CtrlSum"/>      
        </ControlSum>
      </xsl:if>      
      <InitiatingParty>
        <Name/>
        <PostalAdrAddressType/>
        <PostalAdrDepartment/>
        <PostalAdrSubDepartment/>
        <PostalAdrStreetName/>
        <PostalAdrBuildingNumber/>
        <PostalAdrPostCode/>
        <PostalAdrTownName/>
        <PostalAdrCountrySubDivision/>
        <PostalAdrCountryCode/>
        <PostalAdrAddressLine/>
        <OrgIdentBIC/>
        <OrganisIdentOtherIdentification/>
        <OrganisIdentOtherCode/>
        <OrganisIdentOtherProprietary/>
        <OrganisIdentOtherIssuer/>
        <PrivateIdentBirthDate/>
        <PrivateIdentProvinceOfBirth>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
        </PrivateIdentProvinceOfBirth>
        <PrivateIdentCityOfBirth>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
        </PrivateIdentCityOfBirth>
        <PrivateIdentCountryOfBirth>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
        </PrivateIdentCountryOfBirth>
        <xsl:for-each select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:Id/px:PrvtId/px:Othr">        
          <OtherPersonIdentification>        
          <PrivateIdentOtherIdentification>
            <xsl:value-of select="px:Id"/>
          </PrivateIdentOtherIdentification>
          <PrivateIdentOtherCode>
            <xsl:value-of select="px:Cd"/>
          </PrivateIdentOtherCode>
          <PrivateIdentOtherProprietary>
            <xsl:value-of select="px:Prtry"/>
          </PrivateIdentOtherProprietary>
          <PrivateIdentOtherIssuer>
            <xsl:value-of select="px:Issr"/>
          </PrivateIdentOtherIssuer>
          </OtherPersonIdentification>
        </xsl:for-each>
        <CountryOfResidence/>
        <ContactDetailsNamePrefix/>
        <ContactDetailsName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:CtctDtls/px:Nm"/>
        </ContactDetailsName>
        <ContactDetailsPhoneNumber/>
        <ContactDetailsMobileNumber/>
        <ContactDetailsFaxNumber/>
        <ContactDetailsEmailAddress/>
        <ContactDetailsOther>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:InitgPty/px:CtctDtls/px:Othr"/>
        </ContactDetailsOther>
      </InitiatingParty>
      <ForwardingAgent>
        <FinInstBIC>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:BIC"/>
        </FinInstBIC>
        <FinInstClearingSystemIdentifCode>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Cd"/>
        </FinInstClearingSystemIdentifCode>
        <FinInstClearingSystemIdentifProprietary>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Prtry"/>
        </FinInstClearingSystemIdentifProprietary>
        <FinInstMemberIdentification>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:ClrSysMmbId/px:MmbId"/>
        </FinInstMemberIdentification>
        <FinInstName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:Nm"/>
        </FinInstName>
        <FinInstPostalAdrAddressType>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:AdrTp"/>
        </FinInstPostalAdrAddressType>
        <FinInstPostalAdrDepartment>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:Dept"/>
        </FinInstPostalAdrDepartment>
        <FinInstPostalAdrSubDepartment>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:SubDept"/>
        </FinInstPostalAdrSubDepartment>
        <FinInstPostalAdrStreetName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:StrtNm"/>
        </FinInstPostalAdrStreetName>
        <FinInstPostalAdrBuildingNumber>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:BldgNb"/>
        </FinInstPostalAdrBuildingNumber>
        <FinInstPostalAdrPostCode>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:PstCd"/>
        </FinInstPostalAdrPostCode>
        <FinInstPostalAdrTownName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:TwnNm"/>
        </FinInstPostalAdrTownName>
        <FinInstPostalAdrCountrySubDivision>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:CtrySubDvsn"/>
        </FinInstPostalAdrCountrySubDivision>
        <FinInstPostalAdrCountry>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:Ctry"/>
        </FinInstPostalAdrCountry>
        <xsl:for-each select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:PstlAdr/px:AdrLine">
          <xsl:if test="7&gt;position()">
            <FinInstPostalAdrAddressLine>
              <xsl:value-of select="."/>
            </FinInstPostalAdrAddressLine>
          </xsl:if>
        </xsl:for-each>
        <FinInstOtherIdentification>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:Othr/px:Id"/>
        </FinInstOtherIdentification>
        <FinInstOtherSchemeNameCode>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:Othr/px:SchmeNm/px:Cd"/>
        </FinInstOtherSchemeNameCode>
        <FinInstIOtherSchemeNameProprietary>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:Othr/px:SchmeNm/px:Prtry"/>
        </FinInstIOtherSchemeNameProprietary>
        <FinInstOtherIssuer>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:FinInstnId/px:Othr/px:Issr"/>
        </FinInstOtherIssuer>
        <BrnchIdentification>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:Id"/>
        </BrnchIdentification>
        <BrnchIdName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:Nm"/>
        </BrnchIdName>
        <BrnchPostalAdrAddressType>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:AdrTp"/>
        </BrnchPostalAdrAddressType>
        <BrnchPostalAdrDepartment>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:Dept"/>
        </BrnchPostalAdrDepartment>
        <BrnchPostalAdrSubDepartment>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:SubDept"/>
        </BrnchPostalAdrSubDepartment>
        <BrnchPostalAdrStreetName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:StrtNm"/>
        </BrnchPostalAdrStreetName>
        <BrnchPostalAdrBuildingNumber>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:BldgNb"/>
        </BrnchPostalAdrBuildingNumber>
        <BrnchPostalAdrPostCode>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:PstCd"/>
        </BrnchPostalAdrPostCode>
        <BrnchPostalAdrTownName>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:TwnNm"/>
        </BrnchPostalAdrTownName>
        <BrnchPostalAdrCountrySubDivision>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:CtrySubDvsn"/>
        </BrnchPostalAdrCountrySubDivision>
        <BrnchPostalAdrCountry>
          <xsl:value-of select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:Ctry"/>
        </BrnchPostalAdrCountry>
        <xsl:for-each select="/px:Document/px:CstmrCdtTrfInitn/px:GrpHdr/px:FwdgAgt/px:BrnchId/px:PstlAdr/px:AdrLine">
          <xsl:if test="7&gt;position()">
            <BrnchPostalAdrAddressLine>
              <xsl:value-of select="."/>
            </BrnchPostalAdrAddressLine>
          </xsl:if>
        </xsl:for-each>
      </ForwardingAgent>
      <PaymentType>CreditTransfer</PaymentType>
      <xsl:for-each select="/px:Document/px:CstmrCdtTrfInitn/px:PmtInf">
        <PayContainer>
          <CreditTransfer>
            <OrderReference/>
            <CurrencyCode/>
            <PaymentInformationIdentification>
              <xsl:value-of select="px:PmtInfId"/>
            </PaymentInformationIdentification>
            <PaymentMethod>
              <xsl:value-of select="px:PmtMtd"/>
            </PaymentMethod>
            
            <BatchBooking>
             <xsl:choose>
               <xsl:when test="not(px:BtchBookg) or px:BtchBookg = ''">
                <xsl:value-of select="'true'"/>
               </xsl:when>
               <xsl:otherwise>
                <xsl:value-of select="px:BtchBookg"/>
               </xsl:otherwise>
             </xsl:choose>
            </BatchBooking>            
                        
            <NumberOfTransactions>
              <xsl:value-of select="px:NbOfTxs"/>
            </NumberOfTransactions>
            <xsl:if test="px:CtrlSum">            
              <ControlSum>
                <xsl:value-of select="px:CtrlSum"/>
              </ControlSum>
            </xsl:if>            
            <PaymentInfoInstructionPriority>
              <xsl:value-of select="px:PmtTpInf/px:InstrPrty"/>
            </PaymentInfoInstructionPriority>
            <PaymentInfoServiceLevelCode>
              <xsl:value-of select="px:PmtTpInf/px:SvcLvl/px:Cd"/>
            </PaymentInfoServiceLevelCode>
            <PaymentInfoServiceLevelProprietary>
              <xsl:value-of select="px:PmtTpInf/px:SvcLvl/px:Prtry"/>
            </PaymentInfoServiceLevelProprietary>
            <PaymentInfoLocalInstrumentCode>
              <xsl:value-of select="px:PmtTpInf/px:LclInstrm/px:Cd"/>
            </PaymentInfoLocalInstrumentCode>
            <PaymentInfoLocalInstrumentProprietary>
              <xsl:value-of select="px:PmtTpInf/px:LclInstrm/px:Prtry"/>
            </PaymentInfoLocalInstrumentProprietary>
            <PaymentInfoCategoryPurposeCode>
              <xsl:value-of select="px:PmtTpInf/px:CtgyPurp/px:Cd"/>
            </PaymentInfoCategoryPurposeCode>
            <PaymentInfoCategoryPurposeProprietary/>
            <RequestedExecutionDate>
              <xsl:value-of select="px:ReqdExctnDt"/>
            </RequestedExecutionDate>
            <DebtorPartyIdentification>
              <Name>
                <xsl:value-of select="px:Dbtr/px:Nm"/>
              </Name>
              <PostalAdrAddressType>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:AdrTp"/>
              </PostalAdrAddressType>
              <PostalAdrDepartment>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:Dept"/>
              </PostalAdrDepartment>
              <PostalAdrSubDepartment>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:SubDept"/>
              </PostalAdrSubDepartment>
              <PostalAdrStreetName>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:StrtNm"/>
              </PostalAdrStreetName>
              <PostalAdrBuildingNumber>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:BldgNb"/>
              </PostalAdrBuildingNumber>
              <PostalAdrPostCode>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:PstCd"/>
              </PostalAdrPostCode>
              <PostalAdrTownName>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:TwnNm"/>
              </PostalAdrTownName>
              <PostalAdrCountrySubDivision>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:CtrySubDvsn"/>
              </PostalAdrCountrySubDivision>
              <PostalAdrCountryCode>
                <xsl:value-of select="px:Dbtr/px:PstlAdr/px:Ctry"/>
              </PostalAdrCountryCode>
              <xsl:for-each select="px:Dbtr/px:PstlAdr/px:AdrLine">
                <xsl:if test="7&gt;position()">
                  <PostalAdrAddressLine>
                    <xsl:value-of select="."/>
                  </PostalAdrAddressLine>
                </xsl:if>
              </xsl:for-each>
              <OrgIdentBIC>
                <xsl:value-of select="px:Dbtr/px:Id/px:OrgId/px:BICOrBEI"/>
              </OrgIdentBIC>
              <OrganisIdentOtherIdentification>
                <xsl:value-of select="px:Dbtr/px:Id/px:OrgId/px:Othr/px:Id"/>
              </OrganisIdentOtherIdentification>
              <OrganisIdentOtherCode>
                <xsl:value-of select="px:Dbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
              </OrganisIdentOtherCode>
              <OrganisIdentOtherProprietary>
                <xsl:value-of select="px:Dbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
              </OrganisIdentOtherProprietary>
              <OrganisIdentOtherIssuer>
                <xsl:value-of select="px:Dbtr/px:Id/px:OrgId/px:Othr/px:Issr"/>
              </OrganisIdentOtherIssuer>
              <PrivateIdentBirthDate>
                <xsl:value-of select="px:Dbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
              </PrivateIdentBirthDate>
              <PrivateIdentProvinceOfBirth>
                <xsl:value-of select="px:Dbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
              </PrivateIdentProvinceOfBirth>
              <PrivateIdentCityOfBirth>
                <xsl:value-of select="px:Dbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
              </PrivateIdentCityOfBirth>
              <PrivateIdentCountryOfBirth>
                <xsl:value-of select="px:Dbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
              </PrivateIdentCountryOfBirth>
              <xsl:for-each select="px:Dbtr/px:Id/px:PrvtId/px:Othr">   
                <OtherPersonIdentification>              
                <PrivateIdentOtherIdentification>
                  <xsl:value-of select="px:Id"/>
                </PrivateIdentOtherIdentification>
                <PrivateIdentOtherCode>
                  <xsl:value-of select="px:Cd"/>
                </PrivateIdentOtherCode>
                <PrivateIdentOtherProprietary>
                  <xsl:value-of select="px:Prtry"/>
                </PrivateIdentOtherProprietary>
                <PrivateIdentOtherIssuer>
                  <xsl:value-of select="px:Issr"/>
                </PrivateIdentOtherIssuer>
                </OtherPersonIdentification>              
              </xsl:for-each>
              <CountryOfResidence/>
              <ContactDetailsNamePrefix/>
              <ContactDetailsName/>
              <ContactDetailsPhoneNumber/>
              <ContactDetailsMobileNumber/>
              <ContactDetailsFaxNumber/>
              <ContactDetailsEmailAddress/>
              <ContactDetailsOther/>
            </DebtorPartyIdentification>
            <DebtorAccountIBAN>
              <xsl:value-of select="px:DbtrAcct/px:Id/px:IBAN"/>
            </DebtorAccountIBAN>
            <DebtorAccountOtherIdentification>
              <xsl:value-of select="px:DbtrAcct/px:Id/px:Othr/px:Id"/>
            </DebtorAccountOtherIdentification>
            <DebtorAccountOtherIdNameCode/>
            <DebtorAccountOtherIdNameProprietary/>
            <DebtorAccountOtherIdIssuer/>
            <DebtorAccountTypeCode>
              <xsl:value-of select="px:DbtrAcct/px:Tp/px:Cd"/>
            </DebtorAccountTypeCode>
            <DebtorAccountTypeProprietary>
              <xsl:value-of select="px:DbtrAcct/px:Tp/px:Prtry"/>
            </DebtorAccountTypeProprietary>
            <DebtorAccountCurrency>
              <xsl:value-of select="px:DbtrAcct/px:Ccy"/>
            </DebtorAccountCurrency>
            <DebtorAccountName/>
            <DebtorAgentFinancialInstitution>
              <FinInstBIC>
                <xsl:value-of select="px:DbtrAgt/px:FinInstnId/px:BIC"/>
              </FinInstBIC>
              <FinInstClearingSystemIdentifCode>
                <xsl:value-of select="px:DbtrAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Cd"/>
              </FinInstClearingSystemIdentifCode>
              <FinInstClearingSystemIdentifProprietary>
                <xsl:value-of select="px:DbtrAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Prtry"/>
              </FinInstClearingSystemIdentifProprietary>
              <FinInstMemberIdentification>
                <xsl:value-of select="px:DbtrAgt/px:FinInstnId/px:ClrSysMmbId/px:MmbId"/>
              </FinInstMemberIdentification>
              <FinInstName/>
              <FinInstPostalAdrAddressType/>
              <FinInstPostalAdrDepartment/>
              <FinInstPostalAdrSubDepartment/>
              <FinInstPostalAdrStreetName/>
              <FinInstPostalAdrBuildingNumber/>
              <FinInstPostalAdrPostCode/>
              <FinInstPostalAdrTownName/>
              <FinInstPostalAdrCountrySubDivision/>
              <FinInstPostalAdrCountry/>
              <FinInstPostalAdrAddressLine/>
              <FinInstOtherIdentification/>
              <FinInstOtherSchemeNameCode/>
              <FinInstIOtherSchemeNameProprietary/>
              <FinInstOtherIssuer/>
              <BrnchIdentification/>
              <BrnchIdName/>
              <BrnchPostalAdrAddressType/>
              <BrnchPostalAdrDepartment/>
              <BrnchPostalAdrSubDepartment/>
              <BrnchPostalAdrStreetName/>
              <BrnchPostalAdrBuildingNumber/>
              <BrnchPostalAdrPostCode/>
              <BrnchPostalAdrTownName/>
              <BrnchPostalAdrCountrySubDivision/>
              <BrnchPostalAdrCountry/>
              <BrnchPostalAdrAddressLine/>
            </DebtorAgentFinancialInstitution>
            <DebtorAgentAccount>
              <IBAN/>
              <AccountOtherIdentification/>
              <AccountOtherIdNameCode/>
              <AccountOtherIdNameProprietary/>
              <AccountOtherIdIssuer/>
              <AccountTypeCode/>
              <AccountTypeProprietary/>
              <AccountCurrency/>
              <AccountName/>
            </DebtorAgentAccount>
            <UltimateDebtor>
              <Name>
                <xsl:value-of select="px:UltmtDbtr/px:Nm"/>
              </Name>
              <PostalAdrAddressType>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:AdrTp"/>
              </PostalAdrAddressType>
              <PostalAdrDepartment>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:Dept"/>
              </PostalAdrDepartment>
              <PostalAdrSubDepartment>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:SubDept"/>
              </PostalAdrSubDepartment>
              <PostalAdrStreetName>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:StrtNm"/>
              </PostalAdrStreetName>
              <PostalAdrBuildingNumber>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:BldgNb"/>
              </PostalAdrBuildingNumber>
              <PostalAdrPostCode>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:PstCd"/>
              </PostalAdrPostCode>
              <PostalAdrTownName>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:TwnNm"/>
              </PostalAdrTownName>
              <PostalAdrCountrySubDivision>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:CtrySubDvsn"/>
              </PostalAdrCountrySubDivision>
              <PostalAdrCountryCode>
                <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:Ctry"/>
              </PostalAdrCountryCode>
              <xsl:for-each select="px:UltmtDbtr/px:PstlAdr/px:AdrLine">
                <xsl:if test="7&gt;position()">
                  <PostalAdrAddressLine>
                    <xsl:value-of select="."/>
                  </PostalAdrAddressLine>
                </xsl:if>
              </xsl:for-each>
              <OrgIdentBIC>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:BICOrBEI"/>
              </OrgIdentBIC>
              <OrganisIdentOtherIdentification>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:Id"/>
              </OrganisIdentOtherIdentification>
              <OrganisIdentOtherCode>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
              </OrganisIdentOtherCode>
              <OrganisIdentOtherProprietary>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
              </OrganisIdentOtherProprietary>
              <OrganisIdentOtherIssuer>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:Issr"/>
              </OrganisIdentOtherIssuer>
              <PrivateIdentBirthDate>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
              </PrivateIdentBirthDate>
              <PrivateIdentProvinceOfBirth>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
              </PrivateIdentProvinceOfBirth>
              <PrivateIdentCityOfBirth>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
              </PrivateIdentCityOfBirth>
              <PrivateIdentCountryOfBirth>
                <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
              </PrivateIdentCountryOfBirth>
              <xsl:for-each select="px:UltmtDbtr/px:Id/px:PrvtId/px:Othr">
                <OtherPersonIdentification>              
                <PrivateIdentOtherIdentification>
                  <xsl:value-of select="px:Id"/>
                </PrivateIdentOtherIdentification>
                <PrivateIdentOtherCode>
                  <xsl:value-of select="px:Cd"/>
                </PrivateIdentOtherCode>
                <PrivateIdentOtherProprietary>
                  <xsl:value-of select="px:Prtry"/>
                </PrivateIdentOtherProprietary>
                <PrivateIdentOtherIssuer>
                  <xsl:value-of select="px:Issr"/>
                </PrivateIdentOtherIssuer>
                </OtherPersonIdentification>
              </xsl:for-each>              
              <CountryOfResidence/>
              <ContactDetailsNamePrefix/>
              <ContactDetailsName/>
              <ContactDetailsPhoneNumber/>
              <ContactDetailsMobileNumber/>
              <ContactDetailsFaxNumber/>
              <ContactDetailsEmailAddress/>
              <ContactDetailsOther/>
            </UltimateDebtor>
            <ChargeBearer>
              <xsl:value-of select="px:ChrgBr"/>
            </ChargeBearer>
            <ChargesAccountIBAN>
              <xsl:value-of select="px:ChrgsAcct/px:Id/px:IBAN"/>
            </ChargesAccountIBAN>
            <ChargesAccountOtherIdentification>
              <xsl:value-of select="px:ChrgsAcct/px:Id/px:Othr/px:Id"/>
            </ChargesAccountOtherIdentification>
			<ChargesAccountOtherIdNameCode/>
            <ChargesAccountOtherIdNameProprietary/>
            <ChargesAccountOtherIdIssuer/>
            <ChargesAccountTypeCode/>
            <ChargesAccountTypeProprietary/>
            <ChargesAccountCurrency>
              <xsl:value-of select="px:ChrgsAcct/px:Ccy"/>
            </ChargesAccountCurrency>
            <ChargesAccountName/>
            <ChargesAccountAgent>
              <FinInstBIC/>
              <FinInstClearingSystemIdentifCode/>
              <FinInstClearingSystemIdentifProprietary/>
              <FinInstMemberIdentification/>
              <FinInstName/>
              <FinInstPostalAdrAddressType/>
              <FinInstPostalAdrDepartment/>
              <FinInstPostalAdrSubDepartment/>
              <FinInstPostalAdrStreetName/>
              <FinInstPostalAdrBuildingNumber/>
              <FinInstPostalAdrPostCode/>
              <FinInstPostalAdrTownName/>
              <FinInstPostalAdrCountrySubDivision/>
              <FinInstPostalAdrCountry/>
              <FinInstPostalAdrAddressLine/>
              <FinInstOtherIdentification/>
              <FinInstOtherSchemeNameCode/>
              <FinInstIOtherSchemeNameProprietary/>
              <FinInstOtherIssuer/>
              <BrnchIdentification/>
              <BrnchIdName/>
              <BrnchPostalAdrAddressType/>
              <BrnchPostalAdrDepartment/>
              <BrnchPostalAdrSubDepartment/>
              <BrnchPostalAdrStreetName/>
              <BrnchPostalAdrBuildingNumber/>
              <BrnchPostalAdrPostCode/>
              <BrnchPostalAdrTownName/>
              <BrnchPostalAdrCountrySubDivision/>
              <BrnchPostalAdrCountry/>
              <BrnchPostalAdrAddressLine/>
            </ChargesAccountAgent>
            <xsl:for-each select="px:CdtTrfTxInf">
              <Transaction>
                <OrderReference/>
                <InstructionIdentification>
                  <xsl:value-of select="px:PmtId/px:InstrId"/>
                </InstructionIdentification>
                <EndToEndIdentification>
                  <xsl:value-of select="px:PmtId/px:EndToEndId"/>
                </EndToEndIdentification>
                <PaymentInstructionPriority>
                  <xsl:value-of select="px:PmtTpInf/px:InstrPrty"/>
                </PaymentInstructionPriority>
                <PaymentServiceLevelCode>
                  <xsl:value-of select="px:PmtTpInf/px:SvcLvl/px:Cd"/>
                </PaymentServiceLevelCode>
                <PaymentServiceLevelProprietary>
                  <xsl:value-of select="px:PmtTpInf/px:SvcLvl/px:Prtry"/>
                </PaymentServiceLevelProprietary>
                <PaymentLocalInstrumentCode>
                  <xsl:value-of select="px:PmtTpInf/px:LclInstrm/px:Cd"/>
                </PaymentLocalInstrumentCode>
                <PaymentLocalInstrumentProprietary>
                  <xsl:value-of select="px:PmtTpInf/px:LclInstrm/px:Prtry"/>
                </PaymentLocalInstrumentProprietary>
                <PaymentCategoryPurposeCode>
                  <xsl:value-of select="px:PmtTpInf/px:CtgyPurp/px:Cd"/>
                </PaymentCategoryPurposeCode>
                <PaymentCategoryPurposeProprietary>
                  <xsl:value-of select="px:PmtTpInf/px:CtgyPurp/px:Prtry"/>
                </PaymentCategoryPurposeProprietary>
                <InstructedAmount>
                  <xsl:value-of select="px:Amt/px:InstdAmt"/>
                </InstructedAmount>
                <InstructedAmountCurrency>
                  <xsl:value-of select="px:Amt/px:InstdAmt/@Ccy"/>
                </InstructedAmountCurrency>
                <EquivalentAmount>
                  <xsl:value-of select="px:Amt/px:EqvtAmt/px:Amt"/>
                </EquivalentAmount>
                <EquivalentAmountCurrency>
                  <xsl:value-of select="px:Amt/px:EqvtAmt/px:Amt/@Ccy"/>
                </EquivalentAmountCurrency>
                <EquivalentAmountCurOfTransfer>
                  <xsl:value-of select="px:Amt/px:EqvtAmt/px:CcyOfTrf"/>
                </EquivalentAmountCurOfTransfer>
                <XchgRateInfExchangeRate>
                  <xsl:value-of select="px:Amt/px:EqvtAmt/px:CcyOfTrf"/>
                </XchgRateInfExchangeRate>
                <XchgRateInfRateType>
                  <xsl:value-of select="px:XchgRateInf/px:RateTp"/>
                </XchgRateInfRateType>
                <XchgRateInfContactInfo>
                  <xsl:value-of select="px:XchgRateInf/px:CtrctId"/>
                </XchgRateInfContactInfo>
                <ChargeBearer>
                  <xsl:value-of select="px:ChrgBr"/>
                </ChargeBearer>
                <ChequeInstruction>
                  <ChequeType>
                    <xsl:value-of select="px:ChqInstr/px:ChqTp"/>
                  </ChequeType>
                  <ChequeNumber/>
                  <ChequeFromName/>
                  <ChequeFromAdrType/>
                  <ChequeFromAdrDepartment/>
                  <ChequeFromAdrSubDepartment/>
                  <ChequeFromAdrStreetName/>
                  <ChequeFromAdrBuildingNumber/>
                  <ChequeFromAdrPostCode/>
                  <ChequeFromAdrTownName/>
                  <ChequeFromAdrCountrySubDivision/>
                  <ChequeFromAdrCountry/>
                  <ChequeFromAddressLine/>
                  <DeliveryMethodCode>
                    <xsl:value-of select="px:ChqInstr/px:DlvryMtd/px:Cd"/>
                  </DeliveryMethodCode>
                  <DeliveryMethodProprietary>
                    <xsl:value-of select="px:ChqInstr/px:DlvryMtd/px:Prtry"/>
                  </DeliveryMethodProprietary>
                  <DeliverToName/>
                  <DeliverToAdrType/>
                  <DeliverToAdrDepartment/>
                  <DeliverToAdrSubDepartment/>
                  <DeliverToAdrStreetName/>
                  <DeliverToAdrBuildingNumber/>
                  <DeliverToAdrPostCode/>
                  <DeliverToAdrTownName/>
                  <DeliverToAdrCountrySubDivision/>
                  <DeliverToAdrCountry/>
                  <DeliverToAddressLine/>
                  <InstructionPriority/>
                  <ChequeMaturityDate/>
                  <FormsCode/>
                  <MemoField/>
                  <RegionalClearingZone/>
                  <PrintLocation/>
                </ChequeInstruction>
                <UltimateDebtor>
                  <Name>
                    <xsl:value-of select="px:UltmtDbtr/px:Nm"/>
                  </Name>
                  <PostalAdrAddressType>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:AdrTp"/>
                  </PostalAdrAddressType>
                  <PostalAdrDepartment>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:Dept"/>
                  </PostalAdrDepartment>
                  <PostalAdrSubDepartment>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:SubDept"/>
                  </PostalAdrSubDepartment>
                  <PostalAdrStreetName>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:StrtNm"/>
                  </PostalAdrStreetName>
                  <PostalAdrBuildingNumber>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:BldgNb"/>
                  </PostalAdrBuildingNumber>
                  <PostalAdrPostCode>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:PstCd"/>
                  </PostalAdrPostCode>
                  <PostalAdrTownName>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:TwnNm"/>
                  </PostalAdrTownName>
                  <PostalAdrCountrySubDivision>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:CtrySubDvsn"/>
                  </PostalAdrCountrySubDivision>
                  <PostalAdrCountryCode>
                    <xsl:value-of select="px:UltmtDbtr/px:PstlAdr/px:Ctry"/>
                  </PostalAdrCountryCode>
                  <xsl:for-each select="px:UltmtDbtr/px:PstlAdr/px:AdrLine">
                    <xsl:if test="7&gt;position()">
                      <PostalAdrAddressLine>
                        <xsl:value-of select="."/>
                      </PostalAdrAddressLine>
                    </xsl:if>
                  </xsl:for-each>
                  <OrgIdentBIC>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:BICOrBEI"/>
                  </OrgIdentBIC>
                  <OrganisIdentOtherIdentification>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:Id"/>
                  </OrganisIdentOtherIdentification>
                  <OrganisIdentOtherCode>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
                  </OrganisIdentOtherCode>
                  <OrganisIdentOtherProprietary>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
                  </OrganisIdentOtherProprietary>
                  <OrganisIdentOtherIssuer>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:OrgId/px:Othr/px:Issr"/>
                  </OrganisIdentOtherIssuer>
                  <PrivateIdentBirthDate>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
                  </PrivateIdentBirthDate>
                  <PrivateIdentProvinceOfBirth>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
                  </PrivateIdentProvinceOfBirth>
                  <PrivateIdentCityOfBirth>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
                  </PrivateIdentCityOfBirth>
                  <PrivateIdentCountryOfBirth>
                    <xsl:value-of select="px:UltmtDbtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
                  </PrivateIdentCountryOfBirth>
                  <xsl:for-each select="px:UltmtDbtr/px:Id/px:PrvtId/px:Othr">
                    <OtherPersonIdentification>         
                    <PrivateIdentOtherIdentification>
                      <xsl:value-of select="px:Id"/>
                    </PrivateIdentOtherIdentification>
                    <PrivateIdentOtherCode>
                      <xsl:value-of select="px:Cd"/>
                    </PrivateIdentOtherCode>
                    <PrivateIdentOtherProprietary>
                      <xsl:value-of select="px:Prtry"/>
                    </PrivateIdentOtherProprietary>
                    <PrivateIdentOtherIssuer>
                      <xsl:value-of select="px:Issr"/>
                    </PrivateIdentOtherIssuer>
                    </OtherPersonIdentification>
                  </xsl:for-each>
                  <CountryOfResidence/>
                  <ContactDetailsNamePrefix/>
                  <ContactDetailsName/>
                  <ContactDetailsPhoneNumber/>
                  <ContactDetailsMobileNumber/>
                  <ContactDetailsFaxNumber/>
                  <ContactDetailsEmailAddress/>
                  <ContactDetailsOther/>
                </UltimateDebtor>
                <IntermediaryAgent1FinancialInstitution>
                  <FinInstBIC>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:BIC"/>
                  </FinInstBIC>
                  <FinInstClearingSystemIdentifCode>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Cd"/>
                  </FinInstClearingSystemIdentifCode>
                  <FinInstClearingSystemIdentifProprietary>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Prtry"/>
                  </FinInstClearingSystemIdentifProprietary>
                  <FinInstMemberIdentification>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:ClrSysMmbId/px:MmbId"/>
                  </FinInstMemberIdentification>
                  <FinInstName>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:Nm"/>
                  </FinInstName>
                  <FinInstPostalAdrAddressType>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:AdrTp"/>
                  </FinInstPostalAdrAddressType>
                  <FinInstPostalAdrDepartment>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:Dept"/>
                  </FinInstPostalAdrDepartment>
                  <FinInstPostalAdrSubDepartment>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:SubDept"/>
                  </FinInstPostalAdrSubDepartment>
                  <FinInstPostalAdrStreetName>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:StrtNm"/>
                  </FinInstPostalAdrStreetName>
                  <FinInstPostalAdrBuildingNumber>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:BldgNb"/>
                  </FinInstPostalAdrBuildingNumber>
                  <FinInstPostalAdrPostCode>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:PstCd"/>
                  </FinInstPostalAdrPostCode>
                  <FinInstPostalAdrTownName>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:TwnNm"/>
                  </FinInstPostalAdrTownName>
                  <FinInstPostalAdrCountrySubDivision>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:CtrySubDvsn"/>
                  </FinInstPostalAdrCountrySubDivision>
                  <FinInstPostalAdrCountry>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:Ctry"/>
                  </FinInstPostalAdrCountry>
                  <xsl:for-each select="px:IntrmyAgt1/px:FinInstnId/px:PstlAdr/px:AdrLine">
                    <xsl:if test="7&gt;position()">
                      <FinInstPostalAdrAddressLine>
                        <xsl:value-of select="."/>
                      </FinInstPostalAdrAddressLine>
                    </xsl:if>
                  </xsl:for-each>
                  <FinInstOtherIdentification>
                    <xsl:value-of select="px:IntrmyAgt1/px:FinInstnId/px:Othr/px:Id"/>
                  </FinInstOtherIdentification>
                  <FinInstOtherSchemeNameCode/>
                  <FinInstIOtherSchemeNameProprietary/>
                  <FinInstOtherIssuer/>
                  <BrnchIdentification/>
                  <BrnchIdName/>
                  <BrnchPostalAdrAddressType/>
                  <BrnchPostalAdrDepartment/>
                  <BrnchPostalAdrSubDepartment/>
                  <BrnchPostalAdrStreetName/>
                  <BrnchPostalAdrBuildingNumber/>
                  <BrnchPostalAdrPostCode/>
                  <BrnchPostalAdrTownName/>
                  <BrnchPostalAdrCountrySubDivision/>
                  <BrnchPostalAdrCountry/>
                  <BrnchPostalAdrAddressLine/>
                </IntermediaryAgent1FinancialInstitution>
                <IntermediaryAgent1Account>
                  <IBAN/>
                  <AccountOtherIdentification/>
                  <AccountOtherIdNameCode/>
                  <AccountOtherIdNameProprietary/>
                  <AccountOtherIdIssuer/>
                  <AccountTypeCode/>
                  <AccountTypeProprietary/>
                  <AccountCurrency/>
                  <AccountName/>
                </IntermediaryAgent1Account>
                <IntermediaryAgent2FinancialInstitution>
                  <FinInstBIC/>
                  <FinInstClearingSystemIdentifCode/>
                  <FinInstClearingSystemIdentifProprietary/>
                  <FinInstMemberIdentification/>
                  <FinInstName/>
                  <FinInstPostalAdrAddressType/>
                  <FinInstPostalAdrDepartment/>
                  <FinInstPostalAdrSubDepartment/>
                  <FinInstPostalAdrStreetName/>
                  <FinInstPostalAdrBuildingNumber/>
                  <FinInstPostalAdrPostCode/>
                  <FinInstPostalAdrTownName/>
                  <FinInstPostalAdrCountrySubDivision/>
                  <FinInstPostalAdrCountry/>
                  <FinInstPostalAdrAddressLine/>
                  <FinInstOtherIdentification/>
                  <FinInstOtherSchemeNameCode/>
                  <FinInstIOtherSchemeNameProprietary/>
                  <FinInstOtherIssuer/>
                  <BrnchIdentification/>
                  <BrnchIdName/>
                  <BrnchPostalAdrAddressType/>
                  <BrnchPostalAdrDepartment/>
                  <BrnchPostalAdrSubDepartment/>
                  <BrnchPostalAdrStreetName/>
                  <BrnchPostalAdrBuildingNumber/>
                  <BrnchPostalAdrPostCode/>
                  <BrnchPostalAdrTownName/>
                  <BrnchPostalAdrCountrySubDivision/>
                  <BrnchPostalAdrCountry/>
                  <BrnchPostalAdrAddressLine/>
                </IntermediaryAgent2FinancialInstitution>
                <IntermediaryAgent2Account>
                  <IBAN/>
                  <AccountOtherIdentification/>
                  <AccountOtherIdNameCode/>
                  <AccountOtherIdNameProprietary/>
                  <AccountOtherIdIssuer/>
                  <AccountTypeCode/>
                  <AccountTypeProprietary/>
                  <AccountCurrency/>
                  <AccountName/>
                </IntermediaryAgent2Account>
                <IntermediaryAgent3FinancialInstitution>
                  <FinInstBIC/>
                  <FinInstClearingSystemIdentifCode/>
                  <FinInstClearingSystemIdentifProprietary/>
                  <FinInstMemberIdentification/>
                  <FinInstName/>
                  <FinInstPostalAdrAddressType/>
                  <FinInstPostalAdrDepartment/>
                  <FinInstPostalAdrSubDepartment/>
                  <FinInstPostalAdrStreetName/>
                  <FinInstPostalAdrBuildingNumber/>
                  <FinInstPostalAdrPostCode/>
                  <FinInstPostalAdrTownName/>
                  <FinInstPostalAdrCountrySubDivision/>
                  <FinInstPostalAdrCountry/>
                  <FinInstPostalAdrAddressLine/>
                  <FinInstOtherIdentification/>
                  <FinInstOtherSchemeNameCode/>
                  <FinInstIOtherSchemeNameProprietary/>
                  <FinInstOtherIssuer/>
                  <BrnchIdentification/>
                  <BrnchIdName/>
                  <BrnchPostalAdrAddressType/>
                  <BrnchPostalAdrDepartment/>
                  <BrnchPostalAdrSubDepartment/>
                  <BrnchPostalAdrStreetName/>
                  <BrnchPostalAdrBuildingNumber/>
                  <BrnchPostalAdrPostCode/>
                  <BrnchPostalAdrTownName/>
                  <BrnchPostalAdrCountrySubDivision/>
                  <BrnchPostalAdrCountry/>
                  <BrnchPostalAdrAddressLine/>
                </IntermediaryAgent3FinancialInstitution>
                <IntermediaryAgent3Account>
                  <IBAN/>
                  <AccountOtherIdentification/>
                  <AccountOtherIdNameCode/>
                  <AccountOtherIdNameProprietary/>
                  <AccountOtherIdIssuer/>
                  <AccountTypeCode/>
                  <AccountTypeProprietary/>
                  <AccountCurrency/>
                  <AccountName/>
                </IntermediaryAgent3Account>
                <CreditorAgentFinancialInstitution>
                  <FinInstBIC>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:BIC"/>
                  </FinInstBIC>
                  <FinInstClearingSystemIdentifCode>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Cd"/>
                  </FinInstClearingSystemIdentifCode>
                  <FinInstClearingSystemIdentifProprietary>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:ClrSysMmbId/px:ClrSysId/px:Prtry"/>
                  </FinInstClearingSystemIdentifProprietary>
                  <FinInstMemberIdentification>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:ClrSysMmbId/px:MmbId"/>
                  </FinInstMemberIdentification>
                  <FinInstName>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:Nm"/>
                  </FinInstName>
                  <FinInstPostalAdrAddressType>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:AdrTp"/>
                  </FinInstPostalAdrAddressType>
                  <FinInstPostalAdrDepartment>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:Dept"/>
                  </FinInstPostalAdrDepartment>
                  <FinInstPostalAdrSubDepartment>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:SubDept"/>
                  </FinInstPostalAdrSubDepartment>
                  <FinInstPostalAdrStreetName>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:StrtNm"/>
                  </FinInstPostalAdrStreetName>
                  <FinInstPostalAdrBuildingNumber>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:BldgNb"/>
                  </FinInstPostalAdrBuildingNumber>
                  <FinInstPostalAdrPostCode>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:PstCd"/>
                  </FinInstPostalAdrPostCode>
                  <FinInstPostalAdrTownName>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:TwnNm"/>
                  </FinInstPostalAdrTownName>
                  <FinInstPostalAdrCountrySubDivision>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:CtrySubDvsn"/>
                  </FinInstPostalAdrCountrySubDivision>
                  <FinInstPostalAdrCountry>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:Ctry"/>
                  </FinInstPostalAdrCountry>
                  <xsl:for-each select="px:CdtrAgt/px:FinInstnId/px:PstlAdr/px:AdrLine">
                    <xsl:if test="7&gt;position()">
                      <FinInstPostalAdrAddressLine>
                        <xsl:value-of select="."/>
                      </FinInstPostalAdrAddressLine>
                    </xsl:if>
                  </xsl:for-each>
                  <FinInstOtherIdentification>
                    <xsl:value-of select="px:CdtrAgt/px:FinInstnId/px:Othr/px:Id"/>
                  </FinInstOtherIdentification>
                  <FinInstOtherSchemeNameCode/>
                  <FinInstIOtherSchemeNameProprietary/>
                  <FinInstOtherIssuer/>
                  <BrnchIdentification/>
                  <BrnchIdName/>
                  <BrnchPostalAdrAddressType/>
                  <BrnchPostalAdrDepartment/>
                  <BrnchPostalAdrSubDepartment/>
                  <BrnchPostalAdrStreetName/>
                  <BrnchPostalAdrBuildingNumber/>
                  <BrnchPostalAdrPostCode/>
                  <BrnchPostalAdrTownName/>
                  <BrnchPostalAdrCountrySubDivision/>
                  <BrnchPostalAdrCountry/>
                  <BrnchPostalAdrAddressLine/>
                </CreditorAgentFinancialInstitution>
                <CreditorAgentAccount>
                  <IBAN/>
                  <AccountOtherIdentification/>
                  <AccountOtherIdNameCode/>
                  <AccountOtherIdNameProprietary/>
                  <AccountOtherIdIssuer/>
                  <AccountTypeCode/>
                  <AccountTypeProprietary/>
                  <AccountCurrency/>
                  <AccountName/>
                </CreditorAgentAccount>
                <CreditorPartyIdentification>
                  <Name>
                    <xsl:value-of select="px:Cdtr/px:Nm"/>
                  </Name>
                  <PostalAdrAddressType>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:AdrTp"/>
                  </PostalAdrAddressType>
                  <PostalAdrDepartment>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:Dept"/>
                  </PostalAdrDepartment>
                  <PostalAdrSubDepartment>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:SubDept"/>
                  </PostalAdrSubDepartment>
                  <PostalAdrStreetName>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:StrtNm"/>
                  </PostalAdrStreetName>
                  <PostalAdrBuildingNumber>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:BldgNb"/>
                  </PostalAdrBuildingNumber>
                  <PostalAdrPostCode>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:PstCd"/>
                  </PostalAdrPostCode>
                  <PostalAdrTownName>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:TwnNm"/>
                  </PostalAdrTownName>
                  <PostalAdrCountrySubDivision>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:CtrySubDvsn"/>
                  </PostalAdrCountrySubDivision>
                  <PostalAdrCountryCode>
                    <xsl:value-of select="px:Cdtr/px:PstlAdr/px:Ctry"/>
                  </PostalAdrCountryCode>
                  <xsl:for-each select="px:Cdtr/px:PstlAdr/px:AdrLine">
                    <xsl:if test="7&gt;position()">
                      <PostalAdrAddressLine>
                        <xsl:value-of select="."/>
                      </PostalAdrAddressLine>
                    </xsl:if>
                  </xsl:for-each>
                  <OrgIdentBIC>
                    <xsl:value-of select="px:Cdtr/px:Id/px:OrgId/px:BICOrBEI"/>
                  </OrgIdentBIC>
                  <OrganisIdentOtherIdentification>
                    <xsl:value-of select="px:Cdtr/px:Id/px:OrgId/px:Othr/px:Id"/>
                  </OrganisIdentOtherIdentification>
                  <OrganisIdentOtherCode>
                    <xsl:value-of select="px:Cdtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
                  </OrganisIdentOtherCode>
                  <OrganisIdentOtherProprietary>
                    <xsl:value-of select="px:Cdtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
                  </OrganisIdentOtherProprietary>
                  <OrganisIdentOtherIssuer>
                    <xsl:value-of select="px:Cdtr/px:Id/px:OrgId/px:Othr/px:Issr"/>
                  </OrganisIdentOtherIssuer>
                  <PrivateIdentBirthDate>
                    <xsl:value-of select="px:Cdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
                  </PrivateIdentBirthDate>
                  <PrivateIdentProvinceOfBirth>
                    <xsl:value-of select="px:Cdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
                  </PrivateIdentProvinceOfBirth>
                  <PrivateIdentCityOfBirth>
                    <xsl:value-of select="px:Cdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
                  </PrivateIdentCityOfBirth>
                  <PrivateIdentCountryOfBirth>
                    <xsl:value-of select="px:Cdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
                  </PrivateIdentCountryOfBirth>
                  <xsl:for-each select="px:Cdtr/px:Id/px:PrvtId/px:Othr">
                    <OtherPersonIdentification>
                    <PrivateIdentOtherIdentification>
                      <xsl:value-of select="px:Id"/>
                    </PrivateIdentOtherIdentification>
                    <PrivateIdentOtherCode>
                      <xsl:value-of select="px:Cd"/>
                    </PrivateIdentOtherCode>
                    <PrivateIdentOtherProprietary>
                      <xsl:value-of select="px:Prtry"/>
                    </PrivateIdentOtherProprietary>
                    <PrivateIdentOtherIssuer>
                      <xsl:value-of select="px:Issr"/>
                    </PrivateIdentOtherIssuer>
                    </OtherPersonIdentification>
                  </xsl:for-each>
                  <CountryOfResidence/>
                  <ContactDetailsNamePrefix/>
                  <ContactDetailsName/>
                  <ContactDetailsPhoneNumber/>
                  <ContactDetailsMobileNumber/>
                  <ContactDetailsFaxNumber/>
                  <ContactDetailsEmailAddress/>
                  <ContactDetailsOther/>
                </CreditorPartyIdentification>
                <CreditorAccountIBAN>
                  <xsl:value-of select="px:CdtrAcct/px:Id/px:IBAN"/>
                </CreditorAccountIBAN>
                <CreditorAccountOtherIdentification>
                  <xsl:value-of select="px:CdtrAcct/px:Id/px:Othr/px:Id"/>
                </CreditorAccountOtherIdentification>
                <CreditorAccountOtherIdNameCode/>
                <CreditorAccountOtherIdNameProprietary/>
                <CreditorAccountOtherIdIssuer/>
                <CreditorAccountTypeCode/>
                <CreditorAccountTypeProprietary/>
                <CreditorAccountCurrency/>
                <CreditorAccountName/>
                <UltimateCreditorPartyIdentification>
                  <Name>
                    <xsl:value-of select="px:UltmtCdtr/px:Nm"/>
                  </Name>
                  <PostalAdrAddressType>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:AdrTp"/>
                  </PostalAdrAddressType>
                  <PostalAdrDepartment>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:Dept"/>
                  </PostalAdrDepartment>
                  <PostalAdrSubDepartment>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:SubDept"/>
                  </PostalAdrSubDepartment>
                  <PostalAdrStreetName>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:StrtNm"/>
                  </PostalAdrStreetName>
                  <PostalAdrBuildingNumber>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:BldgNb"/>
                  </PostalAdrBuildingNumber>
                  <PostalAdrPostCode>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:PstCd"/>
                  </PostalAdrPostCode>
                  <PostalAdrTownName>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:TwnNm"/>
                  </PostalAdrTownName>
                  <PostalAdrCountrySubDivision>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:CtrySubDvsn"/>
                  </PostalAdrCountrySubDivision>
                  <PostalAdrCountryCode>
                    <xsl:value-of select="px:UltmtCdtr/px:PstlAdr/px:Ctry"/>
                  </PostalAdrCountryCode>
                  <xsl:for-each select="px:UltmtCdtr/px:PstlAdr/px:AdrLine">
                    <xsl:if test="7&gt;position()">
                      <PostalAdrAddressLine>
                        <xsl:value-of select="."/>
                      </PostalAdrAddressLine>
                    </xsl:if>
                  </xsl:for-each>
                  <OrgIdentBIC>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:OrgId/px:BICOrBEI"/>
                  </OrgIdentBIC>
                  <OrganisIdentOtherIdentification>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:OrgId/px:Othr/px:Id"/>
                  </OrganisIdentOtherIdentification>
                  <OrganisIdentOtherCode>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
                  </OrganisIdentOtherCode>
                  <OrganisIdentOtherProprietary>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
                  </OrganisIdentOtherProprietary>
                  <OrganisIdentOtherIssuer>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:OrgId/px:Othr/px:Issr"/>
                  </OrganisIdentOtherIssuer>
                  <PrivateIdentBirthDate>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
                  </PrivateIdentBirthDate>
                  <PrivateIdentProvinceOfBirth>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
                  </PrivateIdentProvinceOfBirth>
                  <PrivateIdentCityOfBirth>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
                  </PrivateIdentCityOfBirth>
                  <PrivateIdentCountryOfBirth>
                    <xsl:value-of select="px:UltmtCdtr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
                  </PrivateIdentCountryOfBirth>
                  <xsl:for-each select="px:UltmtCdtr/px:Id/px:PrvtId/px:Othr">
                    <OtherPersonIdentification>
                    <PrivateIdentOtherIdentification>
                      <xsl:value-of select="px:Id"/>
                    </PrivateIdentOtherIdentification>
                    <PrivateIdentOtherCode>
                      <xsl:value-of select="px:Cd"/>
                    </PrivateIdentOtherCode>
                    <PrivateIdentOtherProprietary>
                      <xsl:value-of select="px:Prtry"/>
                    </PrivateIdentOtherProprietary>
                    <PrivateIdentOtherIssuer>
                      <xsl:value-of select="px:Issr"/>
                    </PrivateIdentOtherIssuer>
                    </OtherPersonIdentification>
                  </xsl:for-each>
                  <CountryOfResidence/>
                  <ContactDetailsNamePrefix/>
                  <ContactDetailsName/>
                  <ContactDetailsPhoneNumber/>
                  <ContactDetailsMobileNumber/>
                  <ContactDetailsFaxNumber/>
                  <ContactDetailsEmailAddress/>
                  <ContactDetailsOther/>
                </UltimateCreditorPartyIdentification>
                <xsl:for-each select="px:InstrForCdtrAgt">
                  <InstructionForCreditorAgent>
                    <Code>
                      <xsl:value-of select="px:Cd"/>
                    </Code>
                    <InstructionInformation>
                      <xsl:value-of select="px:InstrInf"/>
                    </InstructionInformation>
                  </InstructionForCreditorAgent>
                </xsl:for-each>
                <InstructionForDebtorAgent>
                  <xsl:value-of select="px:InstrForDbtrAgt"/>
                </InstructionForDebtorAgent>
                <PurposeCode>
                  <xsl:value-of select="px:Purp/px:Cd"/>
                </PurposeCode>
                <PurposeProprietary/>
                <xsl:for-each select="px:RgltryRptg">
                  <xsl:if test="10&gt;position()">
                    <RegulatoryReporting>
                      <DebitCreditReportingIndicator>
                        <xsl:value-of select="px:DbtCdtRptgInd"/>
                      </DebitCreditReportingIndicator>
                      <AuthorityName>
                        <xsl:value-of select="px:Authrty/px:Nm"/>
                      </AuthorityName>
                      <AuthorityCountry>
                        <xsl:value-of select="px:Authrty/px:Ctry"/>
                      </AuthorityCountry>
                      <xsl:for-each select="px:Dtls">
                        <Details>
                          <Type>
                            <xsl:value-of select="px:Tp"/>
                          </Type>
                          <Date>
                            <xsl:value-of select="px:Dt"/>
                          </Date>
                          <Country>
                            <xsl:value-of select="px:Ctry"/>
                          </Country>
                          <Code>
                            <xsl:value-of select="px:Cd"/>
                          </Code>
                          <Amount>
                            <xsl:value-of select="px:Amt"/>
                          </Amount>
                          <Currency>
                            <xsl:value-of select="px:Amt/@Ccy"/>
                          </Currency>
                          <xsl:for-each select="px:Inf">
                            <Information>
                              <Information>
                                <xsl:value-of select="."/>
                              </Information>
                            </Information>
                          </xsl:for-each>
                        </Details>
                      </xsl:for-each>
                    </RegulatoryReporting>
                  </xsl:if>
                </xsl:for-each>
                <Tax>
                  <CreditorTaxIdentification/>
                  <CreditorRegistrationIdentification/>
                  <CreditorTaxType/>
                  <DebtorTaxIdentification/>
                  <DebtorRegistrationIdentification/>
                  <DebtorTaxType/>
                  <DebtorAuthorisationTitle/>
                  <DebtorAuthorisationName/>
                  <AdministrationZone/>
                  <ReferenceNumber/>
                  <Method/>
                  <TotalTaxableBaseAmount/>
                  <TotalTaxableBaseAmountCurrency/>
                  <TotalTaxAmount/>
                  <TotalTaxAmountCurrency/>
                  <Date/>
                  <SequenceNumber/>
                  <TaxRecord>
                    <Type/>
                    <Category/>
                    <CategoryDetails/>
                    <DebtorStatus/>
                    <CertificateIdentification/>
                    <FormsCode/>
                    <PeriodYear/>
                    <PeriodType/>
                    <PeriodFromDate/>
                    <PeriodToDate/>
                    <TaxAmountRate/>
                    <TaxableBaseAmount/>
                    <TaxableBaseAmountCurrency/>
                    <TotalAmount/>
                    <TotalAmountCurrency/>
                    <TaxAmountDetails>
                      <PeriodYear/>
                      <PeriodType/>
                      <FromDate/>
                      <ToDate/>
                      <Amount/>
                      <Currency/>
                    </TaxAmountDetails>
                    <AdditionalInformation/>
                  </TaxRecord>
                </Tax>
                <RemittanceInformation>
                  <Unstructured>
                    <xsl:value-of select="px:RmtInf/px:Ustrd"/>
                  </Unstructured>
                  <xsl:for-each select="px:RmtInf/px:Strd/px:RfrdDocInf">
                    <RemitReferredDocInfo>
                      <TypeCode>
                        <xsl:value-of select="px:Tp/px:CdOrPrtry/px:Cd"/>
                      </TypeCode>
                      <TypeProprietary>
                        <xsl:value-of select="px:Tp/px:CdOrPrtry/px:Prtry"/>
                      </TypeProprietary>
                      <TypeIssuer>
                        <xsl:value-of select="px:Tp/px:Issr"/>
                      </TypeIssuer>
                      <Number>
                        <xsl:value-of select="px:Nb"/>
                      </Number>
                      <RelatedDate>
                        <xsl:value-of select="px:RltdDt"/>
                      </RelatedDate>
                    </RemitReferredDocInfo>
                  </xsl:for-each>
                  <ReferredDocAmountDuePayableAmount>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:DuePyblAmt"/>
                  </ReferredDocAmountDuePayableAmount>
                  <ReferredDocAmountDuePayableCurrency>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:DuePyblAmt/@Ccy"/>
                  </ReferredDocAmountDuePayableCurrency>
                  <ReferredDocAmountDiscountAppliedAmount>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:DscntApldAmt"/>
                  </ReferredDocAmountDiscountAppliedAmount>
                  <ReferredDocAmountDiscountAppliedCurrency>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:DscntApldAmt/@Ccy"/>
                  </ReferredDocAmountDiscountAppliedCurrency>
                  <ReferredDocAmountCreditNoteAmount>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:CdtNoteAmt"/>
                  </ReferredDocAmountCreditNoteAmount>
                  <ReferredDocAmountCreditNoteCurrency>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:CdtNoteAmt/@Ccy"/>
                  </ReferredDocAmountCreditNoteCurrency>
                  <ReferredDocAmountTaxAmount>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:TaxAmt"/>
                  </ReferredDocAmountTaxAmount>
                  <ReferredDocAmountTaxCurrency>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:TaxAmt/@Ccy"/>
                  </ReferredDocAmountTaxCurrency>
                  <xsl:for-each select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:AdjstmntAmtAndRsn">
                    <PayRemitAdjustmentAmountAndReason>
                      <Amount>
                        <xsl:value-of select="px:Amt"/>
                      </Amount>
                      <Currency>
                        <xsl:value-of select="px:Amt/@Ccy"/>
                      </Currency>
                      <CreditDebitIndicator>
                        <xsl:value-of select="px:CdtDbtInd"/>
                      </CreditDebitIndicator>
                      <Reason>
                        <xsl:value-of select="px:Rsn"/>
                      </Reason>
                      <AdditionalInformation>
                        <xsl:value-of select="px:AddtlInf"/>
                      </AdditionalInformation>
                    </PayRemitAdjustmentAmountAndReason>
                  </xsl:for-each>
                  <ReferredDocAmountRemittedAmount>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:RmtdAmt"/>
                  </ReferredDocAmountRemittedAmount>
                  <ReferredDocAmountRemittedCurrency>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:RfrdDocAmt/px:RmtdAmt/@Ccy"/>
                  </ReferredDocAmountRemittedCurrency>
                  <CreditorReferenceInformationCode>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:CdtrRefInf/px:Tp/px:CdOrPrtry/px:Cd"/>
                  </CreditorReferenceInformationCode>
                  <CreditorReferenceInformationProprietary>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:CdtrRefInf/px:Tp/px:CdOrPrtry/px:Prtry"/>
                  </CreditorReferenceInformationProprietary>
                  <CreditorReferenceInformationIssuer>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:CdtrRefInf/px:Tp/px:Issr"/>
                  </CreditorReferenceInformationIssuer>
                  <CreditorReferenceInformationReference>
                    <xsl:value-of select="px:RmtInf/px:Strd/px:CdtrRefInf/px:Ref"/>
                  </CreditorReferenceInformationReference>
                  <InvoicerPartyIdentification>
                    <Name>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Nm"/>
                    </Name>
                    <PostalAdrAddressType>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:AdrTp"/>
                    </PostalAdrAddressType>
                    <PostalAdrDepartment>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:Dept"/>
                    </PostalAdrDepartment>
                    <PostalAdrSubDepartment>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:SubDept"/>
                    </PostalAdrSubDepartment>
                    <PostalAdrStreetName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:StrtNm"/>
                    </PostalAdrStreetName>
                    <PostalAdrBuildingNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:BldgNb"/>
                    </PostalAdrBuildingNumber>
                    <PostalAdrPostCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:PstCd"/>
                    </PostalAdrPostCode>
                    <PostalAdrTownName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:TwnNm"/>
                    </PostalAdrTownName>
                    <PostalAdrCountrySubDivision>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:CtrySubDvsn"/>
                    </PostalAdrCountrySubDivision>
                    <PostalAdrCountryCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:Ctry"/>
                    </PostalAdrCountryCode>
                    <xsl:for-each select="px:RmtInf/px:Strd/px:Invcr/px:PstlAdr/px:AdrLine">
                      <xsl:if test="7&gt;position()">
                        <PostalAdrAddressLine>
                          <xsl:value-of select="."/>
                        </PostalAdrAddressLine>
                      </xsl:if>
                    </xsl:for-each>
                    <OrgIdentBIC>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:OrgId/px:BICOrBEI"/>
                    </OrgIdentBIC>
                    <OrganisIdentOtherIdentification>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:OrgId/px:Othr/px:Id"/>
                    </OrganisIdentOtherIdentification>
                    <OrganisIdentOtherCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
                    </OrganisIdentOtherCode>
                    <OrganisIdentOtherProprietary/>
                    <OrganisIdentOtherIssuer>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:OrgId/px:Othr/px:Issr"/>
                    </OrganisIdentOtherIssuer>
                    <PrivateIdentBirthDate>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
                    </PrivateIdentBirthDate>
                    <PrivateIdentProvinceOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
                    </PrivateIdentProvinceOfBirth>
                    <PrivateIdentCityOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
                    </PrivateIdentCityOfBirth>
                    <PrivateIdentCountryOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
                    </PrivateIdentCountryOfBirth>
                    <xsl:for-each select="px:RmtInf/px:Strd/px:Invcr/px:Id/px:PrvtId/px:Othr">
                      <OtherPersonIdentification>
                      <PrivateIdentOtherIdentification>
                        <xsl:value-of select="px:Id"/>
                      </PrivateIdentOtherIdentification>
                      <PrivateIdentOtherCode>
                        <xsl:value-of select="px:Cd"/>
                      </PrivateIdentOtherCode>
                      <PrivateIdentOtherProprietary>
                        <xsl:value-of select="px:Prtry"/>
                      </PrivateIdentOtherProprietary>
                      <PrivateIdentOtherIssuer>
                        <xsl:value-of select="px:Issr"/>
                      </PrivateIdentOtherIssuer>
                      </OtherPersonIdentification>
                    </xsl:for-each>
                    <CountryOfResidence>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtryOfRes"/>
                    </CountryOfResidence>
                    <ContactDetailsNamePrefix>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:NmPrfx"/>
                    </ContactDetailsNamePrefix>
                    <ContactDetailsName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:Nm"/>
                    </ContactDetailsName>
                    <ContactDetailsPhoneNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:PhneNb"/>
                    </ContactDetailsPhoneNumber>
                    <ContactDetailsMobileNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:MobNb"/>
                    </ContactDetailsMobileNumber>
                    <ContactDetailsFaxNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:FaxNb"/>
                    </ContactDetailsFaxNumber>
                    <ContactDetailsEmailAddress>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:EmailAdr"/>
                    </ContactDetailsEmailAddress>
                    <ContactDetailsOther>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcr/px:CtctDtls/px:Othr"/>
                    </ContactDetailsOther>
                  </InvoicerPartyIdentification>
                  <InvoiceePartyIdentification>
                    <Name>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Nm"/>
                    </Name>
                    <PostalAdrAddressType>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:AdrTp"/>
                    </PostalAdrAddressType>
                    <PostalAdrDepartment>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:Dept"/>
                    </PostalAdrDepartment>
                    <PostalAdrSubDepartment>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:SubDept"/>
                    </PostalAdrSubDepartment>
                    <PostalAdrStreetName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:StrtNm"/>
                    </PostalAdrStreetName>
                    <PostalAdrBuildingNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:BldgNb"/>
                    </PostalAdrBuildingNumber>
                    <PostalAdrPostCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:PstCd"/>
                    </PostalAdrPostCode>
                    <PostalAdrTownName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:TwnNm"/>
                    </PostalAdrTownName>
                    <PostalAdrCountrySubDivision>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:CtrySubDvsn"/>
                    </PostalAdrCountrySubDivision>
                    <PostalAdrCountryCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:Ctry"/>
                    </PostalAdrCountryCode>
                    <xsl:for-each select="px:RmtInf/px:Strd/px:Invcee/px:PstlAdr/px:AdrLine">
                      <xsl:if test="7&gt;position()">
                        <PostalAdrAddressLine>
                          <xsl:value-of select="."/>
                        </PostalAdrAddressLine>
                      </xsl:if>
                    </xsl:for-each>
                    <OrgIdentBIC>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:OrgId/px:BICOrBEI"/>
                    </OrgIdentBIC>
                    <OrganisIdentOtherIdentification>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:OrgId/px:Othr/px:Id"/>
                    </OrganisIdentOtherIdentification>
                    <OrganisIdentOtherCode>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Cd"/>
                    </OrganisIdentOtherCode>
                    <OrganisIdentOtherProprietary>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:OrgId/px:Othr/px:SchmeNm/px:Prtry"/>
                    </OrganisIdentOtherProprietary>
                    <OrganisIdentOtherIssuer/>
                    <PrivateIdentBirthDate>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:BirthDt"/>
                    </PrivateIdentBirthDate>
                    <PrivateIdentProvinceOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:PrvcOfBirth"/>
                    </PrivateIdentProvinceOfBirth>
                    <PrivateIdentCityOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CityOfBirth"/>
                    </PrivateIdentCityOfBirth>
                    <PrivateIdentCountryOfBirth>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:PrvtId/px:DtAndPlcOfBirth/px:CtryOfBirth"/>
                    </PrivateIdentCountryOfBirth>
                    <xsl:for-each select="px:RmtInf/px:Strd/px:Invcee/px:Id/px:PrvtId/px:Othr">
                      <OtherPersonIdentification>
                      <PrivateIdentOtherIdentification>
                        <xsl:value-of select="px:Id"/>
                      </PrivateIdentOtherIdentification>
                      <PrivateIdentOtherCode>
                        <xsl:value-of select="px:Cd"/>
                      </PrivateIdentOtherCode>
                      <PrivateIdentOtherProprietary>
                        <xsl:value-of select="px:Prtry"/>
                      </PrivateIdentOtherProprietary>
                      <PrivateIdentOtherIssuer>
                        <xsl:value-of select="px:Issr"/>
                      </PrivateIdentOtherIssuer>
                      </OtherPersonIdentification>
                    </xsl:for-each>
                    <CountryOfResidence>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtryOfRes"/>
                    </CountryOfResidence>
                    <ContactDetailsNamePrefix>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:NmPrfx"/>
                    </ContactDetailsNamePrefix>
                    <ContactDetailsName>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:Nm"/>
                    </ContactDetailsName>
                    <ContactDetailsPhoneNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:PhneNb"/>
                    </ContactDetailsPhoneNumber>
                    <ContactDetailsMobileNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:MobNb"/>
                    </ContactDetailsMobileNumber>
                    <ContactDetailsFaxNumber>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:FaxNb"/>
                    </ContactDetailsFaxNumber>
                    <ContactDetailsEmailAddress>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:EmailAdr"/>
                    </ContactDetailsEmailAddress>
                    <ContactDetailsOther>
                      <xsl:value-of select="px:RmtInf/px:Strd/px:Invcee/px:CtctDtls/px:Othr"/>
                    </ContactDetailsOther>
                  </InvoiceePartyIdentification>
                  <xsl:for-each select="px:RmtInf/px:Strd/px:AddtlRmtInf">
                    <xsl:if test="3&gt;position()">
                      <AdditionalRemittanceInformation>
                        <xsl:value-of select="."/>
                      </AdditionalRemittanceInformation>
                    </xsl:if>
                  </xsl:for-each>
                </RemittanceInformation>
              </Transaction>
            </xsl:for-each>
        </CreditTransfer>          
        </PayContainer>
      </xsl:for-each>
      <ParkDataHolder/>
    </px:PayOrder>
  </xsl:template>
</xsl:stylesheet>
