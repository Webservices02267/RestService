package restService.Presentation;

import restService.Application.AccountService;
import restService.Infrastructure.MessageQueueFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


@Path("/customers")
public class CustomerResource  {

	AccountService accountService = new AccountService(new MessageQueueFactory().getMessageQueue());

    @GET
    @Path("/status")
    public Response get() {
        return Response.status(Response.Status.OK).entity(accountService.getStatus()).build();
    }
    

//	@POST
//	@Path("{customerId}/{numberOfTokens}")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response create(@PathParam("customerId") String customerId, @PathParam("numberOfTokens") Integer numberOfTokens) {
//		return Response.status(Response.Status.CREATED).entity(service.createTokens(numberOfTokens,customerId)).build();
//	}
//
//	@GET
//	@Path("{customerId}")
//	public Response get(@PathParam("customerId") String customerId) {
//		return Response.status(Response.Status.OK).entity(service.getTokens(customerId)).build();
//	}
//
//	@DELETE
//	@Path("{customerId}")
//	public Response delete(@PathParam("customerId") String customerId) {
//		return Response.status(Response.Status.OK).entity(service.deleteTokensForCustomer(customerId)).build();
//	}

	

}