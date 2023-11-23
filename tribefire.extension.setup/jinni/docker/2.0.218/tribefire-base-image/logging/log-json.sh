#!/bin/bash

show_help () {
	echo "Description:"
	echo "	Prints the specified string in a json format. The result json contains the following keys/values:
	1. iso8601Utc: the timestamp of the log message - equals to local time with timezone offset at the end
	2. loggerName: the logger name - equals to 'script'
	3. level: the log level (e.g. ERROR, WARN, INFO, DEBUG)
		--level, -l
	4. the logging message - this is either passed as a string argument or as a content from a file
		--message, -m or --file, -f"
	echo "Syntax:"
	echo "  $0 (--file <FILEPATH> | --message <LOG_MESSAGE>)  [--level <LOG_LEVEL>]"
	echo "Example:"
	echo "  $0 --message 'Hello world!' --level DEBUG"
}

LOG_MESSAGE=""
LOG_LEVEL='INFO' # default value is set to INFO
FILEPATH=""

while [[ $# -gt 0 ]]; do
	key="$1"

	case $key in
		--file | -f)
			FILEPATH="$2"
			shift # past argument
			shift # past value
			;;
		--message | -m)
			LOG_MESSAGE="$2"
			shift # past argument
			shift # past value
			;;
		--level | -l)
			LOG_LEVEL="$2"
			shift # past argument
			shift # past value
			;;
		*)	# when there is no flag
			echo "Not supported argument: ${1}"
			exit 1
			;;
	esac
done

if [[ -z ${FILEPATH} && -z ${LOG_MESSAGE} ]];
then
	echo "Please provide either the LOG_MESSAGE or the FILEPATH of the file to be logged."
	show_help
	exit 2
fi

if [[ ! -z ${FILEPATH} && ! -z ${LOG_MESSAGE} ]];
then
	echo "Please provide either the LOG_MESSAGE or the FILEPATH of the file to be logged. Not both!"
	show_help
	exit 3
fi

if [[ ! -z ${FILEPATH} ]];
then
	LOG_MESSAGE=$(cat ${FILEPATH})
fi

LOG_DATE=$(date +%FT%T.%3N%z)
jq -Rn  -n -c -M \
	--arg lm "${LOG_MESSAGE}" \
	--arg ll "${LOG_LEVEL}" \
	--arg ld "${LOG_DATE}" \
			'{"iso8601Utc": $ld,"level":$ll,"loggerName":"script", "message":$lm}'
