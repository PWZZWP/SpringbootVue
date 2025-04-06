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

public class Demo11 {
    public static void main(String[] args) {
        InetAddress addr;
        String ip = "172.26.57.178";
        {
            try {

                addr = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        PcapNetworkInterface nif;

        {
            try {
                nif = Pcaps.getDevByAddress(addr);
            } catch (PcapNativeException e) {
                throw new RuntimeException(e);
            }
        }

        //4.open Pcap Handle
        int snaplen = 65536;
        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;
        int timeout = 10000;
        try (PcapHandle handle = nif.openLive(snaplen, mode, timeout)) {
            //记录开始时间
            long startTime = System.currentTimeMillis();
            //记录包的数量
            int packetCount = 0;
            while (true) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    System.out.println("报的信息："+packet);
                    IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                    if (ipV4Packet != null) {
                        System.out.println("源地址是：" + ipV4Packet.getHeader().getSrcAddr());
                        System.out.println("目的地址：" + ipV4Packet.getHeader().getDstAddr());
                        System.out.println("TCP层协议类型字段：" + ipV4Packet.getHeader().getProtocol());
                        packetCount++;
                    }
                    //获取当前时间
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > 2000) {
                        //输出2秒内接收到报的数量
                        System.out.println("2秒内的流量包数：" + packetCount);
                        //充值开始时间
                        startTime = currentTime;
                        //充值报的数量
                        packetCount = 0;
                    }
                } catch (EOFException | TimeoutException | NotOpenException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }
}
