package biz.karms.sinkit.ejb;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Michal Karm Babacek
 */
@Indexed
public class Rule implements Serializable {

    private static final long serialVersionUID = 9212324523047755691L;

    @Field
    private String startAddress;

    @Field
    private String endAddress;

    @Field
    private String cidrAddress;

    @Field
    private int customerId;

    /**
     * Feed UID : Mode as in
     */
    @IndexedEmbedded
    private HashMap<String, String> sources;

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getCidrAddress() {
        return cidrAddress;
    }

    public void setCidrAddress(String cidrAddress) {
        this.cidrAddress = cidrAddress;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public HashMap<String, String> getSources() {
        return sources;
    }

    public void setSources(HashMap<String, String> sources) {
        this.sources = sources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule)) return false;

        Rule rule = (Rule) o;

        if (customerId != rule.customerId) return false;
        if (startAddress != null ? !startAddress.equals(rule.startAddress) : rule.startAddress != null) return false;
        if (endAddress != null ? !endAddress.equals(rule.endAddress) : rule.endAddress != null) return false;
        if (!cidrAddress.equals(rule.cidrAddress)) return false;
        return sources.equals(rule.sources);

    }

    @Override
    public int hashCode() {
        int result = startAddress != null ? startAddress.hashCode() : 0;
        result = 31 * result + (endAddress != null ? endAddress.hashCode() : 0);
        result = 31 * result + cidrAddress.hashCode();
        result = 31 * result + customerId;
        result = 31 * result + sources.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "startAddress='" + startAddress + '\'' +
                ", endAddress='" + endAddress + '\'' +
                ", cidrAddress='" + cidrAddress + '\'' +
                ", customerId=" + customerId +
                ", sources=" + sources +
                '}';
    }
}
