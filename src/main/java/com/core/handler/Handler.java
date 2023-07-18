package com.core.handler;

import java.lang.reflect.Method;

public class Handler {

    // 控制器，用于反射调用指定的方法
    private Object controller;
    // 与URL匹配的方法
    private Method method;

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Handler(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }
}
