package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hyuan on 2018/4/8.
 */
public class DeleteNotificationSystemKeepAliveApi<T> extends EsightOpenIdCallable<T> {

    public DeleteNotificationSystemKeepAliveApi(Esight esight) {
        super(esight);
    }

    public DeleteNotificationSystemKeepAliveApi(Esight esight, OpenIdProvider openIdProvider) {
        super(esight, openIdProvider);
    }

    @Override
    protected String uri() {
        return "/rest/openapi/notification/common/systemKeepAlive";
    }

    @Override
    protected HttpMethod httpMethod() {
        return HttpMethod.DELETE;
    }

    /**
     * 退订系统保活消息
     * @param systemID 必填，第三方系统标识
     * @param desc 可填，三方系统描述
     * @param returnType
     * @return
     */
    public T doCall(String systemID, String desc, Class<T> returnType) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("systemID", systemID);
        bodyMap.put("desc", desc == null ? "" : desc);
        return super.call(null, bodyMap, returnType);
    }

}
