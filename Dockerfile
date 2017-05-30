FROM fedora:24
MAINTAINER Michal Karm Babacek <karm@email.cz
LABEL description="Infinispan Server"

ENV DEPS            java-1.8.0-openjdk-devel.x86_64 unzip gawk sed wget jna.x86_64 jsch-agent-proxy-usocket-jna.noarch
ENV JBOSS_HOME      "/opt/infinispan/infinispan-server"
ENV JAVA_HOME       "/usr/lib/jvm/java-1.8.0"

RUN dnf -y update && dnf -y install ${DEPS} && dnf clean all
RUN useradd -s /sbin/nologin infinispan
RUN mkdir -p /opt/infinispan && chown infinispan /opt/infinispan && chgrp infinispan /opt/infinispan && chmod ug+rwxs /opt/infinispan

WORKDIR /opt/infinispan
USER infinispan

EXPOSE 8080/tcp
EXPOSE 8009/tcp
EXPOSE 8443/tcp
EXPOSE 8181/tcp
EXPOSE 11223/tcp
EXPOSE 11222/tcp
EXPOSE 4712-4713/tcp
EXPOSE 7600-7620/tcp
EXPOSE 57600-57620/tcp

ENV INFINISPAN_SERVER_VERSION 8.2.6.Final
ENV WF_CONFIG /opt/infinispan/infinispan-server/standalone/configuration/clustered.xml
ENV WF_MODULES /opt/infinispan/infinispan-server/modules/system/layers/base
RUN \
# Fetch Infinispan Server
    wget http://downloads.jboss.org/infinispan/${INFINISPAN_SERVER_VERSION}/infinispan-server-${INFINISPAN_SERVER_VERSION}-bin.zip && \
    unzip infinispan-server-${INFINISPAN_SERVER_VERSION}-bin.zip && rm -rf infinispan-server-${INFINISPAN_SERVER_VERSION}-bin.zip && \
# Workaround for https://github.com/docker/docker/issues/5509
    ln -s /opt/infinispan/infinispan-server-${INFINISPAN_SERVER_VERSION} /opt/infinispan/infinispan-server && \
    sed -i 's/<dependencies>/<dependencies>\n        <module name="org.jgroups.azure"\/>/g' \
        ${WF_MODULES}/org/jgroups/main/module.xml && \
    mkdir -p ${WF_MODULES}/com/microsoft/azure/storage/main/ && \
    mkdir -p ${WF_MODULES}/org/jgroups/azure/main/ && \
    mkdir -p ${WF_MODULES}/org/postgresql/main/ && \
    wget --tries=30 --continue http://central.maven.org/maven2/org/jgroups/jgroups-azure/1.0.0.Final/jgroups-azure-1.0.0.Final.jar -O ${WF_MODULES}/org/jgroups/azure/main/jgroups-azure-1.0.0.Final.jar && \
    wget --tries=30 --continue http://central.maven.org/maven2/com/microsoft/azure/azure-storage/4.0.0/azure-storage-4.0.0.jar -O ${WF_MODULES}/com/microsoft/azure/storage/main/azure-storage-4.0.0.jar && \
    wget --tries=30 --continue https://jdbc.postgresql.org/download/postgresql-9.4.1212.jar -O ${WF_MODULES}/org/postgresql/main/postgresql-9.4.1212.jar

ADD azure-storage-module.xml ${WF_MODULES}/com/microsoft/azure/storage/main/module.xml
ADD jgroups-azure-module.xml ${WF_MODULES}/org/jgroups/azure/main/module.xml
ADD postgresql-module.xml ${WF_MODULES}/org/postgresql/main/module.xml

# Patching Infinispan :-(
ADD infinispan-core.jar_8.2.6.Final+ISPN-7619 ${WF_MODULES}/org/infinispan/main/infinispan-core.jar

RUN echo 'JAVA_OPTS="\
 -server \
 -Xms${INFINISPAN_MS_RAM:-8g} \
 -Xmx${INFINISPAN_MX_RAM:-8g} \
 -XX:+UseG1GC \
 -XX:+UseStringDeduplication \
 -XX:InitiatingHeapOccupancyPercent=70 \
 -XX:+HeapDumpOnOutOfMemoryError \
 -XX:HeapDumpPath=/opt/infinispan \
"' >> /opt/infinispan/infinispan-server/bin/standalone.conf
RUN mkdir -p /opt/infinispan/infinispan-server/standalone/log/
ADD infinispan.sh /opt/infinispan/
CMD ["/opt/infinispan/infinispan.sh"]

# Custom cache definitions and configuration
ADD clustered.xml ${WF_CONFIG}
