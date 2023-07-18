package com.core.context;

import com.core.annotation.AutoWired;
import com.core.annotation.Controller;
import com.core.annotation.Service;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// springmvc容器
public class WebApplicationContext {

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

    public Object getBean(String name) {
        return iocMap.get(name);
    }

    public Map<String, Object> getAll() {
        return iocMap;
    }

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
}
