package buffer;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author JellyfishMIX
 * @date 2021/5/11 10:49
 */
public class BufferExample {
    @Test
    public void gen() throws IOException {
        Random random = new Random();
        String fileName = "word";

        int bufferSize = 4 * 1024;
        // FileOutputStream fileOutput = new FileOutputStream(fileName);
        BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(fileName), bufferSize);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            for (int j = 0; j < 5; j++) {
                fileOutput.write(97 + random.nextInt(5));
            }
            fileOutput.write(32);
        }
        long endTime = System.currentTimeMillis();
        fileOutput.close();
        System.out.format("consume time: %d %s", endTime - startTime, "ms");
    }

    @Test
    public void readTest() throws IOException {
        String fileName = "word";
        FileInputStream fileInput = new FileInputStream(fileName);

        long startTime = System.currentTimeMillis();
        int b;
        while ((b = fileInput.read()) != -1 ) {

        }
        long endTime = System.currentTimeMillis();
        fileInput.close();
        System.out.println(String.format("consume time: %d", endTime - startTime, "ms"));
    }

    @Test
    public void readTestWithBuffer() throws IOException {
        String fileName = "word";
        int bufferSize = 4 * 1024;
        BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(fileName), bufferSize);

        long startTime = System.currentTimeMillis();
        int b;
        byte[] bytes = new byte[8 * 1024];
        while ((b = fileInput.read(bytes)) != -1 ) {

        }
        long endTime = System.currentTimeMillis();
        fileInput.close();
        System.out.println(String.format("consume time: %d", endTime - startTime, "ms"));
    }

    @Test
    public void readTestWithNIO() throws IOException {
        String fileName = "word";
        FileChannel channel = new FileInputStream(fileName).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);

        long startTime = System.currentTimeMillis();
        while (channel.read(buffer) != -1) {
            buffer.flip();
            // 读取数据
            System.out.println(new String(buffer.array()));
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("consume time: %d", endTime - startTime, "ms"));
    }

    @Test
    public void testAsyncRead() throws IOException, ExecutionException, InterruptedException {
        String fileName = "word";
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(Path.of(fileName), StandardOpenOption.READ);

        ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
        Integer readNum;
        do {
            byteBuffer.flip();
            byteBuffer.clear();
            Future<Integer> operation = channel.read(byteBuffer, 0);
            String chars = new String(byteBuffer.slice().array());
            readNum = operation.get();
        } while (readNum != -1);
    }

    @Test
    public void testChinese() {
        String raw = "长坂桥头杀气生，横枪立马眼圆睁。一声好似轰雷震，独退曹家百万兵。";
        Charset charset = StandardCharsets.UTF_8;
        byte[] bytes = charset.encode(raw).array();
        byte[] bytes2 = Arrays.copyOfRange(bytes, 0, 11);

        ByteBuffer bBuffer = ByteBuffer.allocate(12);
        CharBuffer cBuffer = CharBuffer.allocate(12);

        bBuffer.put(bytes2);
        bBuffer.flip();

        charset.newDecoder().decode(bBuffer, cBuffer, true);
        cBuffer.flip();

        char[] tmpChar = new char[cBuffer.length()];
        if (cBuffer.hasRemaining()) {
            cBuffer.get(tmpChar);
            System.out.println("here: " + new String(tmpChar));
        }
        System.out.format("limit-position: %d\n", bBuffer.limit() - bBuffer.position());
        // 把读了一半的 byte 再放回 bBuffer
        Arrays.copyOfRange(bBuffer.array(), bBuffer.position(), bBuffer.limit());
    }
}
