package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import org.springframework.http.HttpMethod;

import java.util.Collections;

/**
 *  
 *  @author licong  
 *  @version 1.0 
 *  @create_time 2017/8/31
 */
public class GetTemplateDetailApi<T> extends EsightOpenIdCallable<T> {
    public GetTemplateDetailApi(Esight esight) {
        super(esight);
    }

    public GetTemplateDetailApi(Esight esight, OpenIdProvider openIdProvider) {
        super(esight, openIdProvider);
    }

    protected String uri() {
        return "/rest/openapi/server/deploy/template/detail";
    }

    protected HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    /**
     * 查询模板详情.
     * @param templateName 模板名称
     * @return 指定返回参数类型
     */
    public T doCall(String templateName,  Class<T> returnType) {

        return call(null, Collections.singletonMap("templateName", templateName), returnType);
    }
}
