#!/bin/bash

# @author Michal Karm Babacek

# Debug logging
echo "STAT: `networkctl status`" >> /opt/infinispan/ip.log
echo "STAT ${INFINISPAN_NIC:-eth0}: `networkctl status ${INFINISPAN_NIC:-eth0}`" >> /opt/infinispan/ip.log

# Wait for the interface to wake up.
# Some providers, such as Tutum, Docker Cloud or Rancher don't bring up
# the overlay network within the container immediately.
TIMEOUT=20
MYIP=""
while [[ "${MYIP}X" == "X" ]] && [[ "${TIMEOUT}" -gt 0 ]]; do
    echo "Loop ${TIMEOUT}" >> /opt/infinispan/ip.log
    MYIP="`networkctl status ${INFINISPAN_NIC:-eth0} | awk '{if($1~/Address:/){printf($2);}}'`"
    export MYIP
    let TIMEOUT=$TIMEOUT-1
    if [[ "${MYIP}" == ${INFINISPAN_ADDR_PREFIX:-10}* ]]; then
        break;
    else 
        MYIP=""
        sleep 1;
    fi
done
echo -e "MYIP: ${MYIP}\nMYNIC: ${INFINISPAN_NIC:-eth0}" >> /opt/infinispan/ip.log
if [[ "${MYIP}X" == "X" ]]; then 
    echo "${INFINISPAN_NIC:-eth0} Interface error. " >> /opt/infinispan/ip.log
    exit 1
fi

# Wildfly constraint
if [ "`echo \"${HOSTNAME}\" | wc -c`" -gt 24 ]; then
    echo "ERROR: HOSTNAME ${HOSTNAME} must be up to 24 characters long."
    exit 1
fi

# Replace NIC. Why not parameter?
# "WFLYCTL0095: Illegal value EXPRESSION for interface criteria nic; must be STRING"
sed -i "s~@INFINISPAN_NIC@~${INFINISPAN_NIC:-eth0}~g" ${WF_CONFIG}

sed -i "s~@INFINISPAN_GMS_MAX_JOIN_ATTEMPTS@~${INFINISPAN_GMS_MAX_JOIN_ATTEMPTS:-10}~g" ${WF_CONFIG}

sed -i "s~@INFINISPAN_POSTGRES_DB@~${INFINISPAN_POSTGRES_DB}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_PORT@~${INFINISPAN_POSTGRES_PORT}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_HOSTNAME@~${INFINISPAN_POSTGRES_HOSTNAME}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_USERNAME@~${INFINISPAN_POSTGRES_USERNAME}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_PASSWORD@~${INFINISPAN_POSTGRES_PASSWORD}~g" ${WF_CONFIG}

# Default life spans of cache entries
sed -i "s~@INFINISPAN_IOC_CACHE_LIFESPAN_MS@~$((${INFINISPAN_IOC_CACHE_LIFESPAN_HOURS:-336}*60*60*1000))~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_WHITELIST_LIFESPAN_MS@~$((${INFINISPAN_WHITELIST_LIFESPAN_HOURS:-336}*60*60*1000))~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_GSB_LIFESPAN_MS@~$((${INFINISPAN_GSB_LIFESPAN_HOURS:-336}*60*60*1000))~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_CACHE_PERIODIC_PURGE_MS@~$((${INFINISPAN_CACHE_PERIODIC_PURGE_HOURS:-24}*60*60*1000))~g" ${WF_CONFIG}

# Eviction - default none
sed -i "s~@INFINISPAN_BLACKLIST_EVICTION_MAX_ENTRIES@~${INFINISPAN_BLACKLIST_EVICTION_MAX_ENTRIES:-"-1"}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_BLACKLIST_EVICTION_STRATEGY@~${INFINISPAN_BLACKLIST_EVICTION_STRATEGY:-"NONE"}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_WHITELIST_EVICTION_MAX_ENTRIES@~${INFINISPAN_WHITELIST_EVICTION_MAX_ENTRIES:-"-1"}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_WHITELIST_EVICTION_STRATEGY@~${INFINISPAN_WHITELIST_EVICTION_STRATEGY:-"NONE"}~g" ${WF_CONFIG}


# Better be volumes
mkdir /tmp/${HOSTNAME}-persist
mkdir /tmp/${HOSTNAME}-temp
sed -i "s~@INFINISPAN_PERSISTENT_LOC@~${INFINISPAN_PERSISTENT_LOC:-/tmp/${HOSTNAME}-persist}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_TEMP_LOC@~${INFINISPAN_TEMP_LOC:-/tmp/${HOSTNAME}-temp}~g" ${WF_CONFIG}

/opt/infinispan/infinispan-server/bin/standalone.sh \
 -b ${MYIP} \
 -c clustered.xml \
 -Djava.net.preferIPv4Stack=true \
 -Djboss.modules.system.pkgs=org.jboss.byteman \
 -Djava.awt.headless=true \
 -Djboss.bind.address.management=${MYIP} \
 -Djboss.bind.address=${MYIP} \
 -Djboss.bind.address.private=${MYIP} \
 -Djgroups.bind_addr=${MYIP} \
 -Djgroups.tcp.address=${MYIP} \
 -Djboss.node.name="${TEST_NODE_NAME:-$HOSTNAME}" \
 -Djboss.host.name="${HOSTNAME}" \
 -Djboss.qualified.host.name="${HOSTNAME}" \
 -Djboss.as.management.blocking.timeout=1800 \
 -Djboss.jgroups.azure_ping.storage_account_name="${INFINISPAN_AZURE_ACCOUNT_NAME}" \
 -Djboss.jgroups.azure_ping.storage_access_key="${INFINISPAN_AZURE_ACCESS_KEY}" \
 -Djboss.jgroups.azure_ping.container="${INFINISPAN_AZURE_CONTAINER}" \
 -Dinfinispan_loglevel="${INFINISPAN_LOGLEVEL:-INFO}" \
 -Dinfinispan_hostname="${HOSTNAME}" \
 -Djboss.socket.binding.port-offset="${TEST_NODE_OFFSET:-0}"
