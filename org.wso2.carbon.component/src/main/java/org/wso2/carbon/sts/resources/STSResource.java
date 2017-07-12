package org.wso2.carbon.sts.resources;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

@Component(
        name = "org.wso2.carbon.sts.resources.STSResource",
        service = Microservice.class,
        immediate = true
)
@Path("/services")
public class STSResource extends AbstractResource {
	
	@Path("/wso2carbon-sts")
	@POST
	@Consumes(MediaType.TEXT_XML)
	public void processRequest(@Context Request request) throws UnsupportedEncodingException{

	}

}
