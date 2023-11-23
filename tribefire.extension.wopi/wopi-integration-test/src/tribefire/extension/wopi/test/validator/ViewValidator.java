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
package tribefire.extension.wopi.test.validator;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.service.integration.EnsureTestDoc;
import com.braintribe.model.wopi.service.integration.EnsureTestDocResult;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.system.exec.RunCommandContext;
import com.braintribe.utils.system.exec.RunCommandRequest;

/**
 * Executing Office Online validation from 'https://github.com/microsoft/wopi-validator-core' - VIEW
 * 
 *
 */
@Category(SpecialEnvironment.class)
public class ViewValidator extends AbstractValidatorWopiTest {

	public ViewValidator(String testGroup, String testName) {
		super(testGroup, testName);
	}

	@Parameters(name = "{index}: {0} - {1}")
	public static Collection<Object[]> data() {

		List<Pair<String, String>> list = new ArrayList<>();
		// Snapshot from: Fri Dec 04 12:19:44 CET 2020
		// CheckFileInfoSchema
		// -----------------------------------------
		list.add(new Pair<>("CheckFileInfoSchema", "FullCheckFileInfoSchema"));
		list.add(new Pair<>("CheckFileInfoSchema", "HostUrls"));
		list.add(new Pair<>("CheckFileInfoSchema", "CheckFileWithInvalidAccessToken"));
		// BaseWopiViewing
		// -----------------------------------------
		list.add(new Pair<>("BaseWopiViewing", "ViewOnlySupport"));
		list.add(new Pair<>("BaseWopiViewing", "GetUnlockedFile"));
		// Locks
		// -----------------------------------------
		list.add(new Pair<>("Locks", "LockLengthValidation"));
		list.add(new Pair<>("Locks", "LockFormatValidation"));
		list.add(new Pair<>("Locks", "SuccessfulLockSequence"));
		list.add(new Pair<>("Locks", "LockMismatchAfterUnlockAndRelockRequest"));
		list.add(new Pair<>("Locks", "DoubleLockSequence"));
		list.add(new Pair<>("Locks", "UnlockUnlockedFile"));
		list.add(new Pair<>("Locks", "LockMismatchOnLockRequest"));
		list.add(new Pair<>("Locks", "LockMismatchOnUnlockRequest"));
		list.add(new Pair<>("Locks", "LockMismatchOnRefreshLockRequest"));
		list.add(new Pair<>("Locks", "LockMismatchOnUnlockAndRelockRequest"));
		list.add(new Pair<>("Locks", "LockMismatchOnPutFileRequest"));
		list.add(new Pair<>("Locks", "LockFileWithInvalidAccessToken"));
		list.add(new Pair<>("Locks", "UnlockFileWithInvalidAccessToken"));
		// GetLock
		// -----------------------------------------
		list.add(new Pair<>("GetLock", "files.GetLock"));
		list.add(new Pair<>("GetLock", "files.GetLockAfterChange"));
		list.add(new Pair<>("GetLock", "files.GetLockOnUnlockedFile"));
		// ExtendedLockLength
		// -----------------------------------------
		list.add(new Pair<>("ExtendedLockLength", "files.ExtendedLockLengthValidation"));
		// EditFlows
		// -----------------------------------------
		// list.add(new Pair<>("EditFlows", "BasicEdit"));
		// list.add(new Pair<>("EditFlows", "EditNoChange"));
		// list.add(new Pair<>("EditFlows", "PutUnlockedFile"));
		// list.add(new Pair<>("EditFlows", "PutUnlockedFileNotZeroBytes"));
		// list.add(new Pair<>("EditFlows", "GetPutFileWithInvalidAccessToken"));
		// FileVersion
		// -----------------------------------------
		list.add(new Pair<>("FileVersion", "files.GetFileReturnsVersion"));
		list.add(new Pair<>("FileVersion", "files.LockReturnsVersion"));
		list.add(new Pair<>("FileVersion", "files.UnlockReturnsVersion"));
		list.add(new Pair<>("FileVersion", "files.PutFileReturnsVersion"));
		list.add(new Pair<>("FileVersion", "files.PutFileReturnsDifferentVersion"));
		list.add(new Pair<>("FileVersion", "files.LockAndUnlockAfterGetFileReturnsSameVersion"));
		// PutUserInfo
		// -----------------------------------------
		// list.add(new Pair<>("PutUserInfo", "PutUserInfoSucceeds"));
		// PutRelativeFile
		// -----------------------------------------
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.SuggestedExtension"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.SuggestedName"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.SuggestedNameConflict"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeName"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameOverwriteTrueNoEffect"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameOverwriteFalseNoEffect"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameConflictNoOverwrite"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameConflictOverwriteFalse"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameConflictOverwriteTrue"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.ConflictingHeaders"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.RelativeNameConflictOverwriteTrueLocked"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.FileNameReturnedIsCorrectlyEncoded"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.FileNameLongerThan512Chars"));
		// list.add(new Pair<>("PutRelativeFile", "PutRelativeFile.IncludeHostUrls"));
		// PutRelativeFileUnsupported
		// -----------------------------------------
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.SuggestedExtension"));
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.SuggestedName"));
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.RelativeName"));
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.RelativeNameOverwriteTrueNoEffect"));
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.RelativeNameOverwriteFalseNoEffect"));
		list.add(new Pair<>("PutRelativeFileUnsupported", "PutRelativeFileUnsupported.ConflictingHeaders"));
		// RenameFileIfCreateChildFileIsNotSupported
		// -----------------------------------------
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.RenameShouldSucceed"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.FileNameAfterRenameIsCorrectlyEncoded"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.RenamingALockedFileWithACorrectLockHeaderValueShouldSucceed"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.RenamingALockedFileWithAnIncorrectLockHeaderValueShouldReturnA409"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.RenamingADeletedFileShouldReturnA404"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsNotSupported",
		// "PutRelativeAndRenameFile.RenameShouldNotChangeFileExtension"));
		// RenameFileIfCreateChildFileIsSupported
		// -----------------------------------------
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.RenameShouldSucceed"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.FileNameAfterRenameIsCorrectlyEncoded"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.RenamingALockedFileWithACorrectLockHeaderValueShouldSucceed"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.RenamingALockedFileWithAnIncorrectLockHeaderValueShouldReturnA409"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.RenamingADeletedFileShouldReturnA404"));
		// list.add(new Pair<>("RenameFileIfCreateChildFileIsSupported",
		// "CreateChildFileAndRenameFile.RenameShouldNotChangeFileExtension"));
		// Ecosystem
		// -----------------------------------------
		// list.add(new Pair<>("Ecosystem", "files.GetEcosystem"));
		// list.add(new Pair<>("Ecosystem", "ecosystem.CheckEcosystem"));
		// list.add(new Pair<>("Ecosystem", "ecosystem.GetRootContainer"));
		// list.add(new Pair<>("Ecosystem", "ecosystem.GetRootContainerInvalidAccessToken"));
		// Container
		// -----------------------------------------
		// list.add(new Pair<>("Container", "containers.CheckContainerInfo"));
		// list.add(new Pair<>("Container", "containers.CreateAndDeleteChildContainer"));
		// list.add(new Pair<>("Container", "containers.CreateAndDeleteChildContainerDupe"));
		// list.add(new Pair<>("Container", "containers.CreateAndDeleteChildContainerExactName"));
		// list.add(new Pair<>("Container", "containers.CreateAndDeleteChildContainerExactNameDupe"));
		// list.add(new Pair<>("Container", "containers.GetEcosystem"));
		// list.add(new Pair<>("Container", "containers.LicensingPropertiesOnChildFolderMatchWithParentFolder"));
		// RenameContainer
		// -----------------------------------------
		// list.add(new Pair<>("RenameContainer", "containers.CreateAndRenameChildContainer"));
		// list.add(new Pair<>("RenameContainer",
		// "containers.CreateAndRenameChildContainerAndVerifyReturnedContainerNameIsCorrectlyEncoded"));
		// EnumerateAncestorsAndChildren
		// -----------------------------------------
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "files.EnumerateAncestors"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateChildren"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateChildrenOnRoot"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateChildrenWithOneFilter"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateChildrenWithMultipleFilters"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateAncestorsOnRoot"));
		// list.add(new Pair<>("EnumerateAncestorsAndChildren", "containers.EnumerateAncestorsOnChildren"));
		// CreateChildFileAndDeleteFile
		// -----------------------------------------
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFileAndDelete"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.SuggestedName"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.SuggestedNameConflict"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeName"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeNameOverwriteTrueNoEffect"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeNameOverwriteFalseNoEffect"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeNameConflictNoOverwrite"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeNameConflictOverwriteFalse"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.RelativeNameConflictOverwriteTrue"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.ConflictingHeaders"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile",
		// "CreateChildFile.RelativeNameConflictOverwriteTrueLocked"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.FileNameReturnedIsCorrectlyEncoded"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile", "CreateChildFile.FileNameLongerThan512Characters"));
		// list.add(new Pair<>("CreateChildFileAndDeleteFile",
		// "CreateChildFile.LicensingPropertiesOnChildFileMatchWithParentFolder"));
		// FileUrlUsage
		// -----------------------------------------
		// list.add(new Pair<>("FileUrlUsage", "GetFromFileUrlAfterPutFile"));
		// FileUrlViewOnly
		// -----------------------------------------
		// list.add(new Pair<>("FileUrlViewOnly", "GetFromFileUrl"));
		// GetSharingUrlForFileWithTypeReadOnly
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForFileWithTypeReadOnly", "files.GetShareUrlForReadOnlyUrlType"));
		// GetSharingUrlForFileWithTypeReadWrite
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForFileWithTypeReadWrite", "files.GetShareUrlForReadWriteUrlType"));
		// GetSharingUrlForFileWithUnknownType
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForFileWithUnknownType", "files.GetShareUrlForUnknownUrlType"));
		// GetSharingUrlForContainerWithTypeReadOnly
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForContainerWithTypeReadOnly",
		// "containers.GetShareUrlForReadOnlyUrlType"));
		// GetSharingUrlForContainerWithTypeReadWrite
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForContainerWithTypeReadWrite",
		// "containers.GetShareUrlForReadWriteUrlType"));
		// GetSharingUrlForContainerWithUnknownType
		// -----------------------------------------
		// list.add(new Pair<>("GetSharingUrlForContainerWithUnknownType", "containers.GetShareUrlForUnknownUrlType"));
		// AddActivities
		// -----------------------------------------
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesSingleMinimalComment"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesSingleFullComment"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesAllCapsID"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesMaxLengthContentID"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesMaxLengthNavigationId"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentUpdate"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentDelete"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesWithBonusToplevelProperty"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesWithBonusDataProperty"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentAndPerson"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentWithThreePeople"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentWithPersonBonusProperty"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesCommentWithNonWopiPerson"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesUnknownActivityType"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesUnknownNoDataActivity"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesTwoActivitiesOneUnknown"));
		// list.add(new Pair<>("AddActivities", "files.AddActivitiesThreeComments"));
		// ProofKeys
		// -----------------------------------------
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentValid.OldValid"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentValid.OldInvalid"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentInvalid.OldValidSignedWithCurrentKey"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentValidSignedWithOldKey.OldInvalid"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentInvalid.OldValidSignedWithOldKey"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.CurrentInvalid.OldInvalid"));
		// list.add(new Pair<>("ProofKeys", "ProofKeys.TimestampOlderThan20Min"));

		return list.stream().map(s -> new Object[] { s.getFirst(), s.getSecond() }).collect(Collectors.toList());
	}

	@Before
	public void setup() {
		EnsureTestDoc request = EnsureTestDoc.T.create();
		request.setDocumentMode(DocumentMode.view);
		request.setTestNames(asSet(testName));
		EnsureTestDocResult result = request.eval(session).get();
		commands = result.getCommands();

		assertThat(commands).isNotNull();
		assertThat(commands).hasSize(1);
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void test() {
		execute();
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	protected void execute() {
		try {
			String command = commands.get(0);
			RunCommandRequest request = new RunCommandRequest(command, 1000);
			logger.debug(() -> "Executing: " + request);
			RunCommandContext commandContext = commandExection.runCommand(request);

			int errorCode = commandContext.getErrorCode();

			logger.info("ErrorCode: " + errorCode);

			assertThat(errorCode).isEqualTo(0);
		} catch (Exception e) {
			throw new RuntimeException("Could not execute validator", e);
		}

	}

}
