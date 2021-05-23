package annotation;

/**
 * @author JellyfishMIX
 * @date 2021/5/22 13:47
 */
public class TimeUsageAspect implements IAspect {
    long startTime;
    long endTime;

    @Override
    public void before() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void after() {
        endTime = System.currentTimeMillis();
        System.out.format("time usage: %d %s\n", endTime - startTime, "ms");
    }
}
