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
sed -i "s~@INFINISPAN_POSTGRES_CONN_STRING@~${INFINISPAN_POSTGRES_CONN_STRING}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_USERNAME@~${INFINISPAN_POSTGRES_USERNAME}~g" ${WF_CONFIG}
sed -i "s~@INFINISPAN_POSTGRES_PASSWORD@~${INFINISPAN_POSTGRES_PASSWORD}~g" ${WF_CONFIG}

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
 -Djboss.node.name="${HOSTNAME}" \
 -Djboss.host.name="${HOSTNAME}" \
 -Djboss.qualified.host.name="${HOSTNAME}" \
 -Djboss.as.management.blocking.timeout=1800 \
 -Djboss.jgroups.azure_ping.storage_account_name="${INFINISPAN_AZURE_ACCOUNT_NAME}" \
 -Djboss.jgroups.azure_ping.storage_access_key="${INFINISPAN_AZURE_ACCESS_KEY}" \
 -Djboss.jgroups.azure_ping.container="${INFINISPAN_AZURE_CONTAINER}" \
 -Dinfinispan_loglevel="${INFINISPAN_LOGLEVEL:-INFO}" \
 -Dinfinispan_hostname="${HOSTNAME}"
