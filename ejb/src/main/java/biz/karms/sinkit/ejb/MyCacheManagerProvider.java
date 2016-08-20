package biz.karms.sinkit.ejb;

import biz.karms.sinkit.ejb.cache.annotations.SinkitCache;
import biz.karms.sinkit.ejb.cache.annotations.SinkitCacheName;
import biz.karms.sinkit.ejb.cache.pojo.BlacklistedRecord;
import biz.karms.sinkit.ejb.cache.pojo.CustomList;
import biz.karms.sinkit.ejb.cache.pojo.GSBRecord;
import biz.karms.sinkit.ejb.cache.pojo.Rule;
import biz.karms.sinkit.ejb.cache.pojo.WhitelistedRecord;
import biz.karms.sinkit.ejb.hasingleton.HATimerServiceActivator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michal Karm Babacek
 */
@ApplicationScoped
@ManagedBean
public class MyCacheManagerProvider implements Serializable {

    private static final long serialVersionUID = 45216839143257496L;

    private static final long ENTRY_LIFESPAN = TimeUnit.MINUTES.toMillis(3L);
    private static final long NEVER = -1L;
    private static final long WAKEUP_INTERVAL = TimeUnit.MINUTES.toMillis(30L);

    @Inject
    private Logger log;

    @Resource(lookup = "java:jboss/infinispan/container/sinkitcontainer")
    private EmbeddedCacheManager manager;

    private Configuration replicatedIndexed(final String cacheName, final int threadPool, final boolean thisIsDNSNode) {
        return new ConfigurationBuilder().jmxStatistics().disable().available(false)
                .clustering().cacheMode(CacheMode.REPL_SYNC)
                .stateTransfer().awaitInitialTransfer(true)
                .timeout(10, TimeUnit.MINUTES)//.async()
                .expiration()
                .lifespan(NEVER)
                .wakeUpInterval(WAKEUP_INTERVAL)
                .maxIdle(NEVER)
                .indexing().index(Index.LOCAL)
                .addProperty("hibernate.search.default.worker.execution", "async")
                .addProperty("hibernate.search.lucene_version", "LUCENE_CURRENT")
                .addProperty("default.directory_provider", "ram")
                .addProperty("default.indexmanager", "near-real-time")
                .eviction().strategy(EvictionStrategy.NONE)
                .transaction().transactionMode(TransactionMode.NON_TRANSACTIONAL)
                .lockingMode(LockingMode.OPTIMISTIC)
                .unsafe().unreliableReturnValues(true)
                /*.persistence()
                .passivation(false)
                .addStore(MongoDBStoreConfigurationBuilder.class)
                .connectionURI(System.getenv("SINKIT_MONGODB_CONNECTION_URI"))
                .collection(cacheName)
                .shared(true)
                .preload(true)
                .async()
                .threadPoolSize(threadPool)
                .enable()
                */
                .persistence()
                .addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                .fetchPersistentState(false)
                .ignoreModifications(thisIsDNSNode)
                .preload(true)
                .shared(true)
                .purgeOnStartup(false)
                .table()
                .dropOnExit(false)
                .createOnStart(true)
                .tableNamePrefix("ISPN_" + cacheName)
                .idColumnName("ID_COLUMN").idColumnType("VARCHAR(255)")
                .dataColumnName("DATA_COLUMN").dataColumnType("BYTEA")
                .timestampColumnName("TIMESTAMP_COLUMN").timestampColumnType("BIGINT")
                .connectionPool()
                .connectionUrl("jdbc:postgresql://" + System.getenv("SINKIT_POSTGRESQL_DB_HOST") + ":" + System.getenv("SINKIT_POSTGRESQL_DB_PORT") + "/" + System.getenv("SINKIT_POSTGRESQL_DB_NAME"))
                .driverClass("org.postgresql.Driver")
                .password(System.getenv("SINKIT_POSTGRESQL_PASS"))
                .username(System.getenv("SINKIT_POSTGRESQL_USER"))
                .async()
                .enabled(true)
                .threadPoolSize(threadPool)
                .build();
    }

