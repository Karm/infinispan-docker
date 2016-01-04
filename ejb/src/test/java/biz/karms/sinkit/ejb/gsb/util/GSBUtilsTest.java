package biz.karms.sinkit.ejb.gsb.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by tom on 12/6/15.
 *
 * @author Tomas Kozel
 */
public class GSBUtilsTest {

    // Testing data provided by google -> ensuring our hashing works the same way as theirs.
    @Test
    public void testHashing() throws Exception {

        String message = "abc";
        byte[] hashPrefix = Arrays.copyOf(GSBUtils.computeHash(message), 4);
        assertArrayEquals(new byte[] {(byte)0xba, (byte) 0x78, (byte) 0x16, (byte) 0xbf}, hashPrefix);

        String message2 = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
        byte[] hashPrefix2 = Arrays.copyOf(GSBUtils.computeHash(message2), 6);;
        assertArrayEquals(new byte[]{(byte) 0x24, (byte) 0x8d, (byte) 0x6a, (byte) 0x61, (byte) 0xd2, (byte) 0x06}, hashPrefix2);
    }

    // Testing data provided by google
    @Test
    public void testCanonicalize() throws Exception {
        assertEquals("http://host/%25", GSBUtils.canonicalizeUrl("HTTP://host/%25%32%35"));
        assertEquals("http://host/%25%25", GSBUtils.canonicalizeUrl("http://host/%25%32%35%25%32%35"));
        assertEquals("http://host/%25", GSBUtils.canonicalizeUrl("http://host/%2525252525252525"));
        assertEquals("http://host/asdf%25asd", GSBUtils.canonicalizeUrl("http://host/asdf%25%32%35asd"));
        assertEquals("http://host/%25%25%25asd%25%25", GSBUtils.canonicalizeUrl("http://host/%%%25%32%35asd%%"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("http://www.google.com/"));
        assertEquals("http://168.188.99.26/.secure/www.ebay.com/", GSBUtils.canonicalizeUrl("http://%31%36%38%2e%31%38%38%2e%39%39%2e%32%36/%2E%73%65%63%75%72%65/%77%77%77%2E%65%62%61%79%2E%63%6F%6D/"));
        assertEquals("http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/", GSBUtils.canonicalizeUrl("http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/"));
        assertEquals("http://host%23.com/~a!b@c%23d$e%25f^00&11*22(33)44_55+", GSBUtils.canonicalizeUrl("http://host%23.com/%257Ea%2521b%2540c%2523d%2524e%25f%255E00%252611%252A22%252833%252944_55%252B"));
        assertEquals("http://195.127.0.11/blah", GSBUtils.canonicalizeUrl("http://3279880203/blah"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("http://www.google.com/blah/.."));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("www.google.com/"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("www.google.com"));
        assertEquals("http://www.evil.com/blah", GSBUtils.canonicalizeUrl("http://www.evil.com/blah#frag"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("http://www.GOOgle.com/"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("http://www.google.com.../"));
        assertEquals("http://www.google.com/foobarbaz2", GSBUtils.canonicalizeUrl("http://www.google.com/foo\tbar\rbaz\n2"));
        assertEquals("http://www.google.com/q?", GSBUtils.canonicalizeUrl("http://www.google.com/q?"));
        assertEquals("http://www.google.com/q?r?", GSBUtils.canonicalizeUrl("http://www.google.com/q?r?"));
        assertEquals("http://www.google.com/q?r?s", GSBUtils.canonicalizeUrl("http://www.google.com/q?r?s"));
        assertEquals("http://evil.com/foo", GSBUtils.canonicalizeUrl("http://evil.com/foo#bar#baz"));
        assertEquals("http://evil.com/foo);", GSBUtils.canonicalizeUrl("http://evil.com/foo);"));
        assertEquals("http://evil.com/foo?bar);", GSBUtils.canonicalizeUrl("http://evil.com/foo?bar);"));
//        assertEquals("http://%01%80.com/", GSBUtils.canonicalizeUrl("http://\\x01\\x80.com/"));
        assertEquals("http://notrailingslash.com/", GSBUtils.canonicalizeUrl("http://notrailingslash.com"));
        assertEquals("http://www.gotaport.com:1234/", GSBUtils.canonicalizeUrl("http://www.gotaport.com:1234/"));
        assertEquals("http://www.gotadefualtport.com/", GSBUtils.canonicalizeUrl("http://www.gotadefualtport.com:80/"));
        assertEquals("http://www.google.com/", GSBUtils.canonicalizeUrl("  http://www.google.com/  "));
        assertEquals("http://%20leadingspace.com/", GSBUtils.canonicalizeUrl("http:// leadingspace.com/"));
        assertEquals("http://%20leadingspace.com/", GSBUtils.canonicalizeUrl("http://%20leadingspace.com/"));
        assertEquals("http://%20leadingspace.com/", GSBUtils.canonicalizeUrl("%20leadingspace.com/"));
        assertEquals("https://www.securesite.com/", GSBUtils.canonicalizeUrl("https://www.securesite.com/"));
        assertEquals("http://host.com/ab%23cd", GSBUtils.canonicalizeUrl("http://host.com/ab%23cd"));
        assertEquals("http://host.com/twoslashes?more//slashes", GSBUtils.canonicalizeUrl("http://host.com//twoslashes?more//slashes"));
        assertEquals("http://google.com/", GSBUtils.canonicalizeUrl("google.com"));
        assertEquals("http://1.2.3.4/", GSBUtils.canonicalizeUrl("1.2.3.4"));
        assertEquals("http://255.255.255.255/", GSBUtils.canonicalizeUrl("4294967295"));
        assertEquals("http://1/", GSBUtils.canonicalizeUrl("1"));
        assertEquals("https://1.0/?path%25", GSBUtils.canonicalizeUrl("https://256:443/?path%25"));

    }

    @Test
    public void testIntToIp() {
        assertEquals("24.86.207.156", GSBUtils.longToIPv4(408342428l));
        assertEquals("1", GSBUtils.longToIPv4(1));
        assertEquals("1.0.0", GSBUtils.longToIPv4(65536l));
        assertEquals("1.0", GSBUtils.longToIPv4(256l));
        assertEquals("255.255.255.255", GSBUtils.longToIPv4(4294967295l));
        assertEquals("0", GSBUtils.longToIPv4(0l));
    }
}
