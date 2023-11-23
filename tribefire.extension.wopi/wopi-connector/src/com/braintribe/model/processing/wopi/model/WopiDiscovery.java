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
package com.braintribe.model.processing.wopi.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Response of MS Web App which operations are supported
 */
/**
 * <p>
 * Java-Klasse for anonymous complex type.
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="net-zone">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="app" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="action" maxOccurs="unbounded">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="ext" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                                     &lt;attribute name="urlsrc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="requires" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="progid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="useParent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                                     &lt;attribute name="newprogid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="newext" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="favIconUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="checkLicense" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="proof-key">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="oldvalue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "netZone", "proofKey" })
@XmlRootElement(name = "wopi-discovery")
public class WopiDiscovery {

	@XmlElement(name = "net-zone", required = true)
	protected WopiDiscovery.NetZone netZone;
	@XmlElement(name = "proof-key", required = true)
	protected WopiDiscovery.ProofKey proofKey;

	/**
	 * Ruft den Wert der netZone-Eigenschaft ab.
	 * 
	 * @return possible object is {@link WopiDiscovery.NetZone }
	 * 
	 */
	public WopiDiscovery.NetZone getNetZone() {
		return netZone;
	}

	/**
	 * Legt den Wert der netZone-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link WopiDiscovery.NetZone }
	 * 
	 */
	public void setNetZone(WopiDiscovery.NetZone value) {
		this.netZone = value;
	}

	/**
	 * Ruft den Wert der proofKey-Eigenschaft ab.
	 * 
	 * @return possible object is {@link WopiDiscovery.ProofKey }
	 * 
	 */
	public WopiDiscovery.ProofKey getProofKey() {
		return proofKey;
	}

	/**
	 * Legt den Wert der proofKey-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link WopiDiscovery.ProofKey }
	 * 
	 */
	public void setProofKey(WopiDiscovery.ProofKey value) {
		this.proofKey = value;
	}

	/**
	 * <p>
	 * Java-Klasse for anonymous complex type.
	 * 
	 * <p>
	 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="app" maxOccurs="unbounded">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="action" maxOccurs="unbounded">
	 *                     &lt;complexType>
	 *                       &lt;complexContent>
	 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="ext" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" />
	 *                           &lt;attribute name="urlsrc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="requires" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="progid" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="useParent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
	 *                           &lt;attribute name="newprogid" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                           &lt;attribute name="newext" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                         &lt;/restriction>
	 *                       &lt;/complexContent>
	 *                     &lt;/complexType>
	 *                   &lt;/element>
	 *                 &lt;/sequence>
	 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                 &lt;attribute name="favIconUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                 &lt;attribute name="checkLicense" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *       &lt;/sequence>
	 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "app" })
	public static class NetZone {

		@XmlElement(required = true)
		protected List<WopiDiscovery.NetZone.App> app;
		@XmlAttribute(name = "name", required = true)
		protected String name;

		/**
		 * Gets the value of the app property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
		 * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
		 * method for the app property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getApp().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link WopiDiscovery.NetZone.App }
		 * 
		 * 
		 */
		public List<WopiDiscovery.NetZone.App> getApp() {
			if (app == null) {
				app = new ArrayList<WopiDiscovery.NetZone.App>();
			}
			return this.app;
		}

		/**
		 * Ruft den Wert der name-Eigenschaft ab.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getName() {
			return name;
		}

		/**
		 * Legt den Wert der name-Eigenschaft fest.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setName(String value) {
			this.name = value;
		}

		/**
		 * <p>
		 * Java-Klasse for anonymous complex type.
		 * 
		 * <p>
		 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
		 * 
		 * <pre>
		 * &lt;complexType>
		 *   &lt;complexContent>
		 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *       &lt;sequence>
		 *         &lt;element name="action" maxOccurs="unbounded">
		 *           &lt;complexType>
		 *             &lt;complexContent>
		 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="ext" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" />
		 *                 &lt;attribute name="urlsrc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="requires" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="progid" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="useParent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
		 *                 &lt;attribute name="newprogid" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *                 &lt;attribute name="newext" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *               &lt;/restriction>
		 *             &lt;/complexContent>
		 *           &lt;/complexType>
		 *         &lt;/element>
		 *       &lt;/sequence>
		 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *       &lt;attribute name="favIconUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
		 *       &lt;attribute name="checkLicense" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 * 
		 * 
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "action" })
		public static class App {

			@XmlElement(required = true)
			protected List<WopiDiscovery.NetZone.App.Action> action;
			@XmlAttribute(name = "name", required = true)
			protected String name;
			@XmlAttribute(name = "favIconUrl", required = true)
			protected String favIconUrl;
			@XmlAttribute(name = "checkLicense", required = true)
			protected boolean checkLicense;

			/**
			 * Gets the value of the action property.
			 * 
			 * <p>
			 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
			 * make to the returned list will be present inside the JAXB object. This is why there is not a
			 * <CODE>set</CODE> method for the action property.
			 * 
			 * <p>
			 * For example, to add a new item, do as follows:
			 * 
			 * <pre>
			 * getAction().add(newItem);
			 * </pre>
			 * 
			 * 
			 * <p>
			 * Objects of the following type(s) are allowed in the list {@link WopiDiscovery.NetZone.App.Action }
			 * 
			 * 
			 */
			public List<WopiDiscovery.NetZone.App.Action> getAction() {
				if (action == null) {
					action = new ArrayList<WopiDiscovery.NetZone.App.Action>();
				}
				return this.action;
			}

			/**
			 * Ruft den Wert der name-Eigenschaft ab.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getName() {
				return name;
			}

			/**
			 * Legt den Wert der name-Eigenschaft fest.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setName(String value) {
				this.name = value;
			}

			/**
			 * Ruft den Wert der favIconUrl-Eigenschaft ab.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getFavIconUrl() {
				return favIconUrl;
			}

			/**
			 * Legt den Wert der favIconUrl-Eigenschaft fest.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setFavIconUrl(String value) {
				this.favIconUrl = value;
			}

			/**
			 * Ruft den Wert der checkLicense-Eigenschaft ab.
			 * 
			 */
			public boolean isCheckLicense() {
				return checkLicense;
			}

			/**
			 * Legt den Wert der checkLicense-Eigenschaft fest.
			 * 
			 */
			public void setCheckLicense(boolean value) {
				this.checkLicense = value;
			}

			/**
			 * <p>
			 * Java-Klasse for anonymous complex type.
			 * 
			 * <p>
			 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
			 * 
			 * <pre>
			 * &lt;complexType>
			 *   &lt;complexContent>
			 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
			 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="ext" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" />
			 *       &lt;attribute name="urlsrc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="requires" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="progid" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="useParent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
			 *       &lt;attribute name="newprogid" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *       &lt;attribute name="newext" type="{http://www.w3.org/2001/XMLSchema}string" />
			 *     &lt;/restriction>
			 *   &lt;/complexContent>
			 * &lt;/complexType>
			 * </pre>
			 * 
			 * 
			 */
			@XmlAccessorType(XmlAccessType.FIELD)
			@XmlType(name = "")
			public static class Action {

				@XmlAttribute(name = "name", required = true)
				protected String name;
				@XmlAttribute(name = "ext")
				protected String ext;
				@XmlAttribute(name = "default")
				protected Boolean _default;
				@XmlAttribute(name = "urlsrc", required = true)
				protected String urlsrc;
				@XmlAttribute(name = "requires")
				protected String requires;
				@XmlAttribute(name = "progid")
				protected String progid;
				@XmlAttribute(name = "useParent")
				protected Boolean useParent;
				@XmlAttribute(name = "newprogid")
				protected String newprogid;
				@XmlAttribute(name = "newext")
				protected String newext;

				/**
				 * Ruft den Wert der name-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getName() {
					return name;
				}

				/**
				 * Legt den Wert der name-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setName(String value) {
					this.name = value;
				}

				/**
				 * Ruft den Wert der ext-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getExt() {
					return ext;
				}

				/**
				 * Legt den Wert der ext-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setExt(String value) {
					this.ext = value;
				}

				/**
				 * Ruft den Wert der default-Eigenschaft ab.
				 * 
				 * @return possible object is {@link Boolean }
				 * 
				 */
				public Boolean isDefault() {
					return _default;
				}

				/**
				 * Legt den Wert der default-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link Boolean }
				 * 
				 */
				public void setDefault(Boolean value) {
					this._default = value;
				}

				/**
				 * Ruft den Wert der urlsrc-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getUrlsrc() {
					return urlsrc;
				}

				/**
				 * Legt den Wert der urlsrc-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setUrlsrc(String value) {
					this.urlsrc = value;
				}

				/**
				 * Ruft den Wert der requires-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getRequires() {
					return requires;
				}

				/**
				 * Legt den Wert der requires-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setRequires(String value) {
					this.requires = value;
				}

				/**
				 * Ruft den Wert der progid-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getProgid() {
					return progid;
				}

				/**
				 * Legt den Wert der progid-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setProgid(String value) {
					this.progid = value;
				}

				/**
				 * Ruft den Wert der useParent-Eigenschaft ab.
				 * 
				 * @return possible object is {@link Boolean }
				 * 
				 */
				public Boolean isUseParent() {
					return useParent;
				}

				/**
				 * Legt den Wert der useParent-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link Boolean }
				 * 
				 */
				public void setUseParent(Boolean value) {
					this.useParent = value;
				}

				/**
				 * Ruft den Wert der newprogid-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getNewprogid() {
					return newprogid;
				}

				/**
				 * Legt den Wert der newprogid-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setNewprogid(String value) {
					this.newprogid = value;
				}

				/**
				 * Ruft den Wert der newext-Eigenschaft ab.
				 * 
				 * @return possible object is {@link String }
				 * 
				 */
				public String getNewext() {
					return newext;
				}

				/**
				 * Legt den Wert der newext-Eigenschaft fest.
				 * 
				 * @param value
				 *            allowed object is {@link String }
				 * 
				 */
				public void setNewext(String value) {
					this.newext = value;
				}

			}

		}

	}

	/**
	 * <p>
	 * Java-Klasse for anonymous complex type.
	 * 
	 * <p>
	 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;attribute name="oldvalue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="oldmodulus" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="oldexponent" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="modulus" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="exponent" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class ProofKey {

		@XmlAttribute(name = "oldvalue", required = true)
		protected String oldvalue;
		@XmlAttribute(name = "oldmodulus", required = true)
		protected String oldmodulus;
		@XmlAttribute(name = "oldexponent", required = true)
		protected String oldexponent;

		@XmlAttribute(name = "value", required = true)
		protected String value;
		@XmlAttribute(name = "modulus", required = true)
		protected String modulus;
		@XmlAttribute(name = "exponent", required = true)
		protected String exponent;

		/**
		 * Ruft den Wert der oldvalue-Eigenschaft ab.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getOldvalue() {
			return oldvalue;
		}

		/**
		 * Legt den Wert der oldvalue-Eigenschaft fest.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setOldvalue(String value) {
			this.oldvalue = value;
		}

		/**
		 * Ruft den Wert der value-Eigenschaft ab.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Legt den Wert der value-Eigenschaft fest.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setValue(String value) {
			this.value = value;
		}

		public String getOldmodulus() {
			return oldmodulus;
		}

		public void setOldmodulus(String oldmodulus) {
			this.oldmodulus = oldmodulus;
		}

		public String getOldexponent() {
			return oldexponent;
		}

		public void setOldexponent(String oldexponent) {
			this.oldexponent = oldexponent;
		}

		public String getModulus() {
			return modulus;
		}

		public void setModulus(String modulus) {
			this.modulus = modulus;
		}

		public String getExponent() {
			return exponent;
		}

		public void setExponent(String exponent) {
			this.exponent = exponent;
		}

	}

}
