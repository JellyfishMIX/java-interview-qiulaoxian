package stream;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author JellyfishMIX
 * @date 2021/3/28 08:45
 */
public class BasicStreamExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BasicStreamExample basicStreamExample = new BasicStreamExample();
        // basicStreamExample.testMapFilter();
        // basicStreamExample.testMapFilterReduce();
        // basicStreamExample.testFlatMap();
        basicStreamExample.testParallel();
    }

    public void testMapFilter() {
        var stream = Stream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .map(x -> x + x)
                .map(x -> x + x + x)
                .map(x -> x + x + x)
                .map(Object::toString)
                // function reference operator
                .map(Integer::parseInt);
        // lambda expression

        Stream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .map(x -> x + x)
                .map(x -> x + x + x)
                .map(x -> x + x + x)
                .map(Object::toString)
                // function reference operator
                .map(Integer::parseInt)
                // forEach is a terminator operator
                .forEach(x -> {
                    System.out.println(x);
                });
    }

    public void testMapFilterReduce() {
        // Monad
        // Optional<Integer>
        var result = Stream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .filter(x -> x < 20)
                .reduce(Math::max);

        var result2 = IntStream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .filter(x -> x < 20)
                .reduce(Math::max);

        var result3 = IntStream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .filter(x -> x < 20)
                .reduce(Math::max)
                .orElse(0);

        // Integer
        var result4 = Stream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .filter(x -> x < 20)
                .reduce(0, Math::max);

        var result5 = Stream.of(1, 2, 3, 4, 5, 6)
                .map(x -> x * x)
                .filter(x -> x < 20)
                .reduce(0, Integer::max);

        // 检验 Optional 中的值是否存在
        if (result.isPresent()) {
            System.out.println(result.get());
        }

        System.out.println(result.orElseGet(() -> 0));
    }

    public void testMutation() {
        var stream = Stream.of(1, 3, 5, 9, 2, 3, 4).sorted();
        stream.forEach(System.out::println);
    }

    // 在一个函数内，作用于某个外部属性变量，此外部属性变量没有体现在输入输出上，则此函数有副作用，为非纯函数。
    int c = 0;

    int add(int a, int b) {
        // side effect
        c++;
        System.out.println("xxx");
        return a + b;
    }

    public void testFlatMap() {
        var set = Stream.of("My", "Mine")
                .flatMap(str -> str.chars().mapToObj(i -> (char)i))
                .collect(Collectors.toSet());
        System.out.println(set.stream().collect(Collectors.toList()));
    }

    public void testParallel() throws ExecutionException, InterruptedException {
        var r = new Random();
        // IntStream.range(0, 1_000_000): 随机取数字最大1_000_000次
        // r.nextInt(10_000_000): 每次在 0 - 10_000_000 随机取数字
        var list = IntStream.range(0, 1_000_000)
                .map(t -> r.nextInt(10_000_000))
                .boxed()
                .collect(Collectors.toList());

        System.out.println("availableProcessors: " + Runtime.getRuntime().availableProcessors());

        // 比较串行和并行
        var t0 = System.currentTimeMillis();
        System.out.println(list.stream().max((a, b) -> a - b));
        System.out.println("time: " + (System.currentTimeMillis() - t0));

        var t1 = System.currentTimeMillis();
        System.out.println(list.parallelStream().max((a, b) -> a - b));
        System.out.println("time: " + (System.currentTimeMillis() - t1));

        // 使用线程池实现 parallel
        var pool = new ForkJoinPool(2);
        var t2 = System.currentTimeMillis();
        var max = pool.submit(() -> list.parallelStream().max((a, b) -> a - b)).get();
        System.out.println(max);
        System.out.println("time: " + (System.currentTimeMillis() - t2));
    }
}
