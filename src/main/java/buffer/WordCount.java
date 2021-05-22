package buffer;

import org.junit.Test;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * @author JellyfishMIX
 * @date 2021/5/18 21:22
 */
public class WordCount {
    /**
     * 线程池
     */
    final ForkJoinPool pool = ForkJoinPool.commonPool();

    @Test
    public void compareWithSingle() throws IOException {
        // 重点：使用 BufferedInputStream 读
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("word"));
        byte[] buffer = new byte[4 * 1024];
        int len;
        HashMap<String, Integer> totalMap = new HashMap<>();
        long startTime = System.currentTimeMillis();
        // 重点 inputStream.read() 每次读一个 byte，很浪费性能。inputStream.read() 读满缓冲数组才会结束读，性能比较好。
        while ((len = inputStream.read(buffer)) != -1) {
            byte[] bytes = Arrays.copyOfRange(buffer, 0, len);
            String str = new String(bytes);
            HashMap<String, Integer> singleMap = countByString(str);
            for (Map.Entry<String, Integer> entry : singleMap.entrySet()) {
                increaseKey(totalMap, entry.getKey(), entry.getValue());
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.format("consume time: %d %s\n", endTime - startTime, "ms");
        System.out.format("total size: %d\n", totalMap.size());
        System.out.format("ababb: %d", totalMap.get("ababb"));
    }

    private HashMap<String, Integer> countByString(String str) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(str);
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            this.increaseKey(hashMap, word, 1);
        }
        return hashMap;
    }

    private void increaseKey(HashMap<String, Integer> hashMap, String key, Integer n) {
        if (hashMap.containsKey(key)) {
            hashMap.put(key, hashMap.get(key) + n);
        } else {
            hashMap.put(key, n);
        }
    }

    class CountTask implements Callable<HashMap<String, Integer>> {
        private final long start;
        private final long end;
        private final String fileName;

        public CountTask(String fileName, long start, long end) {
            this.start = start;
            this.end = end;
            this.fileName = fileName;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public HashMap<String, Integer> call() throws Exception {
            HashMap<String, Integer> hashMap = new HashMap<>();
            FileChannel channel = new RandomAccessFile(this.fileName, "rw").getChannel();

            // [start, end] -> Memory
            // Device -> Kernel Space -> User Space(buffer) -> Thread
            MappedByteBuffer mBuffer = channel.map(FileChannel.MapMode.READ_ONLY, this.start, this.end - this.start);
            String str = StandardCharsets.UTF_8.decode(mBuffer).toString();
            HashMap<String, Integer> map = countByString(str);
            return map;
        }
    }

    List<Future<HashMap<String, Integer>>> taskList = new ArrayList<>();

    public void run(String fileName, long chunkSize) throws ExecutionException, InterruptedException {
        File file = new File(fileName);
        long fileSize = file.length();

        long position = 0;
        long startTime = System.currentTimeMillis();
        while (position < fileSize) {
            long next = Math.min(position + chunkSize, fileSize);
            CountTask task = new CountTask(fileName, position, next);
            position = next;
            Future<HashMap<String, Integer>> future = pool.submit(task);
            taskList.add(future);
        }

        HashMap<String, Integer> totalMap = new HashMap<>();
        for (Future<HashMap<String, Integer>> future : taskList) {
            HashMap<String, Integer> hashMap = future.get();
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                increaseKey(totalMap, entry.getKey(), entry.getValue());
            }
        }
        long endTime = System.currentTimeMillis();

        // 阿姆达定律
        // 120s -> 16core -> 120/16 =?
        // 因为 disk 的数据总线是一条，所以多个线程从硬件条件来说，无法同时读取

        System.out.format("consume time: %d %s\n", endTime - startTime, "ms");
        System.out.format("total size: %d\n", totalMap.size());
        System.out.format("ababb: %d", totalMap.get("ababb"));
    }

    @Test
    public void count() throws ExecutionException, InterruptedException {
        WordCount counter = new WordCount();
        System.out.println("processors: " + Runtime.getRuntime().availableProcessors());
        counter.run("word", 1024 * 1024);
    }
}
