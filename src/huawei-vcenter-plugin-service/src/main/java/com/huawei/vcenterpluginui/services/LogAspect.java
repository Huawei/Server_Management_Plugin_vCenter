package com.huawei.vcenterpluginui.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Created by hyuan on 2017/6/12.
 */
public class LogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogAspect.class);
    private static final String UNKNOWN = "UNKNOWN";

    public void logRequest(JoinPoint joinPoint) {
        try {
            String requestFrom = UNKNOWN;
            if (joinPoint.getArgs() != null) {
                for (Object param : joinPoint.getArgs()) {
                    if (param instanceof HttpServletRequest) {
                        requestFrom = ((HttpServletRequest) param).getRemoteAddr();
                        break;
                    }
                }
            }
            if ("NotificationController.unsubscribeAll(..)".equalsIgnoreCase(joinPoint.getSignature().toShortString())
                || "VCenterController.importCert(..)".equalsIgnoreCase(joinPoint.getSignature().toShortString())) {
                LOGGER.info("Request from " + requestFrom + ": " + joinPoint.getSignature().toShortString());
            } else {
                LOGGER.info("Request from " + requestFrom + ": " + joinPoint.getSignature().toShortString() + " "
                        + Arrays.toString(joinPoint.getArgs()).replaceAll("\"password\":\"[^&]*\"", "\"password\":\"******\"").replaceAll("Password\":\"[^&]*\"", "Password\":\"******\""));
            }
        } catch (Exception e) {
            LOGGER.warn( e.getMessage() );
        }
    }

}
