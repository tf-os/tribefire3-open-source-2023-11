#!/usr/bin/env bash

# For more info on what this thing does please see the README.md file in the /hack directory

# Reset in case getopts has been used previously in the shell.
OPTIND=1

# initialize our script options
namespace=""
username=""
password=""
service_account_file=""
verbose=0
force=0
yes=0

#
#
# FUNCTIONS
#
#

#
# help screen
#
function show_help {
    cmd=$(basename $0)
    echo "Usage: ${cmd} <options> <secret_name> where options is:";
    echo " -h               Show this message"
    echo " -v               Verbose mode"
    echo " -f               If set, remove secrets before creating them if they exist"
    echo " -y               Don't prompt for secret creation"
    echo " -n <namespace>   Create secrets in <namespace>. Mandatory"
    echo " -u <username>    Use <username> for image pull secret. "
    echo " -p <password>    Use <password> for image pull secret. "
    echo " -a <filename>    Use <filename> for database service account."
    echo " -k <key>         Use <key> for database service account Kubernetes secret key, '-k braintribe.json' makes "
    echo "                  the service account JSON available under the secret key 'braintribe.json'"
    echo
    echo "Examples: "
    echo
    echo "# create a db secret with two keys 'username=dev' and 'password=secret'  name dev-db-credentials"
    echo "${cmd} -n test -u dev -p secret -- dev-db-credentials"
    echo
    echo "# create a db service account from 'cloudsql.json'  under key 'bt-db.json'"
    echo "${cmd} -n test -a cloudsql.json -k bt-db.json -- dev-db-credentials"
}

#
# checks if secret exists and delete
#
function handle_existing_secrets {
    secret=$1
    ns=$2
    force=$3
    res=$(kubectl get secret ${secret} -n ${ns} 2>&1)
    not_exists=$?
    if [[ ${not_exists} -eq 0 ]] && [[ "${force}" -eq 1 ]]; then
        res=$(kubectl delete secret ${secret} -n ${ns})
        return 0;
    fi

    if [[ ${not_exists} -eq 0 ]]; then
        echo "Secret ${secret} already exists in namespace ${ns}"
        exit 1;
    fi
}

#
# prompts user for creation confirmation
#
function prompt_user {
    secret=$1
    namespace=$2
    if [[ ${yes} -eq 0 ]]; then
        read -p "Create secret ${secret_name} in namespace ${namespace}? (y/N) " confirm
        if [[ "${confirm}" != "y" ]]; then
            echo "Nothing done";
            exit 1
        fi
    fi
}

#
#
# MAIN
#
#

# parse options
while getopts "h?vtyfn:u:p:a:k:" opt; do
    case "$opt" in
    h|\?)
        show_help
        exit 0
        ;;
    v)  verbose=1
        ;;
    f)  force=1
        ;;
    y)  yes=1
        ;;
    n)  namespace=$OPTARG
        ;;
    u)  username=$OPTARG
        ;;
    p)  password=$OPTARG
        ;;
    a)  service_account_file=$OPTARG
        ;;
    k)  service_account_key=$OPTARG
        ;;
    esac
done

shift $((OPTIND-1))
[[ "${1:-}" = "--" ]] && shift

secret_name=$@

# check that we have a name for the new secret
if [[ -z "${secret_name}" ]]; then
    echo "Provide name of the secret as the final argument!"
    show_help
    exit 1;
fi

# check mandatory options
if [[ -z "${namespace}" ]]; then
    echo "No namespace set. Use -n <namespace>";
    show_help
    exit 1;
fi

# if username option is given, create DB credentials
if [[ -n "${username}" ]]; then
    if [[ -z "${password}" ]]; then
        echo "Creating database credentials requires both -u <username> and -p <password>."
        exit 1;
    fi

    handle_existing_secrets ${secret_name} ${namespace} ${force}

    prompt_user ${secret_name} ${namespace}

    if [[ ${yes} -eq 0 ]]; then
        read -p "Create database credentials ${secret_name} in namespace ${namespace}? (y/N) " confirm
        if [[ "${confirm}" != "y" ]]; then
            echo "Nothing done";
            exit 1
        fi
    fi

    res=$(kubectl create secret generic ${secret_name} -n ${namespace} \
        --from-literal=username=${username} \
         --from-literal=password=${password} 2>&1)

    if [[ $? != "0" ]]; then
        echo "Unable to create database credentials: ${res}";
        exit 1
    fi

    echo "Created database credentials in namespace ${namespace}"
    exit 0
fi

if [[ ! -f "${service_account_file}" ]]; then
    echo "Service account file ${service_account_file} does not exist";
    exit 1
fi

if [[ -z ${service_account_key} ]]; then
    echo "No service account key set. Please provide one with -k"
    show_help;
    exit 1;
fi

handle_existing_secrets ${secret_name} ${namespace} ${force}

prompt_user ${secret_name} ${namespace}

# if service account option is set, create a secret that holds the service account for CloudSQL proxy
res=$(kubectl create secret generic ${secret_name} -n ${namespace} --from-file=${service_account_key}=${service_account_file} 2>&1)
if [[ $? != "0" ]]; then
    echo "Unable to create service account secret: ${res}";
    exit 1
fi

echo "Created database service account in namespace ${namespace}"
exit 0;