package nextstep.di.factory;

import com.google.common.collect.Sets;
import nextstep.annotation.Bean;
import nextstep.annotation.Configuration;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

public class BeanScanner {
    private static final Logger logger = LoggerFactory.getLogger(BeanScanner.class);

    private Reflections reflections;

    public BeanScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Set<Class<?>> getBeans(Class<? extends Annotation>... annotations) {
        Set<Class<?>> beans = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : annotations) {
            beans.addAll(getTypesAnnotatedWith(annotation));
        }
        logger.debug("Bean Scanner : {}", beans);
        return beans;
    }

    public Set<Class<?>> getConfigBeans() {
        Set<Class<?>> typesAnnotatedWith = getTypesAnnotatedWith(Configuration.class);
        Set<Class<?>> configBeans = Sets.newHashSet();
        for (Class<?> typesConfigurationAnnotated : typesAnnotatedWith) {
            Arrays.stream(typesConfigurationAnnotated.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Bean.class))
                    .forEach(method -> configBeans.add(method.getReturnType()));
        }
        return configBeans;
    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
