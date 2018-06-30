package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.esight.api.rest.server.GetServerDeviceApi;
import com.huawei.esight.api.rest.server.GetServerDeviceDetailApi;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.CommonUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;

public class ServerApiServiceImpl extends ESightOpenApiService implements ServerApiService {

    private static final int START_PAGE_NO = 1; // 起始页码数，API限定的
    private static final int MAX_PAGE_SIZE = 100; // 单页最大数据，API限定的

	@Override
	public String queryServer(String ip, HttpSession session, String servertype, int pageNo, int pageSize) throws SQLException {
		ESight eSight = getESightByIp(ip);
		return queryServer(eSight, new SessionOpenIdProvider(eSight, session), servertype, pageNo, pageSize);
	}

    @Override
    public String queryServer(ESight eSight, DefaultOpenIdProvider openIdProvider, String servertype, int pageNo,
                              int pageSize) {
        return new GetServerDeviceApi<String>(eSight, openIdProvider)
                .doCall(servertype, String.valueOf(pageNo), String.valueOf(pageSize), String.class);
    }

	@Override
	public String queryDeviceDetail(String ip, String dn, HttpSession session) throws SQLException {
		ESight eSight = getESightByIp(ip);
		SessionOpenIdProvider openIdProvider = new SessionOpenIdProvider(eSight, session);
		return queryDeviceDetail(eSight, openIdProvider, dn);
	}

	private String queryDeviceDetail(ESight eSight, DefaultOpenIdProvider openIdProvider, String dn) {
		if (openIdProvider == null) {
			openIdProvider = new DefaultOpenIdProvider(eSight);
		}
		return new GetServerDeviceDetailApi<String>(eSight, openIdProvider).doCall(dn, String.class);
	}

    @Override
    public Map<String, List<ESightHAServer>> getESightAllServerList(ESight eSight, DefaultOpenIdProvider
            openIdProvider, String[] serverTypes) {
		Map<String, List<ESightHAServer>> map = new HashMap<>();
		for (String serverType : serverTypes) {
			List<Map<String, Object>> list = getESightServerListByServerType(eSight, openIdProvider, serverType);
			List<ESightHAServer> eSightHAServers = buildESightHAServerByESight(eSight, openIdProvider, list,
					eSight.getId(), serverType);
			map.put(getESightFailKey(eSight.getId(), serverType), eSightHAServers);
		}
		return map;
    }

	@Override
	public String getESightFailKey(int eSightId, String serverType) {
		return String.format("%s_%s", eSightId, serverType);
	}

	/**
	 * 获取eSight下指定服务器类型的服务器列表
	 */
	private List<Map<String, Object>> getESightServerListByServerType(ESight eSight, DefaultOpenIdProvider
			openIdProvider, String serverType) {
		return getESightServerListByServerType(eSight, openIdProvider, serverType, START_PAGE_NO, MAX_PAGE_SIZE);
	}

	/**
	 * 分页递归获取eSight下指定服务器类型的服务器列表
	 */
	private List<Map<String, Object>> getESightServerListByServerType(ESight eSight, DefaultOpenIdProvider
			openIdProvider, String serverType, int pageNo, int pageSize) {
		try {
			String response = this.queryServer(eSight, openIdProvider, serverType, pageNo, pageSize);
			Map<String, Object> dataMap = JsonUtil.readAsMap(response);
			int code = (int) dataMap.get("code");
			if (code == 0) {
				//noinspection unchecked
				List<Map<String, Object>> data = (List<Map<String, Object>>) dataMap.get("data");
				if (data == null || data.isEmpty()) {
					return data;
				}
				pageNo++;
				int totalPage = (int) dataMap.get("totalPage");
				if (pageNo <= totalPage) {
					List<Map<String, Object>> list = getESightServerListByServerType(eSight, openIdProvider,
							serverType, pageNo, pageSize);
					if (list == null) {
						LOGGER.warn(String.format("get eSight server list is null, eSight: %s, serverType: %s, " +
								"pageNo: %s, pageSize: %s", eSight.getHostIp(), serverType, pageNo, pageSize));
						return null;
					}
					data.addAll(list);
				}
				LOGGER.info(String.format(
						"get eSight server list is null, eSight: %s, serverType: %s, " + "pageNo: %s, pageSize: %s, " +
								"data: %s",
						eSight.getHostIp(), serverType, pageNo, pageSize, data.size()));
				return data;
			}
		} catch (Exception e) {
			LOGGER.error(String.format("get eSight server exception, eSight: %s, serverType: %s, " +
					"pageNo: %s, pageSize: %s", eSight.getHostIp(), serverType, pageNo, pageSize));
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private List<ESightHAServer> buildESightHAServerByESight(ESight eSight, DefaultOpenIdProvider openIdProvider,
			List<Map<String, Object>> list, int eSightHostId, String eSightServerType) {
		if (list == null) {
			return null;
		} else if (list.isEmpty()) {
			return Collections.emptyList();
		}

		List<ESightHAServer> result = new LinkedList<>();

        for (Map<String, Object> map : list) {
            //noinspection unchecked
            List<Map<String, Object>> childBlades = (List<Map<String, Object>>) map.get("childBlades");
            // 是否含有子服务器，高密、刀片和部分机架服务器需拿子服务器的UUID
            if (childBlades == null || childBlades.isEmpty()) {
                String uuid = (String) map.get("uuid");
                String status = String.valueOf(map.get("status"));
                String dn = (String) map.get("dn");
                addESightHAServer(eSightHostId, eSightServerType, dn, uuid, status, null, result);
            } else {
                String parentDN = (String) map.get("dn");
                for (Map<String, Object> childBlade : childBlades) {
                    String dn = (String) childBlade.get("dn");
                    try {
                        String responseStr = queryDeviceDetail(eSight, openIdProvider, dn);
                        //LOGGER.info("query device detail, dn: " + dn + " response: " + responseStr);
                        Map<String, Object> resMap = JsonUtil.readAsMap(responseStr);
                        if ("0".equals(String.valueOf(resMap.get("code")))) {
                            //noinspection unchecked
                            List<Map<String, Object>> data = (List<Map<String, Object>>) resMap.get("data");
                            if (data != null && !data.isEmpty()) {
                                Map<String, Object> tempMap = data.get(0);
                                String uuid = (String) tempMap.get("uuid");
                                String status = String.valueOf(tempMap.get("status"));
                                addESightHAServer(eSightHostId, eSightServerType, dn, uuid, status, parentDN, result);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
		return result;
	}

	private void addESightHAServer(int eSightHostId, String eSightServerType, String dn, String uuid, String status,
			String parentDN, List<ESightHAServer> result) {
		if (!StringUtils.hasLength(uuid)) {
			return;
		}
		ESightHAServer eSightHAServer = new ESightHAServer();
		eSightHAServer.setUuid(CommonUtils.formatUUID(uuid));
		eSightHAServer.seteSightServerType(eSightServerType);
		eSightHAServer.seteSightServerStatus(status);
		eSightHAServer.seteSightServerDN(dn);
		eSightHAServer.seteSightServerParentDN(parentDN);
		eSightHAServer.seteSightHostId(eSightHostId);
		eSightHAServer.setStatus(ESightHAServer.STATUS_NOT_SYNC);
		result.add(eSightHAServer);
	}
}
