package org.wso2.carbon.sts.resource.soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Envelope", namespace="http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class Envelope {
	
	@XmlElement(name="Header", namespace="http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;

    @XmlElement(name="Body", namespace="http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;
    
    public void setHeader(Header header) {
    	this.header = header;
    }
    
    public Header getHeader() {
    	return header;
    }
    
    public void setBody(Body body) {
    	this.body = body;
    }
    
    public Body getBody() {
    	return body;
    }

}