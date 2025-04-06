//import org.pcap4j.core.*;
//import org.pcap4j.packet.Packet;
//import org.pcap4j.packet.ipv4.IpV4Packet;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//// 假设 Result 类是自定义的响应结果类
//class Result {
//    private String code;
//    private String message;
//    private Object data;
//
//    public Result(String code, String message, Object data) {
//        this.code = code;
//        this.message = message;
//        this.data = data;
//    }
//
//    public static Result success() {
//        return new Result("200", "Success", null);
//    }
//
//    public static Result success(String message) {
//        return new Result("200", message, null);
//    }
//
//    public static Result error(String code, String message) {
//        return new Result(code, message, null);
//    }
//
//    // Getters and setters
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public Object getData() {
//        return data;
//    }
//
//    public void setData(Object data) {
//        this.data = data;
//    }
//}
//
//@RestController
//public class TrafficCaptureController {
//    private static final int STATISTIC_INTERVAL = 5000; // 假设的统计间隔
//    private final AtomicBoolean running = new AtomicBoolean(false);
//    private final List<Integer> trafficData = new ArrayList<>();
//    private int packetCount = 0;
//    private PcapDumper dumper; // 用于保存数据包的 dumper
//    private static final String PCAPNG_FILE_PATH = "traffic.pcapng";
//
//    @GetMapping("/traffic/startCapture")
//    public Result startCapture(@RequestParam String ip) {
//        if (running.get()) {
//            return Result.error("500", "Capture is already running!");
//        }
//        running.set(true);
//        trafficData.clear();
//        packetCount = 0;
//
//        new Thread(() -> {
//            PcapHandle handle = null;
//            try {
//                InetAddress addr = InetAddress.getByName(ip);
//                PcapNetworkInterface nif = Pcaps.getDevByAddress(addr);
//                if (nif == null) {
//                    System.err.println("未找到匹配的网卡！");
//                    running.set(false);
//                    return;
//                }
//                // 定义抓包参数
//                int snaplen = 65536;
//                PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;
//                int timeout = 10000;
//
//                handle = nif.openLive(snaplen, mode, timeout);
//                // 创建 PcapDumper 用于保存数据包到 pcapng 文件
//                dumper = handle.dumpOpen(PCAPNG_FILE_PATH);
//
//                long startTime = System.currentTimeMillis();
//                while (running.get()) {
//                    try {
//                        Packet packet = handle.getNextPacketEx();
//                        if (packet != null && packet.contains(IpV4Packet.class)) {
//                            packetCount++;
//                            // 将捕获的数据包写入 pcapng 文件
//                            dumper.dump(packet, handle.getTimestamp());
//                        }
//                    } catch (PcapNativeException | NotOpenException e) {
//                        System.err.println("捕获数据时发生异常！" + e.getMessage());
//                        break;
//                    }
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime - startTime >= STATISTIC_INTERVAL) {
//                        synchronized (trafficData) {
//                            trafficData.add(packetCount);
//                        }
//                        packetCount = 0;
//                        startTime = currentTime;
//                    }
//                }
//            } catch (Exception e) {
//                System.err.println("捕获数据时发生异常！" + e.getMessage());
//                e.printStackTrace();
//            } finally {
//                if (dumper != null) {
//                    try {
//                        // 关闭 dumper
//                        dumper.close();
//                    } catch (Exception e) {
//                        System.err.println("关闭 dumper 时发生异常！" + e.getMessage());
//                    }
//                }
//                if (handle != null && handle.isOpen()) {
//                    try {
//                        handle.close();
//                    } catch (Exception e) {
//                        System.err.println("关闭捕获时发生异常！" + e.getMessage());
//                    }
//                }
//                running.set(false);
//            }
//        }).start();
//        return Result.success();
//    }
//
//    @GetMapping("/traffic/stopCapture")
//    public Result stopCapture() {
//        running.set(false);
//        return Result.success("Capture stopped!");
//    }
//
//    @GetMapping("/traffic/trafficData")
//    public List<Integer> getTrafficData() {
//        return trafficData;
//    }
//
//    // 新增下载 pcapng 文件接口
//    @GetMapping("/traffic/downloadPcapng")
//    public ResponseEntity<byte[]> downloadPcapng() {
//        try {
//            File file = new File(PCAPNG_FILE_PATH);
//            FileInputStream fis = new FileInputStream(file);
//            byte[] data = new byte[(int) file.length()];
//            fis.read(data);
//            fis.close();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", "traffic.pcapng");
//
//            return new ResponseEntity<>(data, headers, HttpStatus.OK);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}