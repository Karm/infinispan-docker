package biz.karms.sinkit.ejb.util;

import biz.karms.sinkit.ejb.gsb.util.GSBUtils;

import java.net.URISyntaxException;

/**
 * Created by tom on 11/29/15.
 *
 * @author Tomas Kozel
 */
public class GSBUrl {

    private String url;
    private byte[] hash;
    private String hashString;

    public GSBUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL is null!");
        }

        try {
            this.url = GSBUtils.canonicalizeUrl(url);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("URL " + url + " has wrong format: " + ex.getMessage(), ex);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getHashString() {
        if (hashString == null) {
            computeHash();
        }
        return hashString;
    }

    public String getHashStringPrefix(int bytes) {
        if (hashString == null) {
            computeHash();
        }
        if (bytes * 2 > hashString.length()) return hashString;
        return hashString.substring(0, bytes * 2);
    }

    public byte[] getHash() {
        if (hash == null) {
            computeHash();
        }
        return hash;
    }

    public byte[] getHashPrefix(int length) {
        if (hash == null) {
            computeHash();
        }
        if (hash.length < length) {
            return hash;
        }
        byte[] hashPrefix = new byte[length];
        System.arraycopy( hash, 0, hashPrefix, 0, hashPrefix.length);
        return hashPrefix;
    }

    private void computeHash() {
        hash = GSBUtils.computeHash(url);
        hashString = GSBUtils.hashToString(hash);
    }
}
