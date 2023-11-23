package com.braintribe.build.cmd.assets.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class ZipEntryTransfer {
	public List<File> targetFiles;
	public InputStream in;
	public String slashedPathName;
	
	public ZipEntryTransfer(List<File> targetFiles, InputStream in, String slashedPathName) {
		super();
		this.targetFiles = targetFiles;
		this.in = in;
		this.slashedPathName = slashedPathName;
	}
}
