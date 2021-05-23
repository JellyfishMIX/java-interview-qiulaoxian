package annotation;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

/**
 * @author JellyfishMIX
 * @date 2021/5/22 20:37
 */
public class ObjectFactory {
    public static <T> T newInstance(Class<T> tClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Annotation[] annotations = tClass.getAnnotations();
        List<IAspect> aspectList = new LinkedList<>();

        for (Annotation annotation : annotations) {
            if (annotation instanceof Aspect) {
                Class type = ((Aspect) annotation).type();
                IAspect aspect = (IAspect) type.getConstructor().newInstance();
                aspectList.add(aspect);
            }
        }

        T instance = tClass.getConstructor().newInstance();
        return (T) Proxy.newProxyInstance(
                tClass.getClassLoader(),
                tClass.getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        aspectList.forEach(aspect -> aspect.before());
                        Object result = method.invoke(instance);
                        aspectList.forEach(aspect -> aspect.after());
                        return result;
                    }
                }
        );
    }

    @Test
    public void test() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        IOrder order = ObjectFactory.newInstance(Order.class);
        order.pay();
        order.show();
    }
}