    private Configuration replicatedNotIndexed(final String cacheName, final int threadPool, final boolean thisIsDNSNode) {
        return new ConfigurationBuilder().jmxStatistics().disable().available(false)
                .clustering().cacheMode(CacheMode.REPL_SYNC)
                        //.hash().numOwners(2)
                .stateTransfer().awaitInitialTransfer(true)
                .timeout(10, TimeUnit.MINUTES)//.async()
                .expiration()
                .lifespan(NEVER)
                .wakeUpInterval(WAKEUP_INTERVAL)
                .maxIdle(NEVER)
                .indexing().index(Index.NONE)
                .eviction().strategy(EvictionStrategy.NONE)
                .transaction().transactionMode(TransactionMode.NON_TRANSACTIONAL)
                .lockingMode(LockingMode.OPTIMISTIC)
                .unsafe().unreliableReturnValues(true)
                /*
                .persistence()
                .passivation(false)
                .addStore(MongoDBStoreConfigurationBuilder.class)
                .connectionURI(System.getenv("SINKIT_MONGODB_CONNECTION_URI"))
                .collection(cacheName)
                .fetchPersistentState(false)
                .ignoreModifications(false)
                .purgeOnStartup(false)
                .shared(true)
                .preload(true)
                .async()
                .threadPoolSize(threadPool)
                .enable()*/
                .persistence()
                .addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                .fetchPersistentState(false)
                .ignoreModifications(thisIsDNSNode)
                .preload(true)
                .shared(true)
                .purgeOnStartup(false)
                .table()
                .dropOnExit(false)
                .createOnStart(true)
                .tableNamePrefix("ISPN_" + cacheName)
                .idColumnName("ID_COLUMN").idColumnType("VARCHAR(255)")
                .dataColumnName("DATA_COLUMN").dataColumnType("BYTEA")
                .timestampColumnName("TIMESTAMP_COLUMN").timestampColumnType("BIGINT")
                .connectionPool()
                .connectionUrl("jdbc:postgresql://" + System.getenv("SINKIT_POSTGRESQL_DB_HOST") + ":" + System.getenv("SINKIT_POSTGRESQL_DB_PORT") + "/" + System.getenv("SINKIT_POSTGRESQL_DB_NAME"))
                .driverClass("org.postgresql.Driver")
                .password(System.getenv("SINKIT_POSTGRESQL_PASS"))
                .username(System.getenv("SINKIT_POSTGRESQL_USER"))
                .async()
                .enabled(true)
                .threadPoolSize(threadPool)
                .build();
                /*
                .persistence()
                .addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                .fetchPersistentState(false)
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
                .async()
                .enabled(true)
                .threadPoolSize(20)
                */
    }

