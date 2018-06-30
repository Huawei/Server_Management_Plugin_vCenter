package com.huawei.vcenterpluginui.interceptor;

import com.huawei.vcenterpluginui.services.SessionService;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class SessionHandlerInterceptor extends HandlerInterceptorAdapter {

	protected SessionService sessionService;

	private static final Log LOGGER = LogFactory.getLog(SessionHandlerInterceptor.class);

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

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {
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
}