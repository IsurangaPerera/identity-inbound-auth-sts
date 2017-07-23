package org.wso2.carbon.sts.resource.internal;

import org.apache.neethi.Policy;

public class DataHolder {
	private Policy policy;

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
}
