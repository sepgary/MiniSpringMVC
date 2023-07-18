package com.core.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {

    private final Map<String, Integer> paramMapping;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    // 用反射调用url对应的method方法 将request中请求的参数值自动注入到加了RequestParam注解的参数上
    public Object handle(HttpServletRequest req, HttpServletResponse resp, Handler handler)
            throws InvocationTargetException, IllegalAccessException {

        // 1. 获取参数类型数组，用于后面的参数类型转换
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        // 2. 定义一个参数集合
        Object[] paramValues = new Object[parameterTypes.length];

        // 3. 获取前端的请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            // 3.1 没有@RequestParam修饰，直接跳过
            if (!this.paramMapping.containsKey(param.getKey())) {
                continue;
            }
            // 3.2 将数组转换为逗号分隔的字符串，单个参数的话就是去掉两个[]
            String value = Arrays.toString(param.getValue()).replaceAll("[\\[\\]]", "").replaceAll(",\\s", ",");
            // 3.3 获取参数位置
            int paramIndex = this.paramMapping.get(param.getKey());
            // 3.4 进行类型转换，将字符串转换为Integer, int, String等等
            paramValues[paramIndex] = castStringValue(value, parameterTypes[paramIndex]);
        }

        // 4. 注入request和response
        if (parameterMap.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if (parameterMap.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        // 5. 利用反射调用方法
        return handler.getMethod().invoke(handler.getController(), paramValues);

    }

    private Object castStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.parseInt(value);
        } else {
            return null;
        }
    }
}
