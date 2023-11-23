# Testing of WOPI functionality

## Integration Testing

TODO (JUnit)

## UI Testing

TODO (selenium)

## Testing via `wopi-validator-core`

Check 
- <https://wopi.readthedocs.io/en/latest/build_test_ship/validator.html>
- <https://github.com/Microsoft/Office-Online-Test-Tools-and-Documentation/blob/master/samples/java/ProofKeyTester.java>
- <https://wopi.readthedocs.io/en/latest/scenarios/proofkeys.html>
- <https://github.com/microsoft/Office-Online-Test-Tools-and-Documentation/blob/master/samples/java/ProofKeyTester.java> 
- <https://github.com/Microsoft/wopi-validator-core>

for details on `WOPI validation application`

The test will run against a document with the name: `test.wopitest`. It needs to be available in the WOPI access.

```bash
docker pull tylerbutler/wopi-validator
```

```bash
docker run -it --rm tylerbutler/wopi-validator -- -w http://localhost:8080/tribefire-services/component/wopi/files/1 -t myToken -l 0 -s
```

### General Usage

```bash
docker run -it --rm tylerbutler/wopi-validator -- -w http://192.168.200.44:8080/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMTkxMTE4MTcwODMxMDQ1LWNmZmExZTliLTFmYTYtNGY3OS04NzMzLWM3NTZjN2ExNjlkMyJ9 -l 0 -s
```
### Test cases

Preparation:

- set Custom Public Services URL if needed - e.g. `http://1.2.3.4:9876/tribefire-services`
- set `https://ffc-onenote.officeapps.live.com/hosting/discovery` as WOPI Connector

#### Test cases list

- CheckFileInfoSchema
  - FullCheckFileInfoSchema
  - HostUrls
  - CheckFileWithInvalidAccessToken
- BaseWopiViewing
  - ViewOnlySupport
  - GetUnlockedFile
- Locks
  - LockLengthValidation
  - LockFormatValidation
  - SuccessfulLockSequence
  - LockMismatchAfterUnlockAndRelockRequest
  - DoubleLockSequence
  - UnlockUnlockedFile
  - LockMismatchOnLockRequest
  - LockMismatchOnUnlockRequest
  - LockMismatchOnRefreshLockRequest
  - LockMismatchOnUnlockAndRelockRequest
  - LockMismatchOnPutFileRequest
  - LockFileWithInvalidAccessToken
  - UnlockFileWithInvalidAccessToken
- GetLock
  - files.GetLock
  - files.GetLockAfterChange
  - files.GetLockOnUnlockedFile
- ExtendedLockLength
  - files.ExtendedLockLengthValidation
- EditFlows
  - BasicEdit
  - EditNoChange
  - PutUnlockedFile
  - PutUnlockedFileNotZeroBytes
  - GetPutFileWithInvalidAccessToken
- FileVersion
  - files.GetFileReturnsVersion
  - files.LockReturnsVersion
  - files.UnlockReturnsVersion
  - files.PutFileReturnsVersion
  - files.PutFileReturnsDifferentVersion
  - files.LockAndUnlockAfterGetFileReturnsSameVersion
- PutUserInfo
  - PutUserInfoSucceeds
- PutRelativeFile
  - PutRelativeFile.SuggestedExtension
  - PutRelativeFile.SuggestedName
  - PutRelativeFile.SuggestedNameConflict
  - PutRelativeFile.RelativeName
  - PutRelativeFile.RelativeNameOverwriteTrueNoEffect
  - PutRelativeFile.RelativeNameOverwriteFalseNoEffect
  - PutRelativeFile.RelativeNameConflictNoOverwrite
  - PutRelativeFile.RelativeNameConflictOverwriteFalse
  - PutRelativeFile.RelativeNameConflictOverwriteTrue
  - PutRelativeFile.ConflictingHeaders
  - PutRelativeFile.RelativeNameConflictOverwriteTrueLocked
  - PutRelativeFile.FileNameReturnedIsCorrectlyEncoded
  - PutRelativeFile.FileNameLongerThan512Chars
  - PutRelativeFile.IncludeHostUrls
- PutRelativeFileUnsupported
  - PutRelativeFileUnsupported.SuggestedExtension
  - PutRelativeFileUnsupported.SuggestedName
  - PutRelativeFileUnsupported.RelativeName
  - PutRelativeFileUnsupported.RelativeNameOverwriteTrueNoEffect
  - PutRelativeFileUnsupported.RelativeNameOverwriteFalseNoEffect
  - PutRelativeFileUnsupported.ConflictingHeaders
