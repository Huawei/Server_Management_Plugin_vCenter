package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import org.springframework.http.HttpMethod;
import java.util.HashMap;
import java.util.Map;

/**
 *  
 *  @author licong  
 *  @version 1.0 
 *  @create_time 2017/8/28
 */
public class GetFirmwareTaskDeviceDetailApi<T> extends EsightOpenIdCallable<T> {
    public GetFirmwareTaskDeviceDetailApi(Esight esight){
        super(esight);
    }

    public GetFirmwareTaskDeviceDetailApi(Esight esight, OpenIdProvider openIdProvider) {
        super(esight, openIdProvider);
    }

    protected String uri() {
        return "/rest/openapi/server/firmware/taskdevicedetail";
    }

    protected HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    public T doCall(String taskName,String dn, Class<T> returnType) {
        Map<String, String> urlParamMap = new HashMap<String, String>();
        urlParamMap.put("taskName", taskName);
        urlParamMap.put("dn", dn);
        return super.call(null, urlParamMap, returnType);

    }
}
