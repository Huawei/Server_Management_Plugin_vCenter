package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class PutNotificationSystemKeepAliveApi<T> extends EsightOpenIdCallable<T> {

    public PutNotificationSystemKeepAliveApi(Esight esight) {
        super(esight);
    }

    public PutNotificationSystemKeepAliveApi(Esight esight, OpenIdProvider openIdProvider) {
        super(esight, openIdProvider);
    }

    @Override
    protected String uri() {
        return "/rest/openapi/notification/common/systemKeepAlive";
    }

    @Override
    protected HttpMethod httpMethod() {
        return HttpMethod.PUT;
    }

    /**
     * 订阅系统保活消息
     * @param systemID 必填，第三方系统标识
     * @param openID 必填，网管主动连接第三方系统的认证凭证，由第三方系统分配和利用此字符串认证
     * @param url 必填，网管以 POST 方式向该 URL发送通知消息
     * @param dataType 非必填，通知报文的 data 字段类型，默认值为JSON
     * @param desc 非必填，三方系统描述
     * @param returnType
     * @return
     */
    public T doCall(String systemID, String openID, String url, String dataType, String desc, Class<T> returnType) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("systemID", systemID);
        bodyMap.put("openID", openID);
        bodyMap.put("url", url);
        bodyMap.put("dataType", dataType == null ? "JSON" : dataType);
        bodyMap.put("desc", desc == null ? "" : desc);
        return super.call(bodyMap, returnType);
    }
}