    private Configuration localNotIndexed() {
        return new ConfigurationBuilder().jmxStatistics().disable().available(false)
                .clustering().cacheMode(CacheMode.LOCAL)
                .expiration()
                .lifespan(ENTRY_LIFESPAN)
                .transaction().transactionMode(TransactionMode.NON_TRANSACTIONAL)
                .build();
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("\n\n Constructing caches...\n\n");

        final boolean thisIsDNSNode = !manager.getAddress().toString().contains(HATimerServiceActivator.unwantedNameSuffix);

        /**
         * MANAGER settings and Persistence
         */
        manager.defineConfiguration(SinkitCacheName.RULES_CACHE.toString(), replicatedIndexed("infinispan_rules", 5, thisIsDNSNode));
        manager.defineConfiguration(SinkitCacheName.CUSTOM_LISTS_CACHE.toString(), replicatedIndexed("infinispan_custom_lists", 5, thisIsDNSNode));
        manager.defineConfiguration(SinkitCacheName.GSB_CACHE.toString(), replicatedNotIndexed("infinispan_gsb", 10, thisIsDNSNode));
        manager.defineConfiguration(SinkitCacheName.BLACKLIST_CACHE.toString(), replicatedNotIndexed("infinispan_blacklist", 10, thisIsDNSNode));
        if (thisIsDNSNode) {
            manager.defineConfiguration(SinkitCacheName.WHITELIST_CACHE.toString(), replicatedNotIndexed("infinispan_whitelist", 10, thisIsDNSNode));
        } else {
            log.log(Level.INFO, "This node's address " + manager.getAddress().toString() + " contains unwanted suffix " + HATimerServiceActivator.unwantedNameSuffix + ", so cache " + SinkitCacheName.WHITELIST_CACHE.toString() + " won't be created.");
        }

        /**
         * Start caches
         */
        manager.getCache(SinkitCacheName.BLACKLIST_CACHE.toString()).start();
        if (thisIsDNSNode) {
            manager.getCache(SinkitCacheName.WHITELIST_CACHE.toString()).start();
        }
        manager.getCache(SinkitCacheName.RULES_CACHE.toString()).start();
        manager.getCache(SinkitCacheName.CUSTOM_LISTS_CACHE.toString()).start();
        manager.getCache(SinkitCacheName.GSB_CACHE.toString()).start();

        /**
         * Local search result caches
         */
        manager.defineConfiguration(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE.toString(), localNotIndexed());
        manager.getCache(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE.toString()).start();
        manager.defineConfiguration(SinkitCacheName.RULES_LOCAL_CACHE.toString(), localNotIndexed());
        manager.getCache(SinkitCacheName.RULES_LOCAL_CACHE.toString()).start();
        log.log(Level.INFO, "Caches defined.");
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.BLACKLIST_CACHE)
    public Cache<String, BlacklistedRecord> getBlacklistCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getBlacklistCache called.");
        return manager.getCache(SinkitCacheName.BLACKLIST_CACHE.toString());
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.WHITELIST_CACHE)
    public Cache<String, WhitelistedRecord> getWhitelistCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getWhitelistCache called.");
        if (manager.cacheExists(SinkitCacheName.WHITELIST_CACHE.toString())) {
            return manager.getCache(SinkitCacheName.WHITELIST_CACHE.toString());
        }
        throw new IllegalStateException("getWhitelistCache called on a node that ain't supposed to be running it. DNS node?");
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.CUSTOM_LISTS_CACHE)
    public Cache<String, CustomList> getCustomListCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getCustomListCache called.");
        return manager.getCache(SinkitCacheName.CUSTOM_LISTS_CACHE.toString());
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.RULES_CACHE)
    public Cache<String, Rule> getRuleCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getRuleCache called.");
        return manager.getCache(SinkitCacheName.RULES_CACHE.toString());
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.GSB_CACHE)
    public Cache<String, GSBRecord> getGsbCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getGsbCache called.");
        return manager.getCache(SinkitCacheName.GSB_CACHE.toString());
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE)
    public Cache<String, List<CustomList>> getCustomListLocalCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getCustomListLocalCache called.");
        return manager.getCache(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE.toString());
    }

    @Produces
    @ApplicationScoped
    @SinkitCache(SinkitCacheName.RULES_LOCAL_CACHE)
    public Cache<String, List<Rule>> getRuleLocalCache() {
        if (manager == null) {
            throw new IllegalArgumentException("Manager must not be null.");
        }
        log.log(Level.INFO, "getRuleLocalCache called.");
        return manager.getCache(SinkitCacheName.RULES_LOCAL_CACHE.toString());
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        if (manager != null) {
            manager.getCache(SinkitCacheName.BLACKLIST_CACHE.toString()).stop();
            manager.getCache(SinkitCacheName.RULES_CACHE.toString()).stop();
            manager.getCache(SinkitCacheName.CUSTOM_LISTS_CACHE.toString()).stop();
            manager.getCache(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE.toString()).stop();
            manager.getCache(SinkitCacheName.RULES_LOCAL_CACHE.toString()).stop();
            manager.getCache(SinkitCacheName.GSB_CACHE.toString()).stop();
            manager.undefineConfiguration(SinkitCacheName.BLACKLIST_CACHE.toString());
            manager.undefineConfiguration(SinkitCacheName.RULES_CACHE.toString());
            manager.undefineConfiguration(SinkitCacheName.CUSTOM_LISTS_CACHE.toString());
            manager.undefineConfiguration(SinkitCacheName.CUSTOM_LISTS_LOCAL_CACHE.toString());
            manager.undefineConfiguration(SinkitCacheName.RULES_LOCAL_CACHE.toString());
            manager.undefineConfiguration(SinkitCacheName.GSB_CACHE.toString());

            if (manager.cacheExists(SinkitCacheName.WHITELIST_CACHE.toString())) {
                manager.getCache(SinkitCacheName.WHITELIST_CACHE.toString()).stop();
                manager.undefineConfiguration(SinkitCacheName.WHITELIST_CACHE.toString());
            }

            manager.stop();
            manager = null;
        }
    }
}

