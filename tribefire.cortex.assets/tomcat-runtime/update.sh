#!/bin/bash

# This script is used to automate the process of updating runtime-original and runtime modification folders
# when a new Tomcat version or related files such as a new tomcat-juli replacement are available.


# For a given fully qualified artifact this function determines the relative path
# to the main jar file (in a Maven repository). Example:
# Arg 1:  com.braintribe.tomcat:tomcat-extensions#1.0.16
# Result: com/braintribe/tomcat/tomcat-extensions/1.0.16/tomcat-extensions-1.0.16.jar (returned via echo)
function repositoryRelativeJarFilePath() {
	local fullyQualifiedArtifact=$1
	local version=${fullyQualifiedArtifact#*#}
	local groupIdAndArtifactId=${fullyQualifiedArtifact%%#"${version}"}
	local groupId=${groupIdAndArtifactId%:*}
	local artifactId=${groupIdAndArtifactId#*:}
	local relativeArtifactVersionFolder="${groupId//\./\/}/${artifactId}/${version}"
	local jarFileName="${artifactId}-${version}.jar"
	local relativeJarFilePath="${relativeArtifactVersionFolder}/${jarFileName}"
	# return resulrt via echo
	echo ${relativeJarFilePath}
}


# For each file in the specified custom files directory (i.e. the files we add/modify)
# this function finds the respective files in old and new Tomcat and compares them.
# If any changes are found, this means a manual review is required.
# Arg 1:  custom files dir, i.e. folder containing custom / modified files
# Arg 2:  old Tomcat dir
# Arg 3:  new Tomcat dir
# Arg 4:  whether or not to add log lines for comparing new files with custom files
# Result: whether or not a manual review is required, i.e. true or false (returned via variable FUNCTION_RETURN_VALUE)
function compareOriginalAndNewTomcatForChangesInCustomFiles() {
	local customFilesDir=$1
	local oldTomcatDir=$2
	local newTomcatDir=$3
	local addLogLinesForComparingNewFilesWithCustomFiles=$4

	local reviewRequired=false

	for customFilePath in $(find "${customFilesDir}" -type f); do
		# the customFilePath is the path of our custom file, e.g. full path to path/to/custom/_projection/host/bin/catalina.sh.vm
		# from this we want to derive the tomcatFilePath, e.g. host/bin/catalina.sh
		local tomcatFilePath=${customFilePath}
		# remove path prefix for customFilesDir
		tomcatFilePath=${tomcatFilePath#${customFilesDir}/}
		# remove _projection path prefix (if any)
		tomcatFilePath=${tomcatFilePath#_projection/}
		# remove '.vm' suffix (if any)
		tomcatFilePath=${tomcatFilePath%.vm}

		local oldTomcatFilePath=${oldTomcatDir}/${tomcatFilePath}
		local newTomcatFilePath=${newTomcatDir}/${tomcatFilePath}

		if [ -f "${newTomcatFilePath}" ] && [ -f "${oldTomcatFilePath}" ];then
			 # we use -s instead of --silent in order to support execution in Alpine (Docker)
			cmp -s "${newTomcatFilePath}" "${oldTomcatFilePath}"
			if [ $? -ne 0 ];then
				reviewRequired=true
				echo "UPDATED:  ${newTomcatFilePath}"

				if [ "${addLogLinesForComparingNewFilesWithCustomFiles}" == "true" ]; then
					echo "  ${DIFF_TOOL} ${customFilePath} ${newTomcatFilePath}"
				fi
			else
				#echo "EQUAL:    ${newTomcatFilePath}"
				:
			fi
		elif [ -f "${oldTomcatFilePath}" ];then
			reviewRequired=true
			echo "REMOVED:    ${newTomcatFilePath}"
			echo "  This file doesn't exist in new Tomcat, but it is included in the old new."
			echo "  Since this file was removed, do we want to keep our custom file? This needs to be checked manually."
		elif [ -f "${newTomcatFilePath}" ];then
			echo "NEW:        ${newTomcatFilePath}"
			echo "  This is a new file, i.e. it doesn't exist in the old Tomcat. This should be fine."
		else
			# file doesn't doesn't exist at all, i.e. it's just an additional file which we add
			# (e.g. tribefire logo image file) --> no review required
			:
		fi
	done

	if [ "${reviewRequired}" == "true" ]; then
		echo "ATTENTION! Important differences or changes were found. These must be reviewed."
	else
		echo "(No relevant differences found.)"
	fi

	# since this function prints using echo, we can't use echo to return the result
	# instead we use a global variable
	FUNCTION_RETURN_VALUE=${reviewRequired}
}


# arguments
TOMCAT_VERSION='NOT_SET' # not mandatory (not providing a value just skips the Tomcat version update)
TOMCAT_JULI_ARTIFACT='' # mandatory
TOMCAT_EXTENSIONS_ARTIFACT='' # mandatory ('[user]:[password]')
ARTIFACTORY_REPOSITORY_BASE_URL='' # mandatory ('https://artifactory.example.org/artifactory/example-repository')
ARTIFACTORY_CREDENTIALS='' # mandatory
DEV_LOADER_ARTIFACT='' # mandatory
DIFF_TOOL='bcompare' # the diff tool (just for log libes); default is beyond compare
STOP_IF_REVIEW_REQUIRED='true' # locally it's usually better to stop, so that one can compare new and old Tomcat

while [[ $# -gt 0 ]]; do
	key="$1"

	case $key in
		--tomcatVersion)
			TOMCAT_VERSION="$2"
			shift # past argument
			shift # past value
			;;
		--tomcatJuliArtifact)
			TOMCAT_JULI_ARTIFACT="$2"
			shift # past argument
			shift # past value
			;;
		--tomcatExtensionsArtifact)
			TOMCAT_EXTENSIONS_ARTIFACT="$2"
			shift # past argument
			shift # past value
			;;
		--artifactoryRepositoryBaseUrl)
			ARTIFACTORY_REPOSITORY_BASE_URL="$2"
			shift # past argument
			shift # past value
			;;
		--artifactory-credentials)
			ARTIFACTORY_CREDENTIALS="$2"
			shift # past argument
			shift # past value
			;;
		--devLoaderArtifact)
			DEV_LOADER_ARTIFACT="$2"
			shift # past argument
			shift # past value
			;;
		--diff-tool)
			DIFF_TOOL="$2"
			shift # past argument
			shift # past value
			;;
		--stopIfReviewRequired)
			STOP_IF_REVIEW_REQUIRED="$2"
			shift # past argument
			shift # past value
			;;
		*)    # unknown option
			echo "Unknown option $key"
			show_help
			shift # past argument
			;;
	esac
