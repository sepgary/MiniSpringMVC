# Mini Spring MVC

参考Spring MVC源码实现的一个简易版Spring MVC框架

# 1  项目结构

## 1.1 创建Mvaen工程

因为SpringMVC的底层原理是利用Servlet和Java反射机制，所以主要依赖文件为Servlet，maven配置文件如下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>MiniSpringMVC</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.dom4j</groupId>
            <artifactId>dom4j</artifactId>
            <version>2.1.1</version>
        </dependency>

        <dependency>
            <groupId>jaxen</groupId>
            <artifactId>jaxen</artifactId>
            <version>1.2.0</version>
        </dependency>

    </dependencies>

</project>
```

## 1.2 初始化项目结构

```
├─.idea
└─src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      ├─core			// springmvc核心组件
    │  │      │  ├─annotation		// 注解
    │  │      │  ├─context			// 容器
    │  │      │  ├─handler			// 处理器
    │  │      │  └─servlet			// 前端控制器
    │  │      └─tizz			// 业务包
    │  │          ├─controller		// 控制层
    │  │          └─service			// 业务层
    │  ├─resources			// 资源包
    │  └─webapp				// 视图
    │      └─WEB-INF			// servlet配置
    └─test				// 测试
        └─java
```

项目主要分为两个模块，一个是SpringMVC的核心代码core包，另一个是处理业务逻辑以及处理请求的tizz包

# 2  从@AutoWired开始

## 2.1 @AutoWired

编写两个类，一个是`UserController`，另一个是`UserServiceImpl`，在Spring项目当中，在Controller里面注入Service需要利用`@Autowired`。

该注解利用到类的字段上面，所以元注解Target利用FIELD属性；

动态创建对象：Spring框架在运行时根据配置信息动态创建Bean对象；依赖解析：`@Autowired`注解的解析需要通过反射机制来实现；生命周期管理：`@Autowired`注解通常与Spring容器的生命周期管理一起使用。

由于这几个原因，所以元注解Rentention使用RUNTIME属性；

其次需要指定注入的Bean，所以再加上一个value属性；

```java
package com.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AutoWired {

    String value() default "";
}
```

现在的`UserController`代码如下，注入UserService并调用userService的一个方法

```java
package com.tizz.controller;

import com.core.annotation.AutoWired;
import com.tizz.service.UserService;

public class UserController {

    @AutoWired(value = "userService")
    private UserService userService;

    public void findUser() {
        userService.findUser();
    }
}
```

显然这样是不足够的，还需要将Controller和Service也注入到SpringMVC中管理，最后还需要将`findUser()`方法供给客户端访问，所以还需要引入`@Service`和`@Controller`，以及暴露端口的`@RequestMapping`

## 2.2 其他注解

### 2.2.1 @Service

注解注释在类上面，所以Target元注解为TYPE

```java
package com.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Service {

    String value() default "";
}
```

### 2.2.2 @Controller

注解注释在类上面，所以Target元注解为TYPE

```java
package com.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Controller {

    String value() default "";
}
```

### 2.2.3 @RequestMapping

注解注释在方法上面，所以Target元注解为METHOD

```java
package com.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value() default "";
}
```

### 2.2.4 @RequestParam

注解注释在参数上面，所以Target元注解为PARAMETER

```java
package com.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.PARAMETER)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value() default "";

    boolean required() default true;
}
```



### 2.2.5 完善代码

UserController

```java
package com.tizz.controller;

import com.core.annotation.*;
import com.tizz.bean.User;
import com.tizz.service.UserService;

@Controller(value = "userController")
public class UserController {

    @AutoWired(value = "userService")
    private UserService userService;

    @RequestMapping(value = "/findUser")
    public String findUser() {
        return userService.findUser();
    }

    @RequestMapping(value = "/getJson")
    public User getJson(@RequestParam("name") String name) {
        return userService.getUserByName(name);
    }

    @RequestMapping(value = "/welcome")
    public String welcome() {
        return "forward:/welcome";
    }
}

```

UserServiceImpl

```java
package com.tizz.service.impl;

import com.core.annotation.Service;
import com.tizz.bean.User;
import com.tizz.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Override
    public String findUser() {
        System.out.println("success get findUser");
        return "=====> userService findUser";
    }

    @Override
    public User getUserByName(String name) {
        System.out.println("success get getUserByName");
        // 模拟数据库
        List<User> userList = new ArrayList<>();
        userList.add(new User(1, "zs", 1));
        userList.add(new User(2, "ls", 1));
        userList.add(new User(3, "ww", 2));
        userList.add(new User(4, "jack", 1));
        userList.add(new User(5, "tom", 2));

        for (User user : userList) {
            if (user.getName().equals(name)) {
                return user;
            }
        }

        return null;
    }
}
```

# 3 核心编码

## 3.1 准备前端控制器

1. 编写`mvc.xml`，用于管理整个SpringMVC的配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
<!--    作用于整个业务逻辑-->
    <component-scan base-package="com.tizz.*"/>
</beans>
```

2. 编写`web.xml`，用于管理整个Servlet的配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app>

    <display-name>Archetype Created Web Application</display-name>

    <servlet>
        <servlet-name>DispatcherServlet</servlet-name>
        <servlet-class>com.core.servlet.DispatcherServlet</servlet-class>
<!--        初始化加载-->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:springmvc.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