- RenameFileIfCreateChildFileIsNotSupported
  - PutRelativeAndRenameFile.RenameShouldSucceed
  - PutRelativeAndRenameFile.FileNameAfterRenameIsCorrectlyEncoded
  - PutRelativeAndRenameFile.RenamingALockedFileWithACorrectLockHeaderValueShouldSucceed
  - PutRelativeAndRenameFile.RenamingALockedFileWithAnIncorrectLockHeaderValueShouldReturnA409
  - PutRelativeAndRenameFile.RenamingADeletedFileShouldReturnA404
  - PutRelativeAndRenameFile.RenameShouldNotChangeFileExtension
- RenameFileIfCreateChildFileIsSupported
  - CreateChildFileAndRenameFile.RenameShouldSucceed
  - CreateChildFileAndRenameFile.FileNameAfterRenameIsCorrectlyEncoded
  - CreateChildFileAndRenameFile.RenamingALockedFileWithACorrectLockHeaderValueShouldSucceed
  - CreateChildFileAndRenameFile.RenamingALockedFileWithAnIncorrectLockHeaderValueShouldReturnA409
  - CreateChildFileAndRenameFile.RenamingADeletedFileShouldReturnA404
  - CreateChildFileAndRenameFile.RenameShouldNotChangeFileExtension
- Ecosystem
  - files.GetEcosystem
  - ecosystem.CheckEcosystem
  - ecosystem.GetRootContainer
  - ecosystem.GetRootContainerInvalidAccessToken
- Container
  - containers.CheckContainerInfo
  - containers.CreateAndDeleteChildContainer
  - containers.CreateAndDeleteChildContainerDupe
  - containers.CreateAndDeleteChildContainerExactName
  - containers.CreateAndDeleteChildContainerExactNameDupe
  - containers.GetEcosystem
  - containers.LicensingPropertiesOnChildFolderMatchWithParentFolder
- RenameContainer
  - containers.CreateAndRenameChildContainer
  - containers.CreateAndRenameChildContainerAndVerifyReturnedContainerNameIsCorrectlyEncoded
- EnumerateAncestorsAndChildren
  - files.EnumerateAncestors
  - containers.EnumerateChildren
  - containers.EnumerateChildrenOnRoot
  - containers.EnumerateChildrenWithOneFilter
  - containers.EnumerateChildrenWithMultipleFilters
  - containers.EnumerateAncestorsOnRoot
  - containers.EnumerateAncestorsOnChildren
- CreateChildFileAndDeleteFile
  - CreateChildFileAndDelete
  - CreateChildFile.SuggestedName
  - CreateChildFile.SuggestedNameConflict
  - CreateChildFile.RelativeName
  - CreateChildFile.RelativeNameOverwriteTrueNoEffect
  - CreateChildFile.RelativeNameOverwriteFalseNoEffect
  - CreateChildFile.RelativeNameConflictNoOverwrite
  - CreateChildFile.RelativeNameConflictOverwriteFalse
  - CreateChildFile.RelativeNameConflictOverwriteTrue
  - CreateChildFile.ConflictingHeaders
  - CreateChildFile.RelativeNameConflictOverwriteTrueLocked
  - CreateChildFile.FileNameReturnedIsCorrectlyEncoded
  - CreateChildFile.FileNameLongerThan512Characters
  - CreateChildFile.LicensingPropertiesOnChildFileMatchWithParentFolder
- FileUrlUsage
  - GetFromFileUrlAfterPutFile
- FileUrlViewOnly
  - GetFromFileUrl
- GetSharingUrlForFileWithTypeReadOnly
  - files.GetShareUrlForReadOnlyUrlType
- GetSharingUrlForFileWithTypeReadWrite
  - files.GetShareUrlForReadWriteUrlType
- GetSharingUrlForFileWithUnknownType
  - files.GetShareUrlForUnknownUrlType
- GetSharingUrlForContainerWithTypeReadOnly
  - containers.GetShareUrlForReadOnlyUrlType
- GetSharingUrlForContainerWithTypeReadWrite
  - containers.GetShareUrlForReadWriteUrlType