done

if [ -z "$TOMCAT_JULI_ARTIFACT" ];
then
	echo 'Please provide fully qualified name for the tomcat-juli replacement (usually logging-juli-extensions)!'
	exit 2
fi

if [ -z "$TOMCAT_EXTENSIONS_ARTIFACT" ];
then
	echo 'Please provide fully qualified name for the tomcat-extensions!'
	exit 3
fi

if [ -z "$ARTIFACTORY_REPOSITORY_BASE_URL" ];
then
	echo 'Please provide Artifactory repository base URL!'
	exit 4
fi

if [ -z "$ARTIFACTORY_CREDENTIALS" ];
then
	echo 'Please provide Artifactory credentials!'
	exit 5
fi

if [ -z "$DEV_LOADER_ARTIFACT" ];
then
	echo 'Please provide fully qualified name for the dev-loader!'
	exit 6
fi


echo '************************************************************************************************************************'
echo '* Start tomcat-runtime asset update with the following settings:'
echo '************************************************************************************************************************'

TIMESTAMP=$(date +%Y%m%d%H%M%S)

# use relative path as base dir get shorter paths in logging
#BASE_PATH_PREFIX="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$( dirname "${BASH_SOURCE[0]}" )"
BASE_PATH_PREFIX=""

RUNTIME_ORIGINAL_DIR="${BASE_PATH_PREFIX}runtime-original"
RUNTIME_CUSTOM_DIR="${BASE_PATH_PREFIX}runtime-modifications/custom"
RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR="${BASE_PATH_PREFIX}runtime-modifications/custom-auto-maintained"
# the scipt uses this directory to download binaries such as the Tomcat archive or the dev-loader.
# Tomcat will also be extracted there.
RUNTIME_UPDATE_DIR="${BASE_PATH_PREFIX}runtime-update/${TIMESTAMP}"

