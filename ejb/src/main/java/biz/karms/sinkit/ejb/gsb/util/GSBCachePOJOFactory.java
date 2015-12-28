package biz.karms.sinkit.ejb.gsb.util;

import biz.karms.sinkit.ejb.cache.pojo.GSBRecord;
import biz.karms.sinkit.ejb.gsb.dto.FullHashLookupResponse;

import java.util.*;

/**
 * Created by tom on 12/20/15.
 *
 * @author Tomas Kozel
 */
public class GSBCachePOJOFactory {

    public static GSBRecord createFullHashes(FullHashLookupResponse response)  {
        GSBRecord gsbRecord = new GSBRecord();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, response.getValidSeconds());
        gsbRecord.setFullHashesExpireAt(c);
        HashMap<String, HashSet<String>> blackLists = new HashMap<>(response.getFullHashes().size());
        gsbRecord.setFullHashes(blackLists);
        for (Map.Entry<String, ArrayList<AbstractMap.SimpleEntry<byte[], byte[]>>> entry : response.getFullHashes().entrySet()) {
            HashSet<String> fullHashes = new HashSet<>(entry.getValue().size());
            blackLists.put(entry.getKey(), fullHashes);
            for (AbstractMap.SimpleEntry<byte[], byte[]> fullHash : entry.getValue()) {
                fullHashes.add(GSBUtils.hashToString(fullHash.getKey()));
            }
        }
        return gsbRecord;
    }
}
