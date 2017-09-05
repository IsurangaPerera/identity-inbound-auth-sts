package org.wso2.carbon.sts.resource.internal;

import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.message.Exchange;
import org.apache.neethi.Policy;
import org.w3c.dom.Document;
import org.wso2.carbon.sts.provider.DefaultSecurityTokenServiceProvider;

public class DataHolder {
	private Policy policy;
	private Exchange exchange;
	private CallbackHandler callbackHandler;
	private DefaultSecurityTokenServiceProvider provider;

    private static DataHolder instance = new DataHolder();
    
    private String wsdl;
	private Document policyDocument;

    private DataHolder() {}

    public static DataHolder getInstance() {
        return instance;
    }
    
    public Policy getPolicy() {
    	return policy;
    }
    
    public void setPolicy(Policy policy) {
    	this.policy = policy;
    }

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}
	
	public Exchange getExchange() {
		return exchange;
	}

	public void setServiceProvider(DefaultSecurityTokenServiceProvider provider) {
		this.provider = provider;
	}
	
	public DefaultSecurityTokenServiceProvider getServiceProvider() {
		return provider;
	}

	public void setPasswordCallbackHandler(CallbackHandler callbackHandler) {
		this.callbackHandler = callbackHandler;
	}
	
	public CallbackHandler getPasswordCallbackHandler() {
		return this.callbackHandler;
	}

	public void addWSDL(String doc) {
		this.wsdl = doc;
	}
	
	public String getWSDL() {
		return this.wsdl;
	}

	public void setPolicyDocument(Document policyDocument) {
		this.policyDocument =  policyDocument;
	}
	
	public Document getPolicyDocument() {
		return this.policyDocument;
	}
}