<!--    作用于整个Mapping路径-->
    <servlet-mapping>
        <servlet-name>DispatcherServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```

## 3.2 加载配置文件

在servlet的生命周期中，加载servlet并实例化过久就进行初始化，而初始化调用的函数是init()函数，在init()函数的流程如下

1. 获取配置文件参数
2. 将配置文件参数加载到IOC容器当中，并初始化IOC容器
3. 将请求的URL路径与容器中的方法一一对应

```java
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
```

## 3.3 容器初始化

`WebApplicationContext`的初始化与IOC容器的构建是Spring MVC的核心要素，具体流程如下

1. 获取mvc.xml的配置路径文件，包括Spring MVC的基本环境，包扫描路径；拦截器，过滤器，类型转换等

```java
/** IOC容器 */
private final Map<String, Object> iocMap = new ConcurrentHashMap<>();

/** 类缓存 */
private final List<String> clazzCache = new ArrayList<>();

public WebApplicationContext(String location) {
    SAXReader saxReader=new SAXReader();
    //根据user.xml文档生成Document对象
    try {
        Document document = saxReader.read(this.getClass().getClassLoader().getResource(location.substring(10)));
        Element element = (Element) document.selectSingleNode("/beans/component-scan");
        // 获取包扫描路径
        String[] packs = element.attributeValue("base-package").split(",");
        for (String pack : packs) {
            // 注册包名，将每个包名添加到缓存
            doRegister(pack);
        }
        // 实例化类
        doCreateBean();
        // 依赖注入
        populate();
    } catch (DocumentException e) {
        System.out.println("xml read error");
    }
}
```

2. 遍历扫描的所有包，将包名加载到缓存当中，主要是递归遍历文件夹，直到遇到单个文件为止，然后将其类名加入到集合当中

```java
// 把pack的符合条件的类全部扫描出来放到缓存中
private void doRegister(String pack) {
    // com.tizz.*、com.tizz.controller --> com/tizz/*、com/tizz/controller
    // 1. 获取包的文件路径
    URL url = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
    if (url == null) {
        System.out.println("load clazz to cache fail, reason not found url, packName " + pack);
        return;
    }
    // 2. 根据文件路径获取文件
    File dir = new File(url.getFile());
    // 3. 遍历文件下面的内容
    for (File file : Objects.requireNonNull(dir.listFiles())) {
        // 3.1 是文件夹，递归搜索
        if (file.isDirectory()) {
            doRegister(pack + "." + file.getName());
        } else {
            // 3.2 不是文件夹，加入到缓存当中
            clazzCache.add(pack + "." + file.getName().replace(".class", ""));
        }
    }
}
```

3. 实例化对象，加载到IOC容器当中，将带有 `@Serivce` 和 `@Controller` 的类实例化过后加载到IOC容器当中

```java
private void doCreateBean() {
    for (String clazzName : clazzCache) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            // 存在注解，实例化到IOC容器当中
            if (clazz.isAnnotationPresent(Controller.class)) {
                String value = clazz.getAnnotation(Controller.class).value();
                iocMap.put(value, clazz.newInstance());
                System.out.println(value + " init to iocMap");
            }
            if (clazz.isAnnotationPresent(Service.class)) {
                String value = clazz.getAnnotation(Service.class).value();
                iocMap.put(value, clazz.newInstance());
                System.out.println(value + " init to iocMap");
            }
        } catch (Exception e) {
            System.out.println("create bean error，beanName " + clazzName);
        }
    }
}
```

4. 进行依赖注入，对每个IOC容器内的对象进行依赖注入，即带有 `@AutoWired` 注解的属性，进行属性赋值

```java
private void populate() {
    if (iocMap.isEmpty()) {
        return;
    }
    for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
        // 1. 获取IOC注入的对象
        Object bean = entry.getValue();
        // 2. 获取该对象的所有属性字段
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(AutoWired.class)) {
                continue;
            }
            // 3. 根据@AutoWired值获取容器中对应的实例
            String value = field.getAnnotation(AutoWired.class).value();
            Object iocBean = iocMap.get(value);
            if (iocBean == null) {
                System.out.println("ioc get bean error, reason type " + value +  " is not in iocMap");
                continue;
            }
            // 4. 开放访问权限
            field.setAccessible(true);
            try {
                // 5. 利用反射注入属性
                field.set(bean, iocBean);
                System.out.println(field.getType().getName() +  " autowired success");
            } catch (IllegalAccessException e) {
                System.out.println("ioc in fail, type" + field.getType().getName());
            }
        }
    }
}
```

## 3.4 适配器初始化

1. 将请求的URl路径与每个方法一一对应

`Handler` 类

```java
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
```

适配器映射匹配

```java
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
```

2. 匹配方法内的函数，适配器匹配

`HandlerAdapter`  用于接受请求和响应，并适配参数调用对应的方法

```java
package com.core.handler;

import com.sun.deploy.net.HttpRequest;
import com.sun.deploy.net.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {

    private Map<String, Integer> paramMapping;

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
            // 3.2 将数组转换为逗号分隔的字符串，单个参数的话就是去掉[ ]
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
```

适配器参数匹配

```java
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
```

## 3.5 解析响应

该Spring MVC框架啊可以接受字符串、自定义类、转发、重定向的响应，并完成对应的操作

```java
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
```
