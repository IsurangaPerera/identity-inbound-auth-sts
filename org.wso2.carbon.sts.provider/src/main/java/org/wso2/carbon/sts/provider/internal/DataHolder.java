package org.wso2.carbon.sts.provider.internal;

import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;

public class DataHolder {
	private static DataHolder dataHolder = new DataHolder();
	private STSPropertiesMBean staticPropertBean;
	
	private DataHolder() {}
	
	public static DataHolder getInstance() {
		return dataHolder;
	}
	
	public void setStaticPropertyBean(STSPropertiesMBean staticPropertyBean) {
		this.staticPropertBean = staticPropertyBean;
	}
	
	public STSPropertiesMBean getStaticPropertyBean() {
		return this.staticPropertBean;
	}
}