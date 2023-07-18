package com.core.servlet;

import com.core.annotation.Controller;
import com.core.annotation.RequestMapping;
import com.core.annotation.RequestParam;
import com.core.context.WebApplicationContext;
import com.core.handler.Handler;
import com.core.handler.HandlerAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherServlet extends HttpServlet {

    /** springmvc 容器 */
    private WebApplicationContext webApplicationContext;

    /** URL 和 handler相对应，handler持有method和其对应的controller */
    private final Map<String, Handler> handlerMapping = new HashMap<>();

    /** 适配器，存储每个handler函数的参数 */
    private final Map<Handler, HandlerAdapter> adapterMapping = new ConcurrentHashMap<>();

    @Override
    public void init() {
        System.out.println("start init=======>");
        // 1. 加载初始化参数
        String config = this.getServletConfig().getInitParameter("contextConfigLocation");
        // 2. 初始化IOC容器
        webApplicationContext = new WebApplicationContext(config);
        // 3. 将请求的URL与方法关联
        initHandlerMappings();
        // 4. 适配器匹配
        initHandlerAdapters();
        System.out.println("Spring MVC init over=======>");
    }

    private void initHandlerAdapters() {
        if (handlerMapping.isEmpty()) {
            return;
        }
        // 因为在handle方法中会进行判断是否存在key值，所以可以提前定义，用于整个循环
        Map<String, Integer> paramIndexMap = new HashMap<>();
        // 1。 遍历每个Handler
        for (Map.Entry<String, Handler> entry : handlerMapping.entrySet()) {
            // 2. 获取Handler对应方法的参数列表
            Class<?>[] parameterTypes = entry.getValue().getMethod().getParameterTypes();
            // 3. 缓存Request和Response
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == HttpServletRequest.class || parameterTypes[i] == HttpServletResponse.class) {
                    paramIndexMap.put(parameterTypes[i].getName(), i);
                }
            }
            Annotation[][] parameterAnnotations = entry.getValue().getMethod().getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation anno : parameterAnnotations[i]) {
                    if (anno instanceof RequestParam) {
                        // 4.2 加载到缓存
                        String paramName = ((RequestParam) anno).value();
                        if (!"".equals(paramName.trim())) {
                            paramIndexMap.put(paramName, i);
                            System.out.println("parameterType " + paramName + " success init in to HandlerAdapter");
                        }
                    }
                }
            }
            // 4. 缓存带有@RequsetParam的参数
            for (int i = 0; i < parameterTypes.length; i++) {
                // 4.1 跳过没有注解的
                if (!parameterTypes[i].isAnnotationPresent(RequestParam.class)) {
                    continue;
                }
                // 4.2 加载到缓存
                paramIndexMap.put(parameterTypes[i].getName(), i);
                System.out.println("parameterType " + parameterTypes[i].getName() + " success init in to HandlerAdapter");
            }
            adapterMapping.put(entry.getValue(), new HandlerAdapter(paramIndexMap));
        }
    }

    private void initHandlerMappings() {
        Map<String, Object> iocMap = webApplicationContext.getAll();
        if (iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Object bean = entry.getValue();
            // 1. 匹配Controller注解
            if (!bean.getClass().isAnnotationPresent(Controller.class)) {
                return;
            }
            for (Method method : bean.getClass().getMethods()) {
                // 2. 匹配RequestMapping注解
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                String url = method.getAnnotation(RequestMapping.class).value();
                // 3. 映射URL和Handler
                handlerMapping.put(url, new Handler(bean, method));
                System.out.println(url + " init in to handlerMapping");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doDispatcher(req, resp);
        } catch (InvocationTargetException | IllegalAccessException | ServletException e) {
            resp.getWriter().write("500 Exception，Msg：" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, InvocationTargetException, IllegalAccessException, ServletException {
        // 1. 根据请求路径获取映射适配器
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 Not Found");
            return;
        }
        HandlerAdapter handlerAdapter = adapterMapping.get(handler);

        // 2. 调用适配器的方法
        Object result = handlerAdapter.handle(req, resp, handler);
        if (result instanceof String) {
            String s = (String) result;
            if (s.startsWith("forward:/")) {
                req.getRequestDispatcher(s.substring(9) + ".jsp").forward(req, resp);
            } else if (s.startsWith("redirect:/")) {
                resp.sendRedirect(s.substring(10) + ".jsp");
            } else {
                resp.getWriter().println(result);
            }
        } else {
            resp.getWriter().println(result);
        }
    }

    // 获取删除公共请求URL的请求地址
    // https://localhost:8080/xxx -> /xxx
    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        return handlerMapping.get(url);
    }

}
