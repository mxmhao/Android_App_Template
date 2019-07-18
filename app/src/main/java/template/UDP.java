package template;

import android.content.Context;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

//发送者
class UDPSender {
    private String IPv4;
    private DatagramSocket socket;

    /**
     * //获取WiFi的ip
     WifiInfo wifiInfo = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
     int ipAddress = wifiInfo.getIpAddress();
     String IPv4 = ((ipAddress & 0xff) + "." + (ipAddress>>8 & 0xff) + "." +
     (ipAddress>>16 & 0xff) + "." + (ipAddress>>24 & 0xff));
     */

    public static UDPSender getInstance(Context context) throws SocketException {
//        DatagramSocket socket = new DatagramSocket(port);//这么写会导致端口一直占用
        DatagramSocket socket = new DatagramSocket(null);//这里设置为null，setReuseAddress才会有效
        socket.setReuseAddress(true);//可复用，地址或者端口号
        socket.setBroadcast(true);//允许发送广播
        //socket不用绑定任何IP或端口号也能发送UDP广播，因为系统会自动分配端口

        return new UDPSender(socket, "IPv4");
    }

    private UDPSender(DatagramSocket socket, String IPv4) {
        this.IPv4 = IPv4;
        this.socket = socket;
    }

    private int port = 1234;//对方接收的端口
    public void send() {
        try {
            //要发送的数据
            byte[] bytes = "hello".getBytes();
            //广播地址255.255.255.255
            byte[] broadcast = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
//            InetAddress.getByName(IPv4);//或者对方地址
            //组装数据包
            DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByAddress(broadcast), port);
            socket.send(dataPacket);//发送数据包

        } catch (IOException e) {
//            Log.e(TAG, "run: ", e);
        } finally {
            cancel();
        }
    }

    //发送完数据要释放资源
    public void cancel() {
        if (null != socket) {
            socket.close();
        }
        socket = null;
    }
}

class UDPReceiver extends Thread {

    private boolean cancelled = false;
    private DatagramSocket socket;
    private ReceiveListener listener;

    /**
     *
     * @param port 要监听的本地端口
     * @param listener 把收到的数据通过此接口传递出去
     * @return
     * @throws SocketException
     */
    public static UDPReceiver getInstance(int port, ReceiveListener listener) throws SocketException {
//        DatagramSocket socket = new DatagramSocket(port);//这么写会导致端口一直占用
        DatagramSocket socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));//监听指定的端口，若是不指定端口，系统会自动分配端口
        socket.setBroadcast(true);
        socket.setSoTimeout(15);//设置超时时间
        //若是不指定端口，此方法可获取系统自动分配的端口，然后通过某种方式告诉对方（如：广播包中携带此端口号）
//        socket.getLocalPort();
        return new UDPReceiver(socket, listener);
    }

    private UDPReceiver(DatagramSocket socket, ReceiveListener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    //取消，释放资源
    public void cancel() {
        if (cancelled) return;
        if (null != socket) socket.close();

        socket = null;
        listener = null;
        cancelled = true;
    }

    @Override
    public void run() {
        try {
            byte[] bytes;
            DatagramPacket dataPacket;
            while (!cancelled) {
                bytes = new byte[1024];
                dataPacket = new DatagramPacket(bytes, bytes.length);//存放数据的盒子
                socket.receive(dataPacket);//接收数据包
                new ParseThread(dataPacket, listener).start();//开线程去解析收到的数据，我这里简单粗暴了点
            }
        } catch (SocketTimeoutException e) {
            //超时不用管
        } catch (IOException e) {
//            Log.e(TAG, "run: ", e);
        } finally {
            cancel();
        }
    }

    //用线程去解析数据
    static class ParseThread extends Thread {
        private DatagramPacket dataPacket;
        private ReceiveListener listener;

        public ParseThread(DatagramPacket dataPacket, ReceiveListener listener) {
            this.dataPacket = dataPacket;
            this.listener = listener;
        }

        @Override
        public void run() {
            //把收到的数据转成字符串
            String message = new String(dataPacket.getData(), dataPacket.getOffset(), dataPacket.getLength());
            //解析message成你要的数据
            //把数据传递出去
            listener.receive(message);

            dataPacket = null;
            listener = null;
        }
    }

    public interface ReceiveListener {
        void receive(String message);
    }
}

