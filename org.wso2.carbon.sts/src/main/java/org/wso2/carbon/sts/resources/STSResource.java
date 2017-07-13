package org.wso2.carbon.sts.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.binding.soap.SoapMessage;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

@Component(
        name = "org.wso2.carbon.sts.resources.STSResource",
        service = Microservice.class,
        immediate = true
)
@Api(value = "scim/v2/ServiceProviderConfig")
@SwaggerDefinition(
        info = @Info(
                title = "/SecurityTokenService Endpoint Swagger Definition", version = "1.0",
                description = "STS /SecurityTokenService endpoint",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"))
)
@Path("/services")
public class STSResource extends AbstractResource {
	
	@Path("/wso2carbon-sts")
	@POST
	@Consumes(MediaType.TEXT_XML)
	public void processRequest(@Context Request request) throws UnsupportedEncodingException{
		List<ByteBuffer> fullMessageBody = request.getFullMessageBody();
        ByteBuffer buffer = BufferUtil.merge(fullMessageBody);

        SoapMessage soap = null;
        try {
            soap = setUpMessage(buffer.array());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
