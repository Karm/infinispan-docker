package biz.karms.sinkit.ejb;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.persistence.jdbc.binary.JdbcBinaryStore;
import org.infinispan.persistence.jdbc.configuration.JdbcBinaryStoreConfigurationBuilder;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michal Karm Babacek
 */
@ApplicationScoped
public class MyCacheManagerProvider {

    private static final long ENTRY_LIFESPAN = 3 * 24 * 60 * 60 * 1000; //ms
    private static final int MAX_ENTRIES = 50000;
    @Inject
    private Logger log;

    private DefaultCacheManager manager;

    public DefaultCacheManager getCacheManager() {
        if (manager == null) {
            log.info("\n\n DefaultCacheManager does not exist - constructing a new one\n\n");

            GlobalConfiguration glob = new GlobalConfigurationBuilder().clusteredDefault() // Builds a default clustered
                    // configuration
                    .transport().addProperty(JGroupsTransport.CONFIGURATION_FILE, System.getenv("SINKIT_JGROUPS_NETWORKING")) // provide a specific JGroups configuration
                            //.transport().addProperty("configurationFile", "jgroups-udp.xml") // provide a specific JGroups configuration
                    .globalJmxStatistics().allowDuplicateDomains(true).enable() // This method enables the jmx statistics of
                            // the global configuration and allows for duplicate JMX domains
                    .build(); // Builds the GlobalConfiguration object
            Configuration loc = new ConfigurationBuilder().jmxStatistics().enable() // Enable JMX statistics
                    .clustering().cacheMode(CacheMode.DIST_ASYNC) // Set Cache mode to DISTRIBUTED with SYNCHRONOUS replication
                    .hash().numOwners(2) // Keeps two copies of each key/value pair
                    .expiration().lifespan(ENTRY_LIFESPAN) // Set expiration - cache entries expire after some time (given by
                            // the lifespan parameter) and are removed from the cache (cluster-wide).
                    .indexing().index(Index.ALL)
                    .eviction().strategy(EvictionStrategy.LRU)
                    .maxEntries(MAX_ENTRIES)
                            // .transaction().lockingMode(LockingMode.OPTIMISTIC).transactionManagerLookup(tml)
                    .transaction().transactionMode(TransactionMode.TRANSACTIONAL).lockingMode(LockingMode.OPTIMISTIC)
                            // TODO: Really? Autocommit? -- Yes, autocommit is true by default.
                    .transactionManagerLookup(new GenericTransactionManagerLookup()).autoCommit(true)

                    .persistence().addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                    //.persistence().addStore(JdbcBinaryStoreConfigurationBuilder.class)
                    .fetchPersistentState(true)
                    .ignoreModifications(false)
                    .purgeOnStartup(false)
                    .table()
                    .dropOnExit(false)
                    .createOnStart(true)
                    .tableNamePrefix("ISPN_STRING_TABLE")
                    .idColumnName("ID_COLUMN").idColumnType("VARCHAR(255)")
                    .dataColumnName("DATA_COLUMN").dataColumnType("BYTEA")
                    .timestampColumnName("TIMESTAMP_COLUMN").timestampColumnType("BIGINT")
                    .connectionPool()
                    .connectionUrl("jdbc:postgresql://" + System.getenv("SINKIT_POSTGRESQL_DB_HOST") + ":" + System.getenv("SINKIT_POSTGRESQL_DB_PORT") + "/" + System.getenv("SINKIT_POSTGRESQL_DB_NAME"))
                    .driverClass("org.postgresql.Driver")
                    .password(System.getenv("SINKIT_POSTGRESQL_PASS"))
                    .username(System.getenv("SINKIT_POSTGRESQL_USER"))
                    .build();
            manager = new DefaultCacheManager(glob, loc, true);
            manager.getCache("BLACKLIST_CACHE").start();
            manager.getCache("RULES_CACHE").start();
            log.log(Level.INFO, "I'm returning DefaultCacheManager instance " + manager + ".");
        }
        return manager;
    }

    @PreDestroy
    public void cleanUp() {
        manager.stop();
        manager = null;
    }

}
