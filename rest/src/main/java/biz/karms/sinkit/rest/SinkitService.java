package biz.karms.sinkit.rest;

import biz.karms.sinkit.ejb.cache.pojo.BlacklistedRecord;
import biz.karms.sinkit.ejb.CoreServiceEJB;
import biz.karms.sinkit.ejb.DNSApiEJB;
import biz.karms.sinkit.ejb.WebApiEJB;
import biz.karms.sinkit.ejb.dto.AllDNSSettingDTO;
import biz.karms.sinkit.ejb.dto.CustomerCustomListDTO;
import biz.karms.sinkit.ejb.dto.FeedSettingCreateDTO;
import biz.karms.sinkit.exception.ArchiveException;
import biz.karms.sinkit.exception.IoCValidationException;
import biz.karms.sinkit.ioc.IoCRecord;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michal Karm Babacek
 *         <p/>
 *         TODO: Validation and filtering :-)
 */
@SessionScoped
public class SinkitService implements Serializable {
    private static final long serialVersionUID = -9406069262471624L;
    public static final String ERR_MSG = "Error, please, check your input.";

    @EJB
    private WebApiEJB webapiEJB;

    @EJB
    private CoreServiceEJB coreService;

    @EJB
    private DNSApiEJB dnsApiEJB;

    @Inject
    private Logger log;

    String createHelloMessage(final String name) {
        return new GsonBuilder().create().toJson(webapiEJB.sayHello(name));
    }

    String getStats() {
        return new GsonBuilder().create().toJson(webapiEJB.getStats());
    }

    String putBlacklistedRecord(final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            BlacklistedRecord blacklistedRecord = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, BlacklistedRecord.class);
            blacklistedRecord = webapiEJB.putBlacklistedRecord(blacklistedRecord);
            if (blacklistedRecord != null) {
                return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(blacklistedRecord);
            } else {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "putBlacklistedRecord", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }

    String getBlacklistedRecord(final String key) {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.getBlacklistedRecord(key));
    }

    String getSinkHole(final String client, final String key) {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(dnsApiEJB.getSinkHole(client, key));
    }

    String getBlacklistedRecordKeys() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.getBlacklistedRecordKeys());
    }

    String deleteBlacklistedRecord(final String key) {
        String message = webapiEJB.deleteBlacklistedRecord(key);
        if (message == null) {
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(message);
    }

    String getRules(final String clientIPAddress) {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.getRules(clientIPAddress));
    }

    String getRuleKeys() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.getRuleKeys());
    }

    String deleteRule(final String cidrAddress) {
        String message = webapiEJB.deleteRule(cidrAddress);
        if (message == null) {
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(message);
    }

    String processIoCRecord(final String jsonIoCRecord) throws IoCValidationException, ArchiveException {

        IoCRecord ioc = new GsonBuilder().setDateFormat(IoCRecord.DATE_FORMAT).create().fromJson(jsonIoCRecord, IoCRecord.class);
        ioc = coreService.processIoCRecord(ioc);
        return new GsonBuilder().setDateFormat(IoCRecord.DATE_FORMAT).create().toJson(ioc);
    }

    String runCacheRebuilding() {

        String response;
        if (coreService.runCacheRebuilding()) {
            response = "Cache rebuilding started";
        } else {
            response = "Cache rebuilding already started";
        }
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(response);

    }

    String putDNSClientSettings(final Integer customerId, final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            HashMap<String, HashMap<String, String>> customerDNSSetting = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, new TypeToken<HashMap<String, HashMap<String, String>>>() {
            }.getType());
            if (customerDNSSetting == null) {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.putDNSClientSettings(customerId, customerDNSSetting));
        } catch (Exception e) {
            log.log(Level.SEVERE, "putDNSClientSettings", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }

    String postAllDNSClientSettings(final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            AllDNSSettingDTO[] allDNSSetting = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, AllDNSSettingDTO[].class);
            if (allDNSSetting == null) {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.postAllDNSClientSettings(allDNSSetting));
        } catch (Exception e) {
            log.log(Level.SEVERE, "postAllDNSClientSettings", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }

    String putCustomLists(final Integer customerId, final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            CustomerCustomListDTO[] customerCustomLists = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, CustomerCustomListDTO[].class);
            if (customerCustomLists == null) {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.putCustomLists(customerId, customerCustomLists));
        } catch (Exception e) {
            log.log(Level.SEVERE, "putCustomLists", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }

    String putFeedSettings(final String feedUid, final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            HashMap<Integer, HashMap<String, String>> feedSettings = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, new TypeToken<HashMap<Integer, HashMap<String, String>>>() {
            }.getType());
            if (feedSettings == null) {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.putFeedSettings(feedUid, feedSettings));
        } catch (Exception e) {
            log.log(Level.SEVERE, "putFeedSettings", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }

    String postCreateFeedSettings(final String json) {
        try {
            log.log(Level.FINE, "Received JSON " + json);
            FeedSettingCreateDTO feedSettingCreate = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, FeedSettingCreateDTO.class);
            if (feedSettingCreate == null) {
                return new GsonBuilder().create().toJson(ERR_MSG);
            }
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(webapiEJB.postCreateFeedSettings(feedSettingCreate));
        } catch (Exception e) {
            log.log(Level.SEVERE, "postCreateFeedSettings", e);
            return new GsonBuilder().create().toJson(ERR_MSG);
        }
    }
}
