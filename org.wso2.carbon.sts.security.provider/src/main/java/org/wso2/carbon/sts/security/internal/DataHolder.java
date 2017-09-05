package org.wso2.carbon.sts.security.internal;

import org.w3c.dom.Document;

public class DataHolder {
	private String scenario;
	private Document policyDocument;
    private static DataHolder instance = new DataHolder();

    private DataHolder() {}

    public static DataHolder getInstance() {
        return instance;
    }
    
    public String getScenario() {
    	return scenario;
    }
    
    public void setScenario(String scenario) {
    	this.scenario = scenario;
    }

	public void addPolicyDocument(Document docPolicy) {
		this.policyDocument = docPolicy;
	}
	
	public Document getPolicyDocument(){
		return this.policyDocument;
	}
}