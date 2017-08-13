package org.wso2.carbon.sts.resource.internal;

import org.apache.cxf.message.Exchange;
import org.apache.neethi.Policy;
import org.wso2.carbon.sts.provider2.provider.DefaultSecurityTokenServiceProvider;

public class DataHolder {
	private Policy policy;
	private Exchange exchange;
	private DefaultSecurityTokenServiceProvider provider;

    private static DataHolder instance = new DataHolder();

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
}
