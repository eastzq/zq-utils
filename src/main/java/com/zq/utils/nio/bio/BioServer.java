package com.zq.utils.nio.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 * 阻塞模式 IO
 * 我们已经介绍过使用 Java NIO 包组成一个简单的客户端-服务端网络通讯所需要的 ServerSocketChannel、SocketChannel 和 Buffer，
 * 我们这里整合一下它们，给出一个完整的可运行的例子：
 * 首先，
 * 每次来一个连接都开一个新的线程这肯定是不合适的。当活跃连接数在几十几百的时候当然是可以这样做的，但如果活跃连接数是几万几十万的时候，这么多线程明显就不行了。
 * 每个线程都需要一部分内存，内存会被迅速消耗，同时，线程切换的开销非常大。
 * 其次，
 * 阻塞操作在这里也是一个问题。首先，accept() 是一个阻塞操作，当 accept() 返回的时候，代表有一个连接可以使用了，我们这里是马上就新建线程来处理这个 SocketChannel 了，
 * 但是，但是这里不代表对方就将数据传输过来了。所以，SocketChannel#read 方法将阻塞，等待数据，明显这个等待是不值得的。
 * 同理，write 方法也需要等待通道可写才能执行写入操作，这边的阻塞等待也是不值得的。
 */
public class BioServer {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 监听 8080 端口进来的 TCP 链接
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        while (true) {
            // 这里会阻塞，直到有一个请求的连接进来
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 开启一个新的线程来处理这个请求，然后在 while 循环中继续监听 8080 端口
            SocketHandler handler = new SocketHandler(socketChannel);
            new Thread(handler).start();
        }
    }

}
