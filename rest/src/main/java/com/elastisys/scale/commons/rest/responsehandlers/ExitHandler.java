package com.elastisys.scale.commons.rest.responsehandlers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.rest.server.JaxRsApplication;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * A REST response handler resource that handles requests to terminate a server.
 * It listens for {@code GET /exit} requests and, when received, exits the
 * server process, causing the server it's running in to terminate.
 * <p/>
 * 
 * @see JaxRsApplication
 * 
 * 
 * 
 */
@Path("/exit")
public class ExitHandler {
    static Logger log = LoggerFactory.getLogger(ExitHandler.class);

    public ExitHandler() {
        log.info(getClass().getSimpleName() + " created");
    }

    /**
     * Terminates the REST server process.
     * 
     * @return A {@code 200} {@link Response}.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exit() {
        log.info("GET /exit");
        try {
            return Response.ok().build();
        } finally {
            // shut down in a separate thread (after some delay) to give server
            // a chance to respond to client
            Executors.newSingleThreadExecutor().submit(() -> {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                System.exit(0);
            });
        }
    }
}
