package org.wso2.carbon.sts.resource;

import javax.ws.rs.core.Context;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

public abstract class AbstractResource implements Microservice {

	public abstract void processRequest(@Context Request request,
			@Context Response response);
}
