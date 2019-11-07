package nextstep.di.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nextstep.annotation.Bean;
import nextstep.annotation.Configuration;
import nextstep.stereotype.Controller;
import nextstep.stereotype.Repository;
import nextstep.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    private BeanScanner beanScanner;
    private Set<Class<?>> preInstantiateBeans;
    private Map<Class<?>, Object> beans = Maps.newHashMap();

    public BeanFactory(Object... basePackages) {
        this.beanScanner = new BeanScanner(basePackages);
        this.preInstantiateBeans = beanScanner.getBeans(Controller.class, Service.class, Repository.class);
    }


    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() {
        for (Class<?> clazz : preInstantiateBeans) {
            beans.put(clazz, createBean(clazz));
        }
    }

    private Object createBean(Class<?> clazz) {
        Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(clazz);
        if (injectedConstructor == null) {
            return BeanUtils.instantiateClass(clazz);
        }
        Class[] constructorParameterTypes = injectedConstructor.getParameterTypes();
        Object[] constructorParameterInstance = Arrays.stream(constructorParameterTypes)
                .map(injected -> BeanFactoryUtils.findConcreteClass(injected, preInstantiateBeans))
                .map(this::createBean)
                .toArray();

        return BeanUtils.instantiateClass(injectedConstructor, constructorParameterInstance);
    }

    public Set<Class<?>> getControllers() {
        return beans.keySet().stream()
                .filter(key -> key.isAnnotationPresent(Controller.class))
                .collect(Collectors.toSet());
    }
}
