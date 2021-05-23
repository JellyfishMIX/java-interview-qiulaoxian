package annotation;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/**
 * @author JellyfishMIX
 * @date 2021/5/22 17:33
 */
public class ProxyExampleTest {
    @Test
    public void testProxy() throws InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IOrder order = IAspect.getProxy(Order.class, "proxy.TimeUsageAspect");
        order.pay();
        order.show();
    }
}
