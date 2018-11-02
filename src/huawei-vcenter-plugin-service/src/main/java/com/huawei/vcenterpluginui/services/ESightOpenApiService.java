package com.huawei.vcenterpluginui.services;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.huawei.esight.exception.EsightException;
import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.entity.ESight;

/**
 * A class that support basic eSight API
 */
public class ESightOpenApiService {

    protected ESightDao eSightDao;

    protected static final int FAIL_CODE = -99999;
    
    private static final int CODE_ESIGHT_CONNECT_EXCEPTION = -80010;
    
    protected static final int RESULT_SUCCESS_CODE = 0;

    protected static final double RESULT_SUCCESS_CODE_DOUBLE = 0.0;
    
    protected static final int RESULT_ERROR_CODE = 10000;
    
    protected final Log LOGGER = LogFactory.getLog(this.getClass());

    protected static final String CODE_SUCCESS = String.valueOf(RESULT_SUCCESS_CODE);

    protected static final String CODE_SUCCESS_DOUBLE = String.valueOf(RESULT_SUCCESS_CODE_DOUBLE);


    /**
     * 根据IP获取esight信息
     *
     * @param ip
     * @return
     */
    protected ESight getESightByIp(String ip) throws SQLException {
        return eSightDao.getESightByIp(ip);
    }

    protected ESight getESightById(int id) throws SQLException {
      return eSightDao.getESightById(id);
    }

    public void seteSightDao(ESightDao eSightDao) {
        this.eSightDao = eSightDao;
    }
    
    protected Map<String,Object> getNoEsightMap(){
    	Map<String,Object> map = new HashMap();
    	map.put("code", FAIL_CODE);
    	map.put("description", "No eSight data in DB");
    	return map;
    }
    
    protected Map<String,Object> getEsightExceptionMap(EsightException e){
    	Map<String,Object> map = new HashMap<String,Object>();
		map.put("code", Integer.valueOf(e.getCode()));
		map.put("description", e.getMessage());
		return map;
    }
    
    protected Map<String,Object> getExceptionMap(){
    	Map<String,Object> map = new HashMap<String,Object>();
		map.put("code", CODE_ESIGHT_CONNECT_EXCEPTION);
		map.put("description", "服务器调用失败");
		return map;
    }

    public static boolean isSuccessResponse(Object code) {
        if (code != null) {
            if (code instanceof Integer) {
                return (Integer) code == Integer.parseInt(CODE_SUCCESS);
            } else if (code instanceof Double) {
                return (Double) code == Double.parseDouble(CODE_SUCCESS_DOUBLE);
            } else if (code instanceof String) {
                return CODE_SUCCESS.equals(code.toString()) || CODE_SUCCESS_DOUBLE.equals(code.toString());
            }
        }
        return false;
    }

}
