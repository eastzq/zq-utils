package com.zq.utils.multiexplorer.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * Bean复制复制类
 * 
 * @Description
 * @author wlinc
 * @date 2020年7月8日 下午7:19:23
 * @see
 * @since
 */
public class BeanCopyUtil {


    static Logger logger = LoggerFactory.getLogger(BeanCopyUtil.class);

    private static Map<Class<?>, MethodAccess> methodMap = new HashMap<Class<?>, MethodAccess>();
    private static Map<Class<?>, Map<String, Integer>> methodIndexOfGet = new HashMap<>();
    private static Map<Class<?>, Map<String, Integer>> methodIndexOfSet = new HashMap<>();
    private static Map<Class<?>, Map<String, String>> methodIndexOfType = new HashMap<>();

    /**
     * 浅copy类属性,根据属性名匹配，而不是类型+属性匹配，当类型不同且原属性值为null时，不变动目标类此属性值
     * 
     * @param desc 接收复制参数的类
     * @param orgi 原始类
     */
    public static void copyProperties(Object desc, Object orgi) {
        MethodAccess descMethodAccess = methodAccessFactory(desc);
        MethodAccess orgiMethodAccess = methodAccessFactory(orgi);
        Map<String, Integer> get = methodIndexOfGet.get(orgi.getClass());
        Map<String, Integer> set = methodIndexOfSet.get(desc.getClass());
        Map<String, String> oritypemap = methodIndexOfType.get(orgi.getClass());
        Map<String, String> desctypemap = methodIndexOfType.get(desc.getClass());

        List<String> sameField = null;
        if (get.size() < set.size()) {
            sameField = new ArrayList<>(get.keySet());
            sameField.retainAll(set.keySet());
        } else {
            sameField = new ArrayList<>(set.keySet());
            sameField.retainAll(get.keySet());
        }
        for (String field : sameField) {
            Integer setIndex = set.get(field);
            Integer getIndex = get.get(field);
            String oritype = oritypemap.get(field);
            String desctype = desctypemap.get(field);
            Object value = orgiMethodAccess.invoke(orgi, getIndex);
            try {
                if (!oritype.equalsIgnoreCase(desctype)) {
                    if (value == null) {
                        continue;
                    }
                    switch (desctype) {
                    case "java.lang.String":
                        descMethodAccess.invoke(desc, setIndex.intValue(), value.toString());
                        break;
                    case "java.lang.Integer":
                    case "int":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Integer.valueOf(value.toString()));
                        break;
                    case "java.lang.Long":
                    case "long":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Long.valueOf(value.toString()));
                        break;
                    case "java.lang.Float":
                    case "float":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Long.valueOf(value.toString()));
                        break;
                    case "java.lang.Boolean":
                    case "boolean":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Boolean.valueOf(value.toString()));
                        break;
                    case "java.lang.Double":
                    case "double":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Double.valueOf(value.toString()));
                        break;
                    case "java.lang.Byte":
                    case "byte":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Byte.valueOf(value.toString()));
                        break;
                    case "java.lang.Short":
                    case "short":
                        descMethodAccess.invoke(desc, setIndex.intValue(), Short.valueOf(value.toString()));
                        break;
                    default:
                        break;
                    }
                } else {
                    descMethodAccess.invoke(desc, setIndex.intValue(), value);
                }
            } catch (Exception e) {
                logger.error(String.format("%s:%s", field, e.getMessage()), e);
            }
        }
    }

    /**
     * 获取类SETTER方法的索引
     * 
     * @Description
     * @param clazz
     * @return Map<String,Integer>
     * @author wlinc
     * @date 2020年7月9日 下午6:15:37
     * @see
     */
    public static Map<String, Integer> getMethodIndexOfSet(Class<?> clazz) {
        return methodIndexOfSet.get(clazz);
    }

    /**
     * 获取类GETTER方法的索引
     * 
     * @Description
     * @param clazz
     * @return Map<String,Integer>
     * @author wlinc
     * @date 2020年7月9日 下午6:16:40
     * @see
     */
    public static Map<String, Integer> getMethodIndexOfGet(Class<?> clazz) {
        return methodIndexOfGet.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public static void copyMap(Object desc, Map<String, Object> orgi) {
        MethodAccess descMethodAccess = methodAccessFactory(desc);

        Map<String, Integer> set = methodIndexOfSet.get(desc.getClass());
        Map<String, String> desctypemap = methodIndexOfType.get(desc.getClass());
        for (Map.Entry<String, Object> entry : orgi.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            String key = captureName(entry.getKey());
            Integer setIndex = set.get(key);
            if (setIndex == null) {
                // Map的KEY在BEAN中找不到对应的setter方法或者成员变量
                continue;
            }
            String desctype = desctypemap.get(key);
            try {
                if (!entry.getValue().getClass().getName().equalsIgnoreCase(desctype)) {
                    Class<?>[] paramTypeClass = descMethodAccess.getParameterTypes()[setIndex];
                    if (entry.getValue() instanceof Map) {
                        try {
                            Object subDesc = paramTypeClass[0].newInstance();
                            copyMap(subDesc, (Map<String, Object>) entry.getValue());
                            descMethodAccess.invoke(desc, setIndex.intValue(), subDesc);
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error("Bean转换出现错误", e);
                        }
                    } else if (paramTypeClass[0] != null
                            && !paramTypeClass[0].isAssignableFrom(entry.getValue().getClass())) {
                        String val = entry.getValue().toString();
                        if (val.isEmpty()) {
                            continue;
                        }
                        switch (desctype) {
                        case "java.lang.String":
                            descMethodAccess.invoke(desc, setIndex.intValue(), val);
                            break;
                        case "java.math.BigDecimal":
                            descMethodAccess.invoke(desc, setIndex.intValue(), new BigDecimal(val));
                            break;
                        case "java.lang.Integer":
                        case "int":
                            if ("java.lang.Float".equals(entry.getValue().getClass().getName())) {
                                descMethodAccess.invoke(desc, setIndex.intValue(), Float.valueOf(val).intValue());
                            } else {
                                descMethodAccess.invoke(desc, setIndex.intValue(), Integer.valueOf(val));
                            }
                            break;
                        case "java.lang.Long":
                        case "long":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Long.valueOf(val));
                            break;
                        case "java.lang.Boolean":
                        case "boolean":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Boolean.valueOf(val));
                            break;
                        case "java.lang.Float":
                        case "float":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Long.valueOf(val));
                            break;
                        case "java.lang.Double":
                        case "double":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Double.valueOf(val));
                            break;
                        case "java.lang.Byte":
                        case "byte":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Byte.valueOf(val));
                            break;
                        case "java.lang.Short":
                        case "short":
                            descMethodAccess.invoke(desc, setIndex.intValue(), Short.valueOf(val));
                            break;
                        default:
                            descMethodAccess.invoke(desc, setIndex.intValue(), entry.getValue());
                            break;
                        }
                    } else {
                        descMethodAccess.invoke(desc, setIndex.intValue(), entry.getValue());
                    }
                } else {
                    descMethodAccess.invoke(desc, setIndex.intValue(), entry.getValue());
                }
            } catch (Exception t) {
                logger.error("转化问题：{}\n{}\n{}\n{}\n{}\n{}", descMethodAccess.getParameterTypes(), setIndex, desctype,
                        entry.getValue().getClass().getName(), orgi, key);
                throw t;
            }
        }
    }

    // double check
    public static MethodAccess methodAccessFactory(Object obj) {
        MethodAccess descMethodAccess = methodMap.get(obj.getClass());
        if (descMethodAccess == null) {
            synchronized (obj.getClass()) {
                descMethodAccess = methodMap.get(obj.getClass());
                if (descMethodAccess != null) {
                    return descMethodAccess;
                }
                Class<?> c = obj.getClass();
                MethodAccess methodAccess = MethodAccess.get(c);
                Set<Field> filedList = new HashSet<>();
                Class tmpClass = c;
//                while (tmpClass != Object.class) {
//                    Field[] fields = tmpClass.getDeclaredFields();
//                    filedList.addAll(Arrays.asList(fields));
//                    tmpClass = tmpClass.getSuperclass();
//                }
                Field[] fields = tmpClass.getDeclaredFields();
                filedList.addAll(Arrays.asList(fields));
                tmpClass = tmpClass.getSuperclass();
                Map<String, Integer> indexofget = new HashMap<>();
                Map<String, Integer> indexofset = new HashMap<>();
                Map<String, String> indexoftype = new HashMap<>();
                for (Field field : filedList) {
                    if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) { // 私有非静态
                        String fieldName = captureName(field.getName()); // 获取属性名称
                        int getIndex = methodAccess.getIndex("get" + fieldName); // 获取get方法的下标
                        int setIndex = methodAccess.getIndex("set" + fieldName); // 获取set方法的下标
                        indexofget.put(fieldName, getIndex);
                        indexofset.put(fieldName, setIndex);
                        indexoftype.put(fieldName, field.getType().getName());
                    }
                }
                methodIndexOfGet.put(c, indexofget);
                methodIndexOfSet.put(c, indexofset);
                methodIndexOfType.put(c, indexoftype);
                methodMap.put(c, methodAccess);
                return methodAccess;
            }
        }
        return descMethodAccess;
    }

    /**
     * 获取setter方法后的字段名
     * 
     * @Description
     * @param name
     * @return String
     * @author wlinc
     * @date 2020年7月8日 下午8:39:50
     * @see
     */
    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        if (cs[0] >= 97 && cs[0] <= 122) {
            cs[0] -= 32;
            return String.valueOf(cs);
        } else {
            return name;
        }
    }

    /**
     * bean 转 map TODO
     * 
     * @Description
     * @param <T>
     * @param bean
     * @return Map<String,Object>
     * @author yetuhao
     * @date 2020年10月12日 下午4:42:45
     * @see
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new HashMap<>();
        if (bean != null) {
            BeanMap beanMap = new BeanMap(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    // Bean --> Map 1: 利用Introspector和PropertyDescriptor 将Bean --> Map
    public static Map<String, Object> transBean2Map(Object obj) {

        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            logger.error("transBean2Map Error " + e);
        }

        return map;

    }
}