echo "TOMCAT_VERSION:                      ${TOMCAT_VERSION}"
echo "TOMCAT_JULI_ARTIFACT:                ${TOMCAT_JULI_ARTIFACT}"
echo "TOMCAT_EXTENSIONS_ARTIFACT:          ${TOMCAT_EXTENSIONS_ARTIFACT}"
echo "ARTIFACTORY_REPOSITORY_BASE_URL:     ${ARTIFACTORY_REPOSITORY_BASE_URL}"
echo "DEV_LOADER_ARTIFACT:                 ${DEV_LOADER_ARTIFACT}"
echo "RUNTIME_ORIGINAL_DIR:                ${RUNTIME_ORIGINAL_DIR}"
echo "RUNTIME_CUSTOM_DIR:                  ${RUNTIME_CUSTOM_DIR}"
echo "RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR:  ${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}"
echo "RUNTIME_UPDATE_DIR:                  ${RUNTIME_UPDATE_DIR}"

mkdir -p ${RUNTIME_UPDATE_DIR}


# update Tomcat
if [ "$TOMCAT_VERSION" != "NOT_SET" ];then

	echo
	echo '************************************************************************************************************************'
	echo "* Download and extract Tomcat ${TOMCAT_VERSION}."
	echo '************************************************************************************************************************'

	# parse major version from full version, e.g. 10.0.1 -> 10
	TOMCAT_MAJOR_VERSION=$(echo "${TOMCAT_VERSION}" | cut --delimiter=. --fields 1)

	# download Tomcat archive
	TOMCAT_ARCHIVE_URL="https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}"
	TOMCAT_ARCHIVE_FILENAME="apache-tomcat-${TOMCAT_VERSION}-windows-x64.zip"
	FULL_TOMCAT_ARCHIVE_URL="${TOMCAT_ARCHIVE_URL}/v${TOMCAT_VERSION}/bin/${TOMCAT_ARCHIVE_FILENAME}"
	curl -s ${FULL_TOMCAT_ARCHIVE_URL} -o ${RUNTIME_UPDATE_DIR}/${TOMCAT_ARCHIVE_FILENAME}

	# extract Tomcat archive
	unzip -q ${RUNTIME_UPDATE_DIR}/${TOMCAT_ARCHIVE_FILENAME} -d ${RUNTIME_UPDATE_DIR}
	mv "${RUNTIME_UPDATE_DIR}/apache-tomcat-${TOMCAT_VERSION}" "${RUNTIME_UPDATE_DIR}/host"

	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo '* Check if there are changes in original Tomcat files which will be replaced by custom files (e.g. tomcat-users.xml).'
	echo '************************************************************************************************************************'

	compareOriginalAndNewTomcatForChangesInCustomFiles ${RUNTIME_CUSTOM_DIR} ${RUNTIME_ORIGINAL_DIR} ${RUNTIME_UPDATE_DIR} true
	CUSTOM_FILES_REVIEW_REQUIRED=${FUNCTION_RETURN_VALUE}
	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo '* Check if there are changes in original Tomcat files which will be replaced by custom files which are automatically'
	echo '* maintained (such as catalina.sh.vm).'
	echo '************************************************************************************************************************'

	compareOriginalAndNewTomcatForChangesInCustomFiles ${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR} ${RUNTIME_ORIGINAL_DIR} ${RUNTIME_UPDATE_DIR} false
	CUSTOM_AUTO_MAINTAINED_FILES_REVIEW_REQUIRED=${FUNCTION_RETURN_VALUE}
	echo 'Done.'


	if [ "${CUSTOM_FILES_REVIEW_REQUIRED}" == 'true' ] || [ "${CUSTOM_AUTO_MAINTAINED_FILES_REVIEW_REQUIRED}" == 'true' ];then
		echo
		echo 'There are changes between the two Tomcat versions which must be required (see logs above):'
		echo "  ${DIFF_TOOL} ${RUNTIME_ORIGINAL_DIR}/host/ ${RUNTIME_UPDATE_DIR}/host/"

		if [ "${STOP_IF_REVIEW_REQUIRED}" == 'true' ]; then
			echo
			echo 'The update script stops here.'
			echo

			echo
			exit 1
		fi
	fi


	echo
	echo '************************************************************************************************************************'
	echo "* Update original Tomcat dir ${RUNTIME_ORIGINAL_DIR}."
	echo '************************************************************************************************************************'

	# delete original runtime
	rm -rf "${RUNTIME_ORIGINAL_DIR}"

	# copy updated runtime
	mkdir -p "${RUNTIME_ORIGINAL_DIR}/host"
	cp -r "${RUNTIME_UPDATE_DIR}/host/." "${RUNTIME_ORIGINAL_DIR}/host/"

	# delete folders which we do not need
	rm -rf "${RUNTIME_ORIGINAL_DIR}/host/webapps/docs" "${RUNTIME_ORIGINAL_DIR}/host/webapps/examples" "${RUNTIME_ORIGINAL_DIR}/host/temp"

	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo "* Find all files where 'org.apache.juli.ClassLoaderLogManager' is specified,"
	echo "* copy those files to folder 'custom-auto-maintained' and"
	echo "* replace 'org.apache.juli.ClassLoaderLogManager' with 'com.braintribe.logging.juli.BtClassLoaderLogManager'."
	echo '************************************************************************************************************************'

	for filepath in $(find "${RUNTIME_ORIGINAL_DIR}/host" -type f -regex '.*\.\(xml\|bat\|sh\|policy\)'); do
		relative_filepath=${filepath#${RUNTIME_ORIGINAL_DIR}/host/}
		original_filepath="${RUNTIME_ORIGINAL_DIR}/host/${relative_filepath}"
		if grep -q "org.apache.juli.ClassLoaderLogManager" ${original_filepath};then
			target_filepath="${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/${relative_filepath}"
			cp "${original_filepath}" "${target_filepath}"
			sed -i "s/org.apache.juli.ClassLoaderLogManager/com.braintribe.logging.juli.BtClassLoaderLogManager/g" "${target_filepath}"
		fi
	done
	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo "* Generate the velocity template 'catalina.sh.vm' based on 'catalina.sh'."
	echo '************************************************************************************************************************'
	# We know that 'catalina.sh' exists in 'custom-auto-maintained' folder, because it contains
	# 'org.apache.juli.ClassLoaderLogManager' and was thus copied in the previous step (see above).
	# (Should this change in a future Tomcat version, the move command will just fail here, i.e. we will notice it easily.)

	CATALINA_SH_FILEPATH="${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/bin/catalina.sh"
	CATALINA_SH_VM_FILEPATH="${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/_projection/host/bin/catalina.sh.vm"
	mv "${CATALINA_SH_FILEPATH}" "${CATALINA_SH_VM_FILEPATH}"

	# replace \$ with \\$
	sed -i 's/\(\\\$\)/\\\1/g' ${CATALINA_SH_VM_FILEPATH}
	# replace $ with ${dollar}
	sed -i 's/\$/\${dollar}/g' ${CATALINA_SH_VM_FILEPATH}

	# replace the original 'CATALINA_OUT=...' line ...
	ORIGINAL_CATALINA_OUT_STRING='CATALINA_OUT="${dollar}CATALINA_BASE"\/logs\/catalina.out'
	# ... with the following multi-line string which respects the configured log files directory
	REPLACEMENT_CATALINA_OUT_STRING="\\
#if\(\${request.logFilesDir}\)\\
  CATALINA_OUT=\"\$tools.resolveRelativePath\(\$installationPath,\$request.logFilesDir)\/catalina.out\"\\
#else\\
  CATALINA_OUT=\"\${dollar}CATALINA_BASE\/logs\/catalina.out\"\\
#end"
	sed -i "s/${ORIGINAL_CATALINA_OUT_STRING}/${REPLACEMENT_CATALINA_OUT_STRING}/g" ${CATALINA_SH_VM_FILEPATH}

	# inject variable declarations in the beginning of the file
	# (note that lines are added in reverse order, i.e. the dollar declaration will be the first line)
	INJECT_STRING='#set($installationPath="${dollar}{CATALINA_BASE}\/..\/..")\n'
	sed -i "1s/^/${INJECT_STRING}/" ${CATALINA_SH_VM_FILEPATH}
	INJECT_STRING="#set(\$dollar='\$')\n"
	sed -i "1s/^/${INJECT_STRING}/" ${CATALINA_SH_VM_FILEPATH}

	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo "* Generate the velocity template 'service.bat.vm' based on 'catalina.bat'."
	echo '************************************************************************************************************************'
	# We know that 'catalina.bat' exists, see 'catalina.sh.vm' creation above.

	SERVICE_BAT_FILEPATH="${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/bin/service.bat"
	SERVICE_BAT_VM_FILEPATH="${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/_projection/host/bin/service.bat.vm"
	mv "${SERVICE_BAT_FILEPATH}" "${SERVICE_BAT_VM_FILEPATH}"

	# replace the line '--LogPath ...' line ...
	ORIGINAL_LOG_PATH_STRING='--LogPath "%CATALINA_BASE%\\logs"\(.*\)'
	# ... with the following multi-line string which respects the configured log files directory
	REPLACEMENT_LOG_PATH_STRING="\\
#if(\${request.logFilesDir})\\
    --LogPath \"\$tools.resolveRelativePath(\$installationPath,\$request.logFilesDir)\"\1\\
#else\\
    --LogPath \"%CATALINA_BASE%\\\\logs\"\1\\
#end"
	sed -i "s/${ORIGINAL_LOG_PATH_STRING}/${REPLACEMENT_LOG_PATH_STRING}/g" ${SERVICE_BAT_VM_FILEPATH}

	# inject variable declaration in the beginning if the file
	INJECT_STRING='#set($installationPath="%CATALINA_BASE%\/..\/..")\n'
	sed -i "1s/^/${INJECT_STRING}/" ${SERVICE_BAT_VM_FILEPATH}

	# replace Linux line separators with Windows line separators
	# (redirect to /dev/null, because output is not interesting and --quiet option isn't available on Alpine)
	unix2dos ${SERVICE_BAT_VM_FILEPATH} > /dev/null 2>&1

	echo 'Done.'


	echo
	echo '************************************************************************************************************************'
	echo "* Append Tribefire section to 'catalina.properties'."
	echo '************************************************************************************************************************'
	CATALINA_PROPERTIES_TRIBEFIRE_SECTION_STRING='
###############################################################################
### Tribefire Properties and Settings                                       ###
###############################################################################

TRIBEFIRE_CONTAINER_ROOT_DIR=${catalina.base}
TRIBEFIRE_INSTALLATION_ROOT_DIR=${TRIBEFIRE_CONTAINER_ROOT_DIR}/../..
TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION=${TRIBEFIRE_INSTALLATION_ROOT_DIR}/conf/tribefire.properties

org.apache.tomcat.util.digester.PROPERTY_SOURCE=com.braintribe.tomcat.extension.EncryptedPropertySource,com.braintribe.tomcat.extension.DefaultAwareEnvironmentPropertySource
'
	CATALINA_PROPERTIES_RELATIVE_FILEPATH='host/conf/catalina.properties'
	cp "${RUNTIME_ORIGINAL_DIR}/${CATALINA_PROPERTIES_RELATIVE_FILEPATH}" "${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/${CATALINA_PROPERTIES_RELATIVE_FILEPATH}"
	echo "${CATALINA_PROPERTIES_TRIBEFIRE_SECTION_STRING}" >> "${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/${CATALINA_PROPERTIES_RELATIVE_FILEPATH}"

	echo 'Done.'
fi # end Tomcat update


echo
echo '************************************************************************************************************************'
echo '* Download tomcat-juli replacement.'
echo '************************************************************************************************************************'
curl -s --user "${ARTIFACTORY_CREDENTIALS}" "${ARTIFACTORY_REPOSITORY_BASE_URL}/$(repositoryRelativeJarFilePath ${TOMCAT_JULI_ARTIFACT})" -o ${RUNTIME_UPDATE_DIR}/tomcat-juli.jar
cp "${RUNTIME_UPDATE_DIR}/tomcat-juli.jar" "${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/bin/"
echo 'Done.'


echo
echo '************************************************************************************************************************'
echo '* Download tomcat-extensions.'
echo '************************************************************************************************************************'
curl -s --user "${ARTIFACTORY_CREDENTIALS}" "${ARTIFACTORY_REPOSITORY_BASE_URL}/$(repositoryRelativeJarFilePath ${TOMCAT_EXTENSIONS_ARTIFACT})" -o ${RUNTIME_UPDATE_DIR}/tomcat-extensions.jar
cp "${RUNTIME_UPDATE_DIR}/tomcat-extensions.jar" "${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/lib/"
echo 'Done.'


echo
echo '************************************************************************************************************************'
echo '* Download dev-loader.'
echo '************************************************************************************************************************'
curl -s --user "${ARTIFACTORY_CREDENTIALS}" "${ARTIFACTORY_REPOSITORY_BASE_URL}/$(repositoryRelativeJarFilePath ${DEV_LOADER_ARTIFACT})" -o ${RUNTIME_UPDATE_DIR}/dev-loader.jar
cp "${RUNTIME_UPDATE_DIR}/dev-loader.jar" "${RUNTIME_CUSTOM_AUTO_MAINTAINED_DIR}/host/lib/"
echo 'Done.'


echo
echo '************************************************************************************************************************'
echo '* Update finished.'
echo '************************************************************************************************************************'
