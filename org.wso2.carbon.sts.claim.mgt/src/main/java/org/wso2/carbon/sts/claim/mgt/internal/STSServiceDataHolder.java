package org.wso2.carbon.sts.claim.mgt.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class STSServiceDataHolder {
    Logger logger = LoggerFactory.getLogger(STSServiceDataHolder.class.getName());

    private static STSServiceDataHolder instance = new STSServiceDataHolder();

    private STSServiceDataHolder() {

    }

    public static STSServiceDataHolder getInstance() {
        return instance;
    }
}
