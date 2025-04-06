package com.example.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.TrafficEntity;
import com.example.service.TrafficService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.namednumber.TcpPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/traffic")
public class TrafficController {
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final List<Integer> trafficData = new ArrayList<>();
    //用于控制线程是否继续运行
    private int packetCount = 0;
    private static final long STATISTIC_INTERVAL = 2000;

    private PcapDumper dumper; // 新增：用于保存数据包的 dumper


    private static final String PCAPNG_FILE_PATH = "traffic.pcapng";
    // 日志记录器
    private static final Logger logger = Logger.getLogger(TrafficController.class.getName());
    @Resource
    TrafficService trafficService;

    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<TrafficEntity> list){
        //RequestBody 注解用于获取前端请求体中的数组
        trafficService.deleteBatch(list);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){//PathVariable 注解用于获取前端路径参数
        trafficService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody TrafficEntity trafficEntity){
        trafficService.update(trafficEntity);
        return Result.success();
    }

    @PostMapping("/add")
    public Result add(@RequestBody TrafficEntity trafficEntity){
        trafficService.add(trafficEntity);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll(TrafficEntity trafficEntity){
        //获取当前用户信息
        Account currentUser = TokenUtils.getCurrentUser();
        if(currentUser != null && "ADMIN".equals(currentUser.getRole())){
            trafficEntity.setUserId(currentUser.getId());
        }
        List<TrafficEntity> trafficList = trafficService.selectAll(trafficEntity);
        return Result.success(trafficList);
    }

    //管理员分页查询
    @GetMapping("/selectPage")
    public Result selectPage(TrafficEntity trafficEntity,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize
            ){
        //获取当前用户信息
        Account currentUser = TokenUtils.getCurrentUser();
        if(currentUser != null && !"ADMIN".equals(currentUser.getRole())){
            trafficEntity.setUserId(currentUser.getId());
        }
        PageInfo<TrafficEntity> pageInfo = trafficService.selectPage(trafficEntity,pageNum,pageSize);
       //泛型参数 <TrafficEntity> 表示这个 PageInfo 中存储的是 TrafficEntity 类型的数据
        return Result.success(pageInfo);//返回的是分页对象
    }

    @GetMapping("/export")
    public void exportData(TrafficEntity trafficEntity,HttpServletResponse response) throws Exception {
        String ids= trafficEntity.getIds();
        if(StrUtil.isNotBlank(ids)){
            String[] idsArr = ids.split(",");
            trafficEntity.setIdsArr(idsArr);
        }
        //1.拿到所有的数据<TrafficEntity>：这是 Java 的泛型语法，
        // 用于指定 List 集合中存储的元素类型为 TrafficEntity
        List<TrafficEntity> list = trafficService.selectAll(trafficEntity);
        //2.构建Writer对象
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //设置中文表头
        writer.addHeaderAlias("srcIp", "源IP");
        writer.addHeaderAlias("dstIp", "目的IP");
        writer.addHeaderAlias("packetSize", "报文大小");
        writer.addHeaderAlias("userId", "用户ID");
        writer.addHeaderAlias("label", "标签");
        writer.addHeaderAlias("date", "日期");
        writer.addHeaderAlias("requestFrequency", "请求频率");
        writer.setOnlyAlias(true);
    //4.写出数据到writer
        writer.write(list);
        //5.设置输出的文件名称以及输出流的信息
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("流量数据", StandardCharsets.UTF_8) ;
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");
        //6.写出到输出流中 并关闭writer
        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream);
        writer.close();
        outputStream.close();
    }

    /*批量导入
     */
    @PostMapping("/import")
    public Result importData(MultipartFile file) throws Exception {
//        1.拿到输入流 构建reader
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        //2.通过reader读取excel数据
        reader.addHeaderAlias("源IP","srcIp");
        reader.addHeaderAlias("目的IP","dstIp");
        reader.addHeaderAlias("报文大小","packetSize");
        reader.addHeaderAlias("用户ID","userId");
        reader.addHeaderAlias("标签","label");
        reader.addHeaderAlias("日期","date");
        reader.addHeaderAlias("请求频率","requestFrequency");
        List<TrafficEntity> list = reader.readAll(TrafficEntity.class);
        //3.将数据写入到数据库
        for (TrafficEntity trafficEntity : list) {
            trafficService.add(trafficEntity);
        }
        return Result.success();
    }


    //测试
    @GetMapping("/startCapture")
    public Result startCapture(@RequestParam String ip){
        if(running.get()){
            return Result.error("500","Capture is already running!");
        }
        running.set(true);
        trafficData.clear();
        packetCount = 0;

        new Thread(()->{
            PcapHandle handle = null;
            try {
                InetAddress addr = InetAddress.getByName(ip);
                PcapNetworkInterface nif = Pcaps.getDevByAddress(addr);
                if(nif == null){
                    System.err.println("未找到匹配的网卡！");
                    running.set(false);
                    return;
                }
                //定义抓包参数
                int snaplen = 65536;
                PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;
                int timeout = 10000;

                handle = nif.openLive(snaplen,mode,timeout);
                //新增：创建PcapDumper对象，用于保存数据包到pcapng文件
                dumper = handle.dumpOpen(PCAPNG_FILE_PATH);

                long startTime = System.currentTimeMillis();
                while(running.get()){
                        try{
                        Packet packet = handle.getNextPacketEx();
                        if(packet !=null && packet.contains(IpV4Packet.class)){
                            packetCount++;
                            //新增：将不获的数据包写入pcapng文件
                            dumper.dump(packet,handle.getTimestamp());
                         }
                        } catch(PcapNativeException | NotOpenException e){
                        System.err.println("捕获数据时发生异常！"+e.getMessage());
                        break;
                }
                    long currentTime = System.currentTimeMillis();
                        if(currentTime - startTime >=STATISTIC_INTERVAL){
                            synchronized (trafficData){
                                trafficData.add(packetCount);
                            }
                            packetCount =0;
                            startTime = currentTime;
                        }
                    }
                }catch (Exception e){
            System.err.println("捕获数据时发生异常！"+e.getMessage());
            e.printStackTrace();
            }finally{
            if(dumper != null){
                try{
                    // 确保 dumper 刷新并关闭
                    dumper.flush();
                    dumper.close();
            }catch(Exception e){
                System.err.println("关闭dumper时发生异常！"+e.getMessage());
                }
            }
            if(handle != null && handle.isOpen()){
            try{
                handle.close();
            }catch (Exception e){
                System.err.println("关闭捕获时发生异常！"+e.getMessage());
            }
        }
        running.set(false);
        }
    }).start();
        return Result.success();
        }


    @GetMapping("/stopCapture")
    public Result stopCapture(){
        running.set (false);
        return Result.success("Capture stopped!");
    }

    @GetMapping("/trafficData")
    public List<Integer> getTrafficData() {
        return trafficData;
    }

    @GetMapping("/downloadPcapng")
    public ResponseEntity<StreamingResponseBody> downloadPcapng() {
        File file = new File(PCAPNG_FILE_PATH);
        if (!file.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "traffic.pcapng");

        StreamingResponseBody responseBody = outputStream -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }

    @PostMapping("/uploadAndConvertPcapng")
    public Result uploadAndConvertPcapng(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error();
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            // 写入 CSV 表头
            writer.write("SrcIp,DstIp,SrcPort,DstPort,HttpOpen,TcpWindowSize,ZeroWindow,Duration,ResPacketSplit,Prediction\n");

            // 创建临时文件
            File tempFile = File.createTempFile("temp_pcapng", ".pcapng");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            try (PcapHandle handle = Pcaps.openOffline(tempFile.getAbsolutePath())) {
                Packet packet;
                int packetCounter = 0;
                while ((packet = handle.getNextPacket()) != null) {
                    String srcIp = "None";
                    String dstIp = "None";
                    String srcPort = "None";
                    String dstPort = "None";
                    String httpOpen = "None";
                    String tcpWindowSize = "None";
                    String zeroWindow = "None";
                    String duration = "None";
                    String resPacketSplit = "None";
                    String prediction = "None";

                    if (packet.contains(IpV4Packet.class)) {
                        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                        srcIp = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
                        dstIp = ipV4Packet.getHeader().getDstAddr().getHostAddress();

                        if (packet.contains(TcpPacket.class)) {
                            TcpPacket tcpPacket = packet.get(TcpPacket.class);
                            TcpPort srcTcpPort = tcpPacket.getHeader().getSrcPort();
                            TcpPort dstTcpPort = tcpPacket.getHeader().getDstPort();
                            srcPort = srcTcpPort.valueAsString();
                            dstPort = dstTcpPort.valueAsString();
                            tcpWindowSize = String.valueOf(tcpPacket.getHeader().getWindow());
                            zeroWindow = tcpPacket.getHeader().getWindow() == 0 ? "True" : "False";

                            if (packet.contains(TcpPacket.class) || packet.contains(TcpPacket.class)) {
                                httpOpen = "True";
                            }
                        }
                    }

                    writer.write(srcIp + "," + dstIp + "," + srcPort + "," + dstPort + "," + httpOpen + "," +
                            tcpWindowSize + "," + zeroWindow + "," + duration + "," + resPacketSplit + "," + prediction + "\n");
                    packetCounter++;
                }
                logger.info("成功处理 " + packetCounter + " 个数据包，csv 文件转换完成");
            } catch (IOException | org.pcap4j.core.PcapNativeException | NotOpenException e) {
                logger.log(Level.SEVERE, "转换 pcapng 文件到 csv 文件时发生异常", e);
                return Result.error();
            } finally {
                writer.close();
                // 删除临时文件
                tempFile.delete();
            }

            return Result.success(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error();
        }
    }
}
