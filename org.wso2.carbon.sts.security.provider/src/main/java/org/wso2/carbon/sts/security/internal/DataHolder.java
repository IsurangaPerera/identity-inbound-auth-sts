package org.wso2.carbon.sts.security.internal;


public class DataHolder {
	private String scenario;
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
}