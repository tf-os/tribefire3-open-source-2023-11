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
package tribefire.extension.setup.dev_env_generator.processing.eclipse;

import java.io.File;
import java.util.Map;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.FileTools;

public abstract class EclipseWorkspaceHelper {

	File devEnv;
	String folder;
	String name;
	String content;

	EclipseWorkspaceHelper(File devEnv, String folder, String name, String content) {
		this.devEnv = devEnv;
		this.folder = folder;
		this.name = name;
		this.content = content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean exists() {
		return getCfgFile().exists();
	}

	private String getContent(String data, Map<String, String> substitutions) {
		String contentFinal = data;
		for (Map.Entry<String, String> entry : substitutions.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			contentFinal = contentFinal.replaceAll(key, val);
		}
		return contentFinal;
	}

	public Maybe<Neutral> create(Map<String, String> substitutions) {
		File cfg = getCfgFile();
		return writeFile(cfg, getContent(this.content, substitutions));
	}

	/**
	 * Patches the content of an existing template file with additional information.
	 * 
	 * 
	 * @param custom
	 *            String to be added to template data (prepended)
	 * @param substitutions
	 *            Map of string-to-string to be replaced in original data template. This can be used for replacements, but also to delete specific
	 *            lines/data.
	 */
	public Maybe<Neutral> patch(String custom, Map<String, String> substitutions) {
		File cfg = getCfgFile();
		Maybe<String> contentFile = readFile(cfg);
		if (!contentFile.isSatisfied())
			return contentFile.cast();
		String contentFinal = getContent(contentFile.get(), substitutions);
		String text = custom + "\n" + contentFinal;
		return writeFile(cfg, text);
	}

	private File getCfgDir() {
		return new File(devEnv + "/eclipse-workspace/" + folder);
	}

	public File getCfgFile() {
		return new File(getCfgDir() + "/" + name);
	}

	protected Maybe<Neutral> writeFile(File file, String content) {
		try {
			FileTools.write(file).string(content);
			return Maybe.complete(Neutral.NEUTRAL);

		} catch (Exception e) {
			return Reasons.build(IoError.T).text(e.getMessage()).toMaybe();
		}
	}

	protected Maybe<String> readFile(File file) {
		try {
			String content = FileTools.read(file).asString();
			return Maybe.complete(content);

		} catch (Exception e) {
			return Reasons.build(IoError.T).text("Error during reading of \"" + file.getName() + "\": " + e.getMessage()).toMaybe();
		}
	}

}
