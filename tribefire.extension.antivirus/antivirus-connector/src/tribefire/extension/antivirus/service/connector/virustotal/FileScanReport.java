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
package tribefire.extension.antivirus.service.connector.virustotal;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class FileScanReport {

    @SerializedName("scans")
    private Map<String, VirusScanInfo> scans;
    @SerializedName("scan_id")
    private String scanId;
    @SerializedName("sha1")
    private String sha1;
    @SerializedName("resource")
    private String resource;
    @SerializedName("response_code")
    private Integer responseCode;
    @SerializedName("scan_date")
    private String scanDate;
    @SerializedName("permalink")
    private String permalink;
    @SerializedName("verbose_msg")
    private String verboseMessage;
    @SerializedName("total")
    private Integer total;
    @SerializedName("positives")
    private Integer positives;
    @SerializedName("sha256")
    private String sha256;
    @SerializedName("md5")
    private String md5;

    public FileScanReport() {
    }

	public Map<String, VirusScanInfo> getScans() {
		return scans;
	}

	public void setScans(Map<String, VirusScanInfo> scans) {
		this.scans = scans;
	}

	public String getScanId() {
		return scanId;
	}

	public void setScanId(String scanId) {
		this.scanId = scanId;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	public String getScanDate() {
		return scanDate;
	}

	public void setScanDate(String scanDate) {
		this.scanDate = scanDate;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public String getVerboseMessage() {
		return verboseMessage;
	}

	public void setVerboseMessage(String verboseMessage) {
		this.verboseMessage = verboseMessage;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getPositives() {
		return positives;
	}

	public void setPositives(Integer positives) {
		this.positives = positives;
	}

	public String getSha256() {
		return sha256;
	}

	public void setSha256(String sha256) {
		this.sha256 = sha256;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
