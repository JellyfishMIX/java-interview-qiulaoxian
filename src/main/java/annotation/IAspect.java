package annotation;

import trymonad.Try;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JellyfishMIX
 * @date 2021/5/22 13:42
 */
public interface IAspect {
    void before();
    void after();

    static <T> T getProxy(Class<T> tClass, String ... aspects) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Try<IAspect>> aspectInstanceList = Arrays.stream(aspects).map(aspectName -> Try.ofFailable(() -> {
            Class aspectClass = Class.forName(aspectName);
            return (IAspect) aspectClass.getConstructor().newInstance();
        }))
                .filter(aspect -> aspect.isSuccess())
                .collect(Collectors.toList());

        // 创建被代理对象的实例
        T instance = tClass.getConstructor().newInstance();
        return (T) Proxy.newProxyInstance(
                tClass.getClassLoader(),
                tClass.getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        for (Try<IAspect> aspect : aspectInstanceList) {
                            aspect.get().before();
                        }
                        Object result = method.invoke(instance);
                        for (Try<IAspect> aspect : aspectInstanceList) {
                            aspect.get().after();
                        }
                        return result;
                    }
                }
        );
    }
}
