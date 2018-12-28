package com.huawei.vcenterpluginui.interceptor;

import com.huawei.vcenterpluginui.services.SessionService;
import com.huawei.vcenterpluginui.services.VCenterInfoService;
import com.huawei.vcenterpluginui.services.VmActionService;
import com.huawei.vcenterpluginui.utils.ThumbprintsUtils;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

public class  SessionHandlerInterceptor extends HandlerInterceptorAdapter {

	protected SessionService sessionService;

	@Autowired
	private VmActionService vmActionService;

	@Autowired
	private VCenterInfoService vCenterInfoService;

	private static final Log LOGGER = LogFactory.getLog(SessionHandlerInterceptor.class);

	private static boolean IS_VIEW_RESOLVER_LOADED = false;

	private static boolean IS_VCENTER_THUMBPRINT_LOADED = false;

	@Autowired
    public SessionHandlerInterceptor(@Qualifier("sessionService") SessionService sessionService) {
		this.sessionService = sessionService;
    }

    // Empty Interceptor to avoid compiler warnings in huawei-vcenter-plugin-ui's
    // bundle-context.xml
    // where the bean is declared
    public SessionHandlerInterceptor() {
    	sessionService = null;
    }

	@Autowired
	private ApplicationContext applicationContext;

	private void initViewResolver() {
		LOGGER.info("Setting view resolver is started");
		IS_VIEW_RESOLVER_LOADED = true;
		try {
			ContentNegotiatingViewResolver viewResolver = applicationContext
					.getBean(ContentNegotiatingViewResolver.class);
			if (viewResolver == null) {
				LOGGER.warn("No ContentNegotiatingViewResolver exists");
				return;
			}
			List<View> defaultViews = new ArrayList<>();

			View defaultView = null;
			try {
				defaultView = (View) Class
						.forName("org.springframework.web.servlet.view.json.MappingJacksonJsonView")
						.newInstance();
			} catch (Throwable throwable) {
				defaultView = (View) Class
						.forName("org.springframework.web.servlet.view.json.MappingJackson2JsonView")
						.newInstance();
			}
			defaultViews.add(defaultView);
			viewResolver.setDefaultViews(defaultViews);
			LOGGER.info("View resolver has been loaded");
		} catch (Throwable throwable) {
			LOGGER.warn(throwable.getMessage(), throwable);
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {
		if (!IS_VIEW_RESOLVER_LOADED) {
			initViewResolver();
		}

		// callback function doesn't validate session
		if ((request.getRequestURI().endsWith("/services/notification/systemKeepAlive")
				|| request.getRequestURI().endsWith("/services/notification"))
				&& StringUtils.hasLength(request.getHeader("openid"))) {
			return true;
		// triggered by uninstalling plugin
		} else if(request.getRequestURI().endsWith("/services/notification/unsubscribe")
				&& RequestMethod.POST.toString().equalsIgnoreCase(request.getMethod())) {
			if (!StringUtils.hasLength(request.getParameter("vcenterUsername")) || !StringUtils.hasLength(request.getParameter("vcenterPassword"))) {
				response.getWriter().write("{\"code\":\"-90003\",\"data\":null,\"description\":\"Auth failed\"}");
				return false;
			}
			return true;
		} else if (sessionService.getUserSession() == null) {
			response.getWriter().write("{\"code\":\"-90003\",\"data\":null,\"description\":\"Auth failed\"}");
			return false;
		} else {
			int endIndex = request.getRequestURL().length() - request.getPathInfo().length() + 1;
			String url = request.getRequestURL().substring(0, endIndex);
			LOGGER.info("current user:" + sessionService.getUserSession().userName + "   current domain:" + url);
		}

		// add vCenter thumbprints
		if (!IS_VCENTER_THUMBPRINT_LOADED) {
			try {
				UserSession userSession = sessionService.getUserSession();
				if (userSession != null) {
					ServerInfo[] serverInfos = userSession.serversInfo;
					Set<String> newThumbprints = new HashSet<>();
					for (ServerInfo serverInfo : serverInfos) {
						String serverThumbprint = serverInfo.thumbprint.replaceAll(":", "").toLowerCase();
						if (serverInfo != null && !ThumbprintsUtils.getRuntimeThumbprints()
								.contains(serverThumbprint)) {
							newThumbprints.add(serverThumbprint);
						}
					}
					if (!newThumbprints.isEmpty()) {
						LOGGER.info("Saving server thumbprints, count: " + newThumbprints.size());
						vCenterInfoService
								.saveThumbprints(newThumbprints.toArray(new String[newThumbprints.size()]));
					}
					IS_VCENTER_THUMBPRINT_LOADED = true;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to add vCenter thumbprints", e);
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
	}

	public SessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	public VmActionService getVmActionService() {
		return vmActionService;
	}

	public void setVmActionService(VmActionService vmActionService) {
		this.vmActionService = vmActionService;
	}
}