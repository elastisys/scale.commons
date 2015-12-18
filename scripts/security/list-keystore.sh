#!/bin/bash

#
# displays the contents of a Java Key Store
#

if [ "${1}" = "" ]; then
    echo "error: expecting a PKCS12/JKS-encoded keystore as only input."
    exit 1
fi
keystore=${1}
keytool -list -v -keystore ${keystore}
