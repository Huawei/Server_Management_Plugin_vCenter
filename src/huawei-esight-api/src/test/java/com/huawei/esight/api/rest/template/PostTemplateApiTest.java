package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hyuan on 2017/6/30.
 */
public class PostTemplateApiTest {
    @Test
    public void testBodyCall() {
        String body = "{\n" +
                "    \"templateName\": \"test001\",\n" +
                "    \"templateType\": \"HBA\",\n" +
                "    \"templateDesc\": \"\",\n" +
                "    \"templateProp\": {\n" +
                "        \"adapterModel\": \"LPE12000\",\n" +
                "        \"slot\": \"1\",\n" +
                "        \"Port0\": {\n" +
                "            \"SANBoot\": \"Enabled\"\n" +
                "        },\n" +
                "        \"Port1\": null\n" +
                "    }\n" +
                "}";
        Map dataMap = new PostTemplateApi<Map>(EsightHelper.getEsight()).doCall(body, Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }

    @Test
    public void testMapCall() {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("templateName", "test002");
        dataMap.put("templateType", "HBA");
        dataMap.put("templateDesc", "");
        dataMap.put("templateProp", "{\n" +
                "        \"adapterModel\": \"LPE12000\",\n" +
                "        \"slot\": \"1\",\n" +
                "        \"Port0\": {\n" +
                "            \"SANBoot\": \"Enabled\"\n" +
                "        },\n" +
                "        \"Port1\": null\n" +
                "    }");
        Map responseMap = new PostTemplateApi<Map>(EsightHelper.getEsight()).doCall(dataMap, Map.class);
        Assert.assertEquals(0, responseMap.get("code"));
    }

    @Test
    public void testParamCall() {
        Map responseMap = new PostTemplateApi<Map>(EsightHelper.getEsight())
                .doCall("test003",
                        "HBA",
                        "",
                        "{\"adapterModel\": \"LPE12000\",\"slot\": \"1\",\"Port0\": {\"SANBoot\": \"Enabled\"},\"Port1\": null}",
                        Map.class);
        Assert.assertEquals(0, responseMap.get("code"));
    }
}