- GetSharingUrlForContainerWithUnknownType
  - containers.GetShareUrlForUnknownUrlType
- AddActivities
  - files.AddActivitiesSingleMinimalComment
  - files.AddActivitiesSingleFullComment
  - files.AddActivitiesAllCapsID
  - files.AddActivitiesMaxLengthContentID
  - files.AddActivitiesMaxLengthNavigationId
  - files.AddActivitiesCommentUpdate
  - files.AddActivitiesCommentDelete
  - files.AddActivitiesWithBonusToplevelProperty
  - files.AddActivitiesWithBonusDataProperty
  - files.AddActivitiesCommentAndPerson
  - files.AddActivitiesCommentWithThreePeople
  - files.AddActivitiesCommentWithPersonBonusProperty
  - files.AddActivitiesCommentWithNonWopiPerson
  - files.AddActivitiesUnknownActivityType
  - files.AddActivitiesUnknownNoDataActivity
  - files.AddActivitiesTwoActivitiesOneUnknown
  - files.AddActivitiesThreeComments
- ProofKeys
  - ProofKeys.CurrentValid.OldValid
  - ProofKeys.CurrentValid.OldInvalid
  - ProofKeys.CurrentInvalid.OldValidSignedWithCurrentKey
  - ProofKeys.CurrentValidSignedWithOldKey.OldInvalid
  - ProofKeys.CurrentInvalid.OldValidSignedWithOldKey
  - ProofKeys.CurrentInvalid.OldInvalid
  - ProofKeys.TimestampOlderThan20Min



#### VIEW:

##### WORKING test groups:

CheckFileInfoSchema
- BaseWopiViewing
- Locks
- GetLock
- ExtendedLockLength
- FileVersion
- PutRelativeFileUnsupported

##### POSTPONED test groups:
- ProofKeys
- PutUserInfo
- FileUrlUsage

##### SKIPPED BY INTENTION test groups:
- PutRelativeFile
- RenameFileIfCreateChildFileIsNotSupported
- RenameFileIfCreateChildFileIsSupported
- Ecosystem
- Container
- CreateChildFileAndDeleteFile
- RenameContainer
- EnumerateAncestorsAndChildren
- FileUrlViewOnly
- GetSharingUrlForFileWithTypeReadOnly
- GetSharingUrlForFileWithTypeReadWrite
- GetSharingUrlForFileWithUnknownType
- GetSharingUrlForContainerWithTypeReadOnly
- GetSharingUrlForContainerWithTypeReadWrite
- GetSharingUrlForContainerWithUnknownType
- AddActivities
- EditFlows

###########

##### Examples
```bash
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g CheckFileInfoSchema
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g BaseWopiViewing
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g Locks
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g GetLock
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g ExtendedLockLength
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g FileVersion
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_view_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g PutRelativeFileUnsupported
```

#### EDIT:

##### WORKING test groups:
- CheckFileInfoSchema
- BaseWopiViewing
- Locks
- GetLock
- ExtendedLockLength
- FileVersion
- PutRelativeFileUnsupported
- EditFlows

##### POSTPONED test groups:
- ProofKeys
- PutUserInfo
- FileUrlUsage

##### SKIPPED BY INTENTION:
- PutRelativeFile
- RenameFileIfCreateChildFileIsNotSupported
- RenameFileIfCreateChildFileIsSupported
- Ecosystem
- Container
- CreateChildFileAndDeleteFile
- RenameContainer
- EnumerateAncestorsAndChildren
- FileUrlViewOnly
- GetSharingUrlForFileWithTypeReadOnly
- GetSharingUrlForFileWithTypeReadWrite
- GetSharingUrlForFileWithUnknownType
- GetSharingUrlForContainerWithTypeReadOnly
- GetSharingUrlForContainerWithTypeReadWrite
- GetSharingUrlForContainerWithUnknownType
- AddActivities

##### Examples

```bash
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g CheckFileInfoSchema
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g BaseWopiViewing
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g Locks
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g GetLock
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g ExtendedLockLength
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g FileVersion
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g PutRelativeFileUnsupported
docker run --rm -it tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/testWopiResources_edit_test.wopitest_forTesting__ -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMjA4MTAxODQxNTgyLWQ5NzA2OWJiLTBjNTQtNGM5My1hNzZmLTg5NDAwNzNkYzIzYSJ9 -l 0 -g EditFlows
```
