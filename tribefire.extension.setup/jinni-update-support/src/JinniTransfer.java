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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class JinniTransfer {
	public static void main(String[] args) {
		try {
			File jinniUpdateDir = new File(args[0]);
			File jinniInstallationDir = new File(args[1]);

			System.out.println("JinniTransfer: " + jinniUpdateDir + " -> " + jinniInstallationDir);

			// we keep all original files and only overwrite

			// keep and do not overwrite these files:
			Predicate<CharSequence> keepFiles = doNotOverwrite();

			// make a notice ".new" if file was changed, but keep user file
			Predicate<CharSequence> noticeFile = noticIfChanged();

			moveFilesRecursively(jinniUpdateDir, jinniInstallationDir, new StringBuilder(), keepFiles, noticeFile);

			if (isUnix()) {
				setFilePermissions(new File(jinniInstallationDir, "bin"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static boolean isUnix() {
		String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if (osName.contains("mac") || osName.contains("darwin") || osName.contains("linux") || osName.contains("nix") || osName.contains("nux")
				|| osName.contains("aix")) {
			return true;
		} else {
			return false;
		}
	}

	private static void setFilePermissions(File dir) throws IOException {
		for (File file : dir.listFiles()) {
			if (file.isFile() && !file.getName().endsWith(".bat")) {
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr--r--");
				Files.setPosixFilePermissions(file.toPath(), permissions);
			}
		}
	}

	private static Predicate<CharSequence> doNotOverwrite() {
		Pattern updateScriptsPattern = Pattern.compile("/bin/jinni-update.*");
		Pattern updateJarPattern = Pattern.compile("/lib/jinni-update-support\\.jar");
		Predicate<CharSequence> updateScriptsFilter = s -> updateScriptsPattern.matcher(s).matches();
		Predicate<CharSequence> updateJarFilter = s -> updateJarPattern.matcher(s).matches();
		Predicate<CharSequence> combinedFilter = updateScriptsFilter.or(updateJarFilter);
		return combinedFilter;
	}

	private static Predicate<CharSequence> noticIfChanged() {
		Pattern confPattern = Pattern.compile("/conf/.*");
		Pattern aliasPattern = Pattern.compile("/aliases/.*");
		Predicate<CharSequence> confFilter = s -> confPattern.matcher(s).matches();
		Predicate<CharSequence> aliasFilter = s -> aliasPattern.matcher(s).matches();
		Predicate<CharSequence> combinedFilter = confFilter.or(aliasFilter);
		return combinedFilter;
	}

	public static void moveFilesRecursively(File sourceFolder, File targetFolder, StringBuilder pathStack, Predicate<CharSequence> exclusionFilter,
			Predicate<CharSequence> noticeFiles) throws IOException {

		if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
			System.err.println("The source folder \"" + sourceFolder + "\" does not exist. Stopping. ");
			return;
		}

		if (!targetFolder.exists() || !targetFolder.isDirectory()) {
			System.err.println("The target folder \"" + targetFolder + "\" does not exist. Stopping. ");
			return;
		}

		for (File sourceFile : sourceFolder.listFiles()) {
			int oldLength = pathStack.length();
			pathStack.append('/');
			pathStack.append(sourceFile.getName());

			// /root/sub2

			try {
				if (exclusionFilter.test(pathStack)) {
					sourceFile.delete();
					continue;
				}

				File targetFile = new File(targetFolder, sourceFile.getName());

				if (sourceFile.isDirectory()) {
					targetFile.mkdir();
					moveFilesRecursively(sourceFile, targetFile, pathStack, exclusionFilter, noticeFiles);
				} else {
					boolean needsUpdate = true;

					if (targetFile.exists()) {

						if (noticeFiles.test(pathStack)) {
							// this is a user-file that is never overwritten
							needsUpdate = false;
							// check, if source and target are not identical
							if (Files.mismatch(sourceFile.toPath(), targetFile.toPath()) >= 0) {
								// files are different -> notice
								File noticeFile = new File(targetFolder, sourceFile.getName() + ".new");
								if (noticeFile.exists())
									noticeFile.delete();
								Files.move(sourceFile.toPath(), noticeFile.toPath());
							}

						} else { // target must be overwritten
							targetFile.delete();
						}
					}

					if (needsUpdate)
						Files.move(sourceFile.toPath(), targetFile.toPath());
				}

			} finally {
				pathStack.setLength(oldLength);
			}
		}
		sourceFolder.delete();
	}

}
