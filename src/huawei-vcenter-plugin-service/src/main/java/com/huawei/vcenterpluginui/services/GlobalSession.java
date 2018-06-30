package com.huawei.vcenterpluginui.services;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Background task session object cache
 * Created by hyuan on 2018/6/8.
 */
public class GlobalSession implements HttpSession {

    private volatile ConcurrentMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();

    private int maxInactiveInterval;
    private String id = super.toString();
    private long creationTime = System.currentTimeMillis();

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return 0L;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        this.maxInactiveInterval = i;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return concurrentHashMap.get(s);
    }

    @Override
    public Object getValue(String s) {
        return concurrentHashMap.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Enumeration<String>() {
            private List<String> keyList = new ArrayList<>(concurrentHashMap.keySet());

            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return keyList.size() > index;
            }

            @Override
            public String nextElement() {
                return keyList.get(index++);
            }
        };
    }

    @Override
    public String[] getValueNames() {
        return concurrentHashMap.keySet().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String s, Object o) {
        concurrentHashMap.put(s, o);
    }

    @Override
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        concurrentHashMap.remove(s);
    }

    @Override
    public void removeValue(String s) {
        if (s == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : concurrentHashMap.entrySet()) {
            if (s.equals(entry.getValue())) {
                concurrentHashMap.remove(entry.getKey());
            }
        }
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }
}
