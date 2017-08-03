package org.wso2.carbon.sts.resource.internal;

import org.apache.cxf.message.Exchange;
import org.apache.neethi.Policy;

public class DataHolder {
	private Policy policy;
	private Exchange exchange;

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
}
