package com.zq.utils.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelTest {
    public static void main(String[] args) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        FileInputStream inputStream = new FileInputStream("D:/test.txt");
        FileChannel channel = inputStream.getChannel();
        channel.read(readBuffer);
        FileOutputStream outputStream = new FileOutputStream("D:/test.txt");
        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
        writeBuffer.put("天下大事必做于细，天下难事必做于易".getBytes());
        writeBuffer.flip();
        FileChannel channel1 = outputStream.getChannel();
        while(writeBuffer.hasRemaining()){
            channel1.write(writeBuffer);
        }
        channel1.force(true);
    }
}
