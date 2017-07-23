package org.wso2.carbon.sts.resource;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Context;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

public abstract class AbstractResource implements Microservice {

	public abstract String processRequest(@Context Request request)
			throws UnsupportedEncodingException;
}
