package org.wso2.carbon.sts.policy.mgt.internal;

public class DataHolder {
	private int policyScenario;

    private static DataHolder instance = new DataHolder();

    private DataHolder() {

    }

    public static DataHolder getInstance() {
        return instance;
    }
    
    public int getPolicyScenario() {
    	return policyScenario;
    }
    
    public void setPolicyScenario(int i) {
    	policyScenario = i;
    }
}
