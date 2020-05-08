package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.utils.HttpRequestUtil;

import org.springframework.http.HttpMethod;

import java.util.Collections;

/**
 *  
 *  @author licong  
 *  @version 1.0 
 *  @create_time 2017/8/30
 */
public class DeleteFirmwareApi <T> extends EsightOpenIdCallable<T> {
    public DeleteFirmwareApi(Esight esight) {
        super(esight);
    }

    public DeleteFirmwareApi(Esight esight, OpenIdProvider openIdProvider) {
        super(esight, openIdProvider);
    }

    protected String uri() {
        return "/rest/openapi/server/firmware/basepackage/del";
    }

    protected HttpMethod httpMethod() {
        return HttpMethod.POST;
    }

    /**
     * 删除配置模板.
     * @param basepackageName 固件名称
     * @param returnType 返回参数类型
     * @return 指定返回参数类型
     */
    public T doCall(String basepackageName, Class<T> returnType) {
        String body = HttpRequestUtil.concatParamAndEncode(
                Collections.singletonMap("basepackageName", basepackageName));
        return super.call(body, null, returnType);
    }
}
