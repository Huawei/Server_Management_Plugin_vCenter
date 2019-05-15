package com.huawei.vcenterpluginui.mvc;

import com.huawei.esight.exception.EsightException;
import com.huawei.esight.exception.NoOpenIdException;
import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.exception.NoEsightException;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.services.ESightOpenApiService;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;

/**
 * Created by hyuan on 2017/5/23.
 */
@Controller
public class BaseController {

  protected static final Log LOGGER = LogFactory.getLog(BaseController.class);

  protected static final String CODE_SUCCESS = "0";

  protected static final String CODE_ALL_FAILURE = "-100001";
  protected static final String CODE_NOTALL_FAILURE = "-100000";

  private static final String CODE_FAILURE = "-99999";

  private static final String CODE_DB_EXCEPTION = "-70001";
  private static final String CODE_ESIGHT_RETURN_PWD_EXCEPTION = "1";
  private static final String CODE_ESIGHT_CONNECT_EXCEPTION = "-80010";
  private static final String CODE_NO_ESIGHT_EXCEPTION = "-80011";
  private static final String CODE_CERT_EXCEPTION = "-80002";

  private static final String PREFIX = "-50";

  private static final String FIELD_CODE = "code";

  private static final String FIELD_DESCRIPTION = "description";

  private static final String FIELD_DATA = "data";

  private static final ResponseBodyBean FAILURE_BEAN = new ResponseBodyBean(CODE_FAILURE, null,
      null);

  private static final ResponseBodyBean SUCCESS_BEAN = new ResponseBodyBean(CODE_SUCCESS, null,
      null);

