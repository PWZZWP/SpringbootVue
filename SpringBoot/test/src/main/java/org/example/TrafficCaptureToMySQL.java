package org.example;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TrafficCaptureToMySQL {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/traffic_capture";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public static void main(String[] args) {
        try {
            // 获取网络设备
            PcapNetworkInterface nif = Pcaps.getDevByName("eth0"); // 根据实际情况修改网络设备名

            if (nif == null) {
                System.out.println("No device found.");
                return;
            }

            // 打开网络设备
            int snapshotLength = 65536; // 最大捕获字节数
            int readTimeout = 50; // 读取超时时间
            PcapHandle handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);

            // 开始捕获流量
            int maxPackets = 100; // 捕获的最大数据包数量
            handle.loop(maxPackets, new PacketListener());

            // 关闭网络设备
            handle.close();
        } catch (PcapNativeException | NotOpenException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class PacketListener implements PacketListener {
        @Override
        public void gotPacket(Packet packet) {
            if (packet.contains(IpV4Packet.class)) {
                IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                String sourceIp = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
                String destinationIp = ipV4Packet.getHeader().getDstAddr().getHostAddress();
                String protocol = ipV4Packet.getHeader().getProtocol().name();
                int packetLength = ipV4Packet.length();

                // 将流量信息插入到 MySQL 数据库中
                insertTrafficInfo(sourceIp, destinationIp, protocol, packetLength);
            }
        }
    }

    private static void insertTrafficInfo(String sourceIp, String destinationIp, String protocol, int packetLength) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO traffic_info (source_ip, destination_ip, protocol, packet_length) VALUES (?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, sourceIp);
                preparedStatement.setString(2, destinationIp);
                preparedStatement.setString(3, protocol);
                preparedStatement.setInt(4, packetLength);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
