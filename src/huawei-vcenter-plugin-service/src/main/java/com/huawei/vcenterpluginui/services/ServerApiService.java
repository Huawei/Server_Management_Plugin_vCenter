package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * It must be declared as osgi:service with the same name in
 * main/resources/META-INF/spring/bundle-context-osgi.xml
 */
public interface ServerApiService {

	/**
	 * 查询服务器列表
	 * @param ip
	 * @param session
	 * @param servertype
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	String queryServer(String ip, HttpSession session, String servertype, int pageNo, int pageSize) throws SQLException;

	/**
	 * 查询服务器列表
	 * @param eSight
	 * @param openIdProvider
	 * @param servertype
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	String queryServer(ESight eSight, DefaultOpenIdProvider openIdProvider, String servertype, int pageNo, int pageSize) throws SQLException;

	/**
	 * 查询服务器详细信息
	 * @param ip
	 * @param dn
	 * @param session
	 * @return
	 * @throws Exception
	 */
	String queryDeviceDetail(String ip, String dn, HttpSession session) throws SQLException;

    /**
     * 获取eSight指定服务器类型下的所有服务器
     * @param eSight eSight信息
     * @param openIdProvider 授权provider
     * @param serverTypes 服务器类型数组
     * @return map，其中key为"eSightId_serverType"
     */
    Map<String, List<ESightHAServer>> getESightAllServerList(ESight eSight, DefaultOpenIdProvider openIdProvider,
                                                             String[] serverTypes);

    /**
     * 获取getESightAllServerList方法返回值map中的key
     * @param eSightId eSight自增编号
     * @param serverType 服务器类型
     * @return map key
     */
    String getESightFailKey(int eSightId, String serverType);

}
