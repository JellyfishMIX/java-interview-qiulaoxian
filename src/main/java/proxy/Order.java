package proxy;

/**
 * @author JellyfishMIX
 * @date 2021/5/22 13:44
 */
public class Order implements IOrder {
    int state = 0;

    @Override
    public void pay() throws InterruptedException {
        Thread.sleep(50);
        state = 1;
    }

    @Override
    public void show() {
        System.out.println("order status: " + state);
    }
}
