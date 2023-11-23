#!/bin/sh

function replace_echo_lines {

	LIST_SH_FILES=$(find /opt/tribefire/runtime/host/bin -type f -name '*.sh')
	for SH_FILE in ${LIST_SH_FILES}; do
		# Replace echo->log-json.sh in lines that start with 'echo "' with 0 or more white spaces in the beginning of the line
		sed -i 's/^\([ ]*\)echo[ ]\+["]\(.*\)["]\(.*\)$/\1log-json.sh -m "\2"\3/g' "${SH_FILE}"
		# Replace echo->log-json.sh in lines that start with '*:*) echo' with 0 or more white spaces in the beginning of the line
		sed -i 's/^\([ ]*\)\([\*]:[\*][\)]\)\([ ]*\)echo[ ]\+["]\(.*\)["]\(.*\)$/\1\2\3log-json.sh -m "\4"\5/g' "${SH_FILE}"
		# Replace empty echo lines
		sed -i 's/^\([ ]*\)echo$/\1log-json.sh -m ""/g' "${SH_FILE}"
		# After several replacements (echo->log-json.sh), we need to check if there are still some lines that contain an 'echo' command.
		# The following command iterates through all *.sh files and checks for occurances of 'echo' excluding some known lines.
		FILE_CONTENT=$(cat "${SH_FILE}")
		FILE_CONTENT=$(sed '/echo $1 \| grep "[^0-9]" >\/dev\/null 2>&1/d' <<< "${FILE_CONTENT}")
		FILE_CONTENT=$(sed '/echo $! > "$CATALINA_PID"/d' <<< "${FILE_CONTENT}")
		FILE_CONTENT=$(sed '/2\\>\\&1 \\&\\& echo \\$! \\>\\"$catalina_pid_file\\" \\; \\} $catalina_out_command "&"/d' <<< "${FILE_CONTENT}")
		echo "${FILE_CONTENT}" | grep "echo" # returns 0 if there is a matching line, otherwise returns 1
		if [[ "$?" -eq 0 ]];then # last element of the PIPESTATUS
			echo "Found unexpected lines that contain the echo command in file ${SH_FILE}"
			exit 100
		fi
	done
}

if [ ! -z "${ENVIRONMENT_SPECIFIC_URL_REWRITE_RULES}" ]; then
	# there are environment specific rewrite rules which we have to add to the rewrite config file
	URL_REWRITE_CONFIG=/opt/tribefire/runtime/host/conf/Catalina/localhost/rewrite.config

	if [ -f ${URL_REWRITE_CONFIG} ]; then
		# we already have a rewrite config with application specific rules. the environment specific rules must be the first ones. thus we first rename file and then append rules later (see below).
		mv ${URL_REWRITE_CONFIG} ${URL_REWRITE_CONFIG}.original
	else
		mkdir --parents $(dirname ${URL_REWRITE_CONFIG})
	fi

	# create rewrite configuration file with environment specific rules
	echo -e "${ENVIRONMENT_SPECIFIC_URL_REWRITE_RULES}" > ${URL_REWRITE_CONFIG}

	if [ -f ${URL_REWRITE_CONFIG}.original ]; then
		# append application rules. they will be applied AFTER the environment rules.
		echo '' >> ${URL_REWRITE_CONFIG}
		cat ${URL_REWRITE_CONFIG}.original >> ${URL_REWRITE_CONFIG}
	fi
fi

SYSTEM_INFO=$(
echo '********************************************************************************'
echo '*** System Info ****************************************************************'
echo '********************************************************************************'
echo '*** Operating System ***'
cat /etc/os-release
echo '*** Java ***'
java -version 2>&1
echo '*** Environment Variables ***'
env | sort | grep -v 'CREDENTIAL\|PASS\|SECRET\|TOKEN'
echo '********************************************************************************'
echo '********************************************************************************'
)

if [ "${DEBUG_PORTS_ENABLED}" == "true" ]; then
	export JPDA_ADDRESS=*:8000
	export JPDA_TRANSPORT=dt_socket
	OPTIONAL_CATALINA_ARGS="jpda"
else
	OPTIONAL_CATALINA_ARGS=""
fi

if { [ -n "${KUBERNETES_SERVICE_HOST}" ] && [ "${JSON_LOGGING_ENABLED}" != "false" ]; } || [ "${JSON_LOGGING_ENABLED}" == "true" ]; then
	log-json.sh -m "${SYSTEM_INFO}"
	replace_echo_lines
	cp /opt/tribefire/runtime/host/conf/json-logging.properties /opt/tribefire/runtime/host/conf/logging.properties
else
	echo "${SYSTEM_INFO}"
	cp /opt/tribefire/runtime/host/conf/default-logging.properties /opt/tribefire/runtime/host/conf/logging.properties
fi
rm -f /opt/tribefire/runtime/host/conf/default-logging.properties
rm -f /opt/tribefire/runtime/host/conf/json-logging.properties

source /opt/tribefire/runtime/host/bin/catalina.sh ${OPTIONAL_CATALINA_ARGS} run
