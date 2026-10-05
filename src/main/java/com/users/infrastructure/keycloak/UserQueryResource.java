package com.users.infrastructure.keycloak;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * The user search of the Admin API with exactly the parameters this service uses. The stock
 * client has no overload that combines a search term, the enabled filter and paging.
 */
@Path("/admin/realms/{realm}/users")
@Produces(MediaType.APPLICATION_JSON)
interface UserQueryResource {

	@GET
	List<UserRepresentation> search(@PathParam("realm") String realm, @QueryParam("search") String search,
			@QueryParam("enabled") Boolean enabled, @QueryParam("first") Integer first,
			@QueryParam("max") Integer max, @QueryParam("briefRepresentation") Boolean briefRepresentation);

	@GET
	@Path("count")
	Integer count(@PathParam("realm") String realm, @QueryParam("search") String search,
			@QueryParam("enabled") Boolean enabled);
}
