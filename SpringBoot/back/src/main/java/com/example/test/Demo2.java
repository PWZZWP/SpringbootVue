package com.example.test;


import org.pcap4j.core.*;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class Demo2 {
    //用于存储包的数量，使用volatile保证线程可见性。
    static volatile int packetCount = 0;
    //用于控制线程是否继续运行
    static volatile boolean running = true;

    public static void main(String[] args) throws PcapNativeException {
        InetAddress addr;
        String ip = "172.26.153.169";
            try {
                addr = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                //捕获异常并输出错误信息，避免直接抛出RuntimeException
                System.err.println("无法解析IP地址："+e.getMessage());
                return;
            }

        PcapNetworkInterface nif=null;
            try {
                //根据Ip地址获取网络接口
                nif = Pcaps.getDevByAddress(addr);
            } catch (PcapNativeException e) {
                //捕获异常并输出错误信息，避免直接抛出RuntimeException
               System.err.println("无法解析Ip地址："+e.getMessage());
               return;
            }
        if(nif==null){
            System.err.println("未找到匹配的网络接口");
            return;
        }
        System.out.println("网卡名字是！" + nif.getName());

        //4.open Pcap Handle.定义抓包参数
        int snaplen = 65536;
        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;
        int timeout = 10000;

        try (PcapHandle handle = nif.openLive(snaplen, mode, timeout)) {
            //创建并启动抓包线程
            Thread captureThread = new Thread(() ->{
                try{
                    while(running){
                        Packet packet = handle.getNextPacketEx();
                        System.out.println("收到一个包:"+packet);
                        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                        TcpPacket tcpPacket = packet.get(TcpPacket.class);
                        if(ipV4Packet!=null){
                            System.out.println("源地址是：" + ipV4Packet.getHeader().getSrcAddr());
                            System.out.println("目的地址：" + ipV4Packet.getHeader().getDstAddr());
                            System.out.println("TCP层协议类型字段：" + ipV4Packet.getHeader().getProtocol());

                            //增加报的数量
                            packetCount++;
                        }
                        if(tcpPacket!=null){
                            System.out.println("TCP源端口：" + tcpPacket.getHeader().getSrcPort());
                            System.out.println("TCP目的端口：" + tcpPacket.getHeader().getDstPort());
//                            System.out.println("应用程协议类型："+tcpPacket.getHeader());
                            System.out.println("TCP窗口大小："+tcpPacket.getHeader().getWindow());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            captureThread.start();

            //创建并启动统计线程
            Thread countThread = new Thread(()->{
                long startTime = System.currentTimeMillis();
                while(running){
                    long currentTime = System.currentTimeMillis();
                    if(currentTime-startTime>2000){
                        System.out.println("2秒内的流量包数："+packetCount);
                        startTime = currentTime;
                        if(packetCount>100) {
                            System.out.println("流量包数过多，可能存在攻击行为");}
                        packetCount = 0;
                    }
                }
            });
            countThread.start();

            //等待抓包线程结束
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                System.err.println("出现错误："+e.getMessage());
            }
            //停止统计线程
            running = false;
            try {
                countThread.join();
            } catch (InterruptedException e) {
                System.err.println("出现错误："+e.getMessage());
            }

        }
    }
}