  @ExceptionHandler(NoEsightException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(NoEsightException exception) {
    LOGGER.debug("No eSight configuration!" + exception.getMessage());
    return generateError(generateCode(exception.getCode()), exception.getMessage(),
        Collections.emptyList());
  }


  @ExceptionHandler(RestClientException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(RestClientException exception,
      HttpServletRequest request) {
    LOGGER.error("Rest client Exception!" + exception.getMessage());
    Throwable rootCause = exception.getRootCause();
    if (rootCause instanceof CertificateException) {
      return generateError(request, CODE_CERT_EXCEPTION, exception.getMessage(), null);
    }
    return generateError(request, CODE_ESIGHT_CONNECT_EXCEPTION, exception.getMessage(), null);
  }

  @ExceptionHandler(VcenterException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(VcenterException exception,
      HttpServletRequest request) {
    LOGGER.error("vCenter plugin exception!" + exception.getMessage());
    return generateError(request, generateCode(exception.getCode()), exception.getMessage(), null);
  }

  @ExceptionHandler(NoOpenIdException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(NoOpenIdException exception,
      HttpServletRequest request) {
    LOGGER.error("Cannot get OpenId!" + exception.getMessage());
    if (exception.getCode() == null || exception.getCode().isEmpty()) {
      exception.setCode(CODE_ESIGHT_CONNECT_EXCEPTION);
    } else if (CODE_ESIGHT_RETURN_PWD_EXCEPTION.equals(exception.getCode())) {
      exception.setCode("-33" + exception.getCode());
    }
    return generateError(request, generateCode(exception.getCode()), exception.getMessage(), null);
  }

  @ExceptionHandler(EsightException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(EsightException exception,
      HttpServletRequest request) {
    LOGGER.error("eSight Exception!" + exception.getMessage());
    if (CODE_NO_ESIGHT_EXCEPTION.equals(exception.getCode())) {
      return generateError(request, exception.getCode(), exception.getMessage(), null);
    }
    return generateError(request, PREFIX + exception.getCode(), exception.getMessage(), null);
  }

  @ExceptionHandler(SQLException.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(SQLException exception,
      HttpServletRequest request) {
    LOGGER.error("DB Exception!" + exception.getMessage());
    Map<String, Object> errorMap = new HashMap<>();
    errorMap.put(FIELD_CODE, CODE_DB_EXCEPTION);
    errorMap.put(FIELD_DESCRIPTION, exception.getMessage());
    errorMap.put(FIELD_DATA, null);
    return errorMap;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.OK)
  protected Map<String, Object> handleException(Exception exception,
      HttpServletRequest request) {
    LOGGER.error("System Exception!" + exception.getMessage());
    return generateError(request, CODE_FAILURE, exception.getMessage(), null);
  }

  private Map<String, Object> generateError(HttpServletRequest request, String code, String message,
      Object data) {
    Map<String, Object> errorMap = new HashMap<>();
    errorMap.put(FIELD_CODE, code);
    errorMap.put(FIELD_DESCRIPTION, message);
    errorMap.put(FIELD_DATA, data);
    if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
      String ip = request.getParameter("esightIp");
      if (ip == null || "".equals(ip.trim())) {
        ip = request.getParameter("ip");
      }
      errorMap.put("ip", ip == null ? "" : ip);
    }
    return errorMap;
  }

  private Map<String, Object> generateError(String code, String message, Object data) {
    return generateError(null, code, message, data);
  }

  public String generateCode() {
    return generateCode(null);
  }

  public String generateCode(String code) {
    return (code == null || code.isEmpty()) ? CODE_FAILURE : code;
  }

  public boolean isSuccessResponse(Object code) {
    return ESightOpenApiService.isSuccessResponse(code);
  }

  protected ResponseBodyBean success() {
    return SUCCESS_BEAN;
  }

  protected ResponseBodyBean success(Object data) {
    return success(data, null);
  }

  protected ResponseBodyBean success(Object data, String description) {
    ResponseBodyBean bodyBean = null;
    bodyBean = new ResponseBodyBean(CODE_SUCCESS, null, null);
    bodyBean.setData(data);
    bodyBean.setDescription(description);
    return bodyBean;
  }

  protected ResponseBodyBean getResultByData(String data) throws IOException {
    return getResultByData(data, "");
  }

  protected ResponseBodyBean getResultByData(Map dataMap) {
    return getResultByData(dataMap, "");
  }

  protected ResponseBodyBean getResultByData(Map dataMap, String prefix) {
    if (!isSuccessResponse(dataMap.get(FIELD_CODE))) {
      if (dataMap.containsKey(FIELD_CODE)) {
        return failure(prefix + dataMap.get(FIELD_CODE).toString(),
            dataMap.get(FIELD_DESCRIPTION).toString(), dataMap);
      }
      return failure(CODE_FAILURE, "eSight result exception", dataMap);
    } else {
      return success(dataMap, (String) dataMap.get(FIELD_DESCRIPTION));
    }
  }

  protected ResponseBodyBean getResultByData(String data, String prefix) throws IOException {
    return getResultByData(JsonUtil.readAsMap(data), prefix);
  }

  protected ResponseBodyBean failure() {
    return FAILURE_BEAN;
  }

  protected ResponseBodyBean failure(String description) {
    return failure(CODE_FAILURE, description);
  }

  protected ResponseBodyBean failure(String code, String description) {
    ResponseBodyBean bodyBean = null;
    bodyBean = new ResponseBodyBean(CODE_FAILURE, null, null);
    bodyBean.setDescription(description);
    bodyBean.setCode(code);
    return bodyBean;
  }

  protected ResponseBodyBean failure(String code, String description, Object data) {
    ResponseBodyBean bodyBean = null;
    bodyBean = new ResponseBodyBean(CODE_FAILURE, null, null);
    bodyBean.setDescription(description);
    bodyBean.setCode(code);
    bodyBean.setData(data);

    return bodyBean;
  }

  protected ResponseBodyBean listData(List<Map<String, Object>> dataMapList) {
    return listData(dataMapList, "");
  }

  protected ResponseBodyBean listData(List<Map<String, Object>> dataMapList, String prefix) {

    Boolean success = false;
    Boolean failure = false;
    int errorCode = 0;
    String errorDescription = null;

    for (Map<String, Object> dataMap : dataMapList) {
      if (!isSuccessResponse(dataMap.get(FIELD_CODE))) {
        if (!failure) {
          errorCode = (int) dataMap.get(FIELD_CODE);
          errorDescription = (String) dataMap.get(FIELD_DESCRIPTION);
        }
        dataMap.put(FIELD_CODE, prefix + String.valueOf((int) dataMap.get(FIELD_CODE)));
        failure = true;
      } else {
        success = true;
      }
    }

    if (success && failure) {
      return failure(CODE_NOTALL_FAILURE, errorDescription, dataMapList);
    }
    if (failure) {
      return failure(CODE_ALL_FAILURE, errorDescription, dataMapList);
    }
//    else if (failure) {
//      return failure(prefix + String.valueOf(errorCode), errorDescription, dataMapList);
//    }

    return success(dataMapList);
  }

}
