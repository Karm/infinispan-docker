package biz.karms.sinkit.rest;

import biz.karms.sinkit.exception.IoCValidationException;
import com.google.gson.JsonSyntaxException;
import com.kanishka.virustotal.exception.InvalidArguentsException;
import com.kanishka.virustotal.exception.QuotaExceededException;
import com.kanishka.virustotal.exception.UnauthorizedAccessException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Michal Karm Babacek
 *         <p/>
 *         TODO: Validation :-)
 *         TODO: OAuth
 */
@Path("/")
public class SinkitREST {

    @Inject
    SinkitService sinkitService;

    @Inject
    private Logger log;

    @Inject
    StupidAuthenticator stupidAuthenticator;

    public static final String AUTH_HEADER_PARAM = "X-sinkit-token";
    public static final String AUTH_FAIL = "❤ AUTH ERROR ❤";

    @GET
    @Path("/hello/{name}")
    @Produces({"application/json;charset=UTF-8"})
    public String getHelloMessage(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("name") String name) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.createHelloMessage(name);
        } else {
            return AUTH_FAIL;
        }
    }

    @GET
    @Path("/stats")
    @Produces({"application/json;charset=UTF-8"})
    public String getStats(@HeaderParam(AUTH_HEADER_PARAM) String token) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.getStats();
        } else {
            return AUTH_FAIL;
        }
    }

    @GET
    @Path("/blacklist/dns/{client}/{key}")
    @Produces({"application/json;charset=UTF-8"})
    public String getSinkHole(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("client") String client, @PathParam("key") String key) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.getSinkHole(client, key);
        } else {
            return AUTH_FAIL;
        }
    }

    @GET
    @Path("/blacklist/record/{key}")
    @Produces({"application/json;charset=UTF-8"})
    public String getBlacklistedRecord(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("key") String key) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.getBlacklistedRecord(key);
        } else {
            return AUTH_FAIL;
        }
    }

    @GET
    @Path("/blacklist/records")
    @Produces({"application/json;charset=UTF-8"})
    public String getBlacklistedRecordKeys(@HeaderParam(AUTH_HEADER_PARAM) String token) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.getBlacklistedRecordKeys();
        } else {
            return AUTH_FAIL;
        }
    }

    @DELETE
    @Path("/blacklist/record/{key}")
    @Produces({"application/json;charset=UTF-8"})
    public String deleteBlacklistedRecord(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("key") String key) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.deleteBlacklistedRecord(key);
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/blacklist/record/")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String putBlacklistedRecord(@HeaderParam(AUTH_HEADER_PARAM) String token, @FormParam("record") String record) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.putBlacklistedRecord(record);
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/blacklist/ioc/")
    @Produces({"application/json;charset=UTF-8"})
    //@Consumes({"application/json;charset=UTF-8"})
    public Response putIoCRecord(@HeaderParam(AUTH_HEADER_PARAM) String token, String ioc) {

        if (!stupidAuthenticator.isAuthenticated(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(AUTH_FAIL).build();
        }

        try {
            String response = sinkitService.processIoCRecord(ioc);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (IoCValidationException | JsonSyntaxException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            log.severe("Processiong IoC went wrong: " + ex.getMessage());
            log.severe("IoC: " + ioc);
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/rebuildCache/")
    @Produces({"application/json;charset=UTF-8"})
    public Response rebuildCache(@HeaderParam(AUTH_HEADER_PARAM) String token) {

        if (!stupidAuthenticator.isAuthenticated(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(AUTH_FAIL).build();
        }

        String response = sinkitService.runCacheRebuilding();
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @GET
    @Path("/rules/{ip}")
    @Produces({"application/json;charset=UTF-8"})
    public String getRules(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("ip") String ip) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.getRules(ip);
        } else {
            return AUTH_FAIL;
        }
    }

    /**
     * Rattus - PORTAL --> CORE
     */
    @PUT
    @Path("/rules/customer/{customerId}")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String putDNSClientSettings(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("customerId") Integer customerId, String settings) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.putDNSClientSettings(customerId, settings);
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/rules/all")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String postAllDNSClientSettings(@HeaderParam(AUTH_HEADER_PARAM) String token, String rules) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.postAllDNSClientSettings(rules);
        } else {
            return AUTH_FAIL;
        }
    }


    @PUT
    @Path("/lists/{customerId}")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String putCustomLists(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("customerId") Integer customerId, String lists) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.putCustomLists(customerId, lists);
        } else {
            return AUTH_FAIL;
        }
    }

    @PUT
    @Path("/feed/{feedUid}")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String putFeedSettings(@HeaderParam(AUTH_HEADER_PARAM) String token, @PathParam("feedUid") String feedUid, String settings) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.putFeedSettings(feedUid, settings);
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/feed/create")
    @Produces({"application/json;charset=UTF-8"})
    @Consumes({"application/json;charset=UTF-8"})
    public String postCreateFeedSettings(@HeaderParam(AUTH_HEADER_PARAM) String token, String feed) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            return sinkitService.postCreateFeedSettings(feed);
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/log/record")
    public String logRecrod(@HeaderParam(AUTH_HEADER_PARAM) String token, String logRecord) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            try {
                sinkitService.addEventLogRecord(logRecord);
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "Not Ok";
            }
        } else {
            return AUTH_FAIL;
        }
    }

    @POST
    @Path("/total/enrich")
    public String enrich(@HeaderParam(AUTH_HEADER_PARAM) String token) {
        if (stupidAuthenticator.isAuthenticated(token)) {
            try {
                sinkitService.enrich();
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "Not Ok";
            }
        } else {
            return AUTH_FAIL;
        }
    }
}
