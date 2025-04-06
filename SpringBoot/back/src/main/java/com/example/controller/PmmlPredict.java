package com.example.controller;

import com.example.common.Result;
import org.apache.commons.io.FilenameUtils;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/pmml")
public class PmmlPredict {
    private static final Logger logger = LoggerFactory.getLogger(PmmlPredict.class);
    private static final String MODEL_DIR = "A:\\Desktop\\SpringBoot\\back\\src\\main\\resources\\PMML\\";
    private static final String DEFAULT_MODEL_FILE = "cart_model2.pmml";
    private static final AtomicInteger currentModelId = new AtomicInteger(1);
    private static final Map<String, Integer> fileIdMap = new HashMap<>();
    private static int currentEnabledModelId = -1;

    static {
        initModels();
    }

    private static void initModels() {
        File directory = new File(MODEL_DIR);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".pmml")) {
                    fileIdMap.put(file.getName(), currentModelId.getAndIncrement());
                }
            }
        }
    }

    @PostMapping("/uploadModel")
    public Result uploadModel(@RequestParam("modelFile") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".pmml")) {
                return Result.error("400", "请上传有效的 PMML 文件");
            }

            Path destPath = Paths.get(MODEL_DIR, fileName);
            File dest = destPath.toFile();

            if (dest.exists()) {
                return Result.error("400", "文件已存在，请更改文件名后重新上传");
            }

            Files.createDirectories(destPath.getParent());
            file.transferTo(dest);

            int modelId = currentModelId.getAndIncrement();
            fileIdMap.put(fileName, modelId);
            return Result.success("模型导入成功");
        } catch (IOException e) {
            logger.error("模型上传失败，文件名: {}", file.getOriginalFilename(), e);
            return Result.error("500", "模型导入失败");
        }
    }

    @PostMapping("/disableModel/{modelId}")
    public Result disableModel(@PathVariable int modelId) {
        if (currentEnabledModelId == modelId) {
            currentEnabledModelId = -1;
            return Result.success("模型禁用成功");
        }
        return Result.error("400", "该模型未启用，无需禁用");
    }

    @GetMapping("/getModels")
    public Result getModels() {
        List<Map<String, Object>> modelList = new ArrayList<>();
        File directory = new File(MODEL_DIR);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".pmml")) {
                    String fileName = file.getName();
                    int modelId = fileIdMap.getOrDefault(fileName, -1);
                    String status = (modelId == currentEnabledModelId) ? "启用" : "停用";
                    modelList.add(Map.of(
                            "id", modelId,
                            "name", fileName,
                            "status", status
                    ));
                }
            }
        }
        return Result.success(modelList);
    }

    @PostMapping("/enableModel/{modelId}")
    public Result enableModel(@PathVariable int modelId) {
        if (!fileIdMap.containsValue(modelId)) {
            return Result.error("404", "模型未找到");
        }

        currentEnabledModelId = modelId;
        return Result.success("模型启用成功");
    }

    @DeleteMapping("/deleteModel/{modelId}")
    public Result deleteModel(@PathVariable int modelId) {
        String fileName = fileIdMap.entrySet().stream()
                .filter(entry -> entry.getValue() == modelId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (fileName == null) {
            return Result.error("404", "模型未找到");
        }

        File file = new File(MODEL_DIR + fileName);
        if (file.exists() && !file.delete()) {
            return Result.error("500", "删除模型文件失败");
        }

        fileIdMap.remove(fileName);
        if (modelId == currentEnabledModelId) {
            currentEnabledModelId = -1;
        }
        return Result.success("模型删除成功");
    }

    private Evaluator loadPmml() {
        try {
            String fileName = fileIdMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == currentEnabledModelId)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(DEFAULT_MODEL_FILE);

            InputStream inputStream = new FileInputStream(MODEL_DIR + fileName);
            PMML pmml = PMMLUtil.unmarshal(inputStream);
            ModelEvaluatorFactory factory = ModelEvaluatorFactory.newInstance();
            return factory.newModelEvaluator(pmml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/predict")
    public Result predict(@RequestParam String httpOpen, @RequestParam int tcpWindowSize,
                          @RequestParam int zeroWindow, @RequestParam int duration,
                          @RequestParam int resPacketSplit,
                          @RequestParam String srcIp, @RequestParam String dstIp,
                          @RequestParam int srcPort, @RequestParam int dstPort) {
        if ("No".equals(httpOpen)) {
            System.out.println("httpOpen:" + httpOpen + " tcpWindowSize:" + tcpWindowSize + " zeroWindow:" + zeroWindow + " duration:" + duration + " resPacketSplit:" + resPacketSplit + " srcIp:" + srcIp + " dstIp:" + dstIp + " srcPort:" + srcPort + " dstPort:" + dstPort + " 预测结果：NORMAL");
            return Result.success("NORMAL");
        }

        Evaluator evaluator = loadPmml();
        if (evaluator == null) {
            return Result.error("500", "未能预测，模型加载失败");
        }
        double httpOpenValue = httpOpen.equals("Yes") ? 1.0 : 0.0;
        double tcpWindowSizeLe1024Value = tcpWindowSize <= 1024 ? 1.0 : 0.0;
        double zeroWindowGt10Value = zeroWindow > 10 ? 1.0 : 0.0;
        double durationGt300Value = duration > 300 ? 1.0 : 0.0;

        Map<String, Double> data = new HashMap<>();
        data.put("httpOpen", httpOpenValue);
        data.put("tcpWindowSize<=1024", tcpWindowSizeLe1024Value);
        data.put("zeroWindow>10/min", zeroWindowGt10Value);
        data.put("duration>300s", durationGt300Value);

        List<InputField> inputFields = evaluator.getInputFields();
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName.getValue());
            FieldValue inputFieldValue = inputField.prepare(rawValue);
            arguments.put(inputFieldName, inputFieldValue);
        }

        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        TargetField targetField = evaluator.getTargetFields().get(0);
        FieldName targetFieldName = targetField.getName();

        Object targetFieldValue = results.get(targetFieldName);
        int primitiveValue = -1;
        if (targetFieldValue instanceof Computable) {
            Computable computable = (Computable) targetFieldValue;
            primitiveValue = ((Number) computable.getResult()).intValue();
        }
        String result = (primitiveValue == 1) ? "ABNORMAL" : "NORMAL";
        System.out.println("httpOpen:" + httpOpen + " tcpWindowSize:" + tcpWindowSize + " zeroWindow:" + zeroWindow + " duration:" + duration + " resPacketSplit:" + resPacketSplit  + " srcIp:" + srcIp + " dstIp:" + dstIp + " srcPort:" + srcPort + " dstPort:" + dstPort + " 预测结果：" + result);
        return Result.success(result);
    }

    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam("fileName") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("400", "上传的文件为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
                return Result.error("400", "只允许上传 CSV 文件");
            }

            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return Result.error("400", "文件大小超过限制（最大10MB）");
            }

            String sanitizedFilename = sanitizeFilename(originalFilename);
            String uniqueFilename = generateUniqueFilename(sanitizedFilename);

            String uploadDir = "csvFiles";
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(uniqueFilename);
            file.transferTo(filePath);

            return Result.success(Map.of(
                    "fileName", uniqueFilename,
                    "fileSize", file.getSize(),
                    "filePath", filePath.toString()
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("500", "文件上传失败");
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    private String generateUniqueFilename(String filename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String extension = FilenameUtils.getExtension(filename);
        String baseName = FilenameUtils.getBaseName(filename);
        return baseName + "_" + timestamp + "." + extension;
    }

    @PostMapping("/predict2")
    public Result predict2(@RequestParam("filePath") MultipartFile file) {
        Evaluator evaluator = loadPmml();
        if (evaluator == null) {
            return Result.error("500", "未能预测，模型加载失败");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            String fileContent = content.toString();

            List<Map<String, String>> records = parseCsv(fileContent);
            if (records.isEmpty()) {
                return Result.error("400", "文件内容为空");
            }

            Set<String> requiredFields = new HashSet<>(Arrays.asList(
                    "httpOpen", "tcpWindowSize", "zeroWindow", "duration"
            ));
            if (!records.get(0).keySet().containsAll(requiredFields)) {
                return Result.error("400", "CSV 文件缺少必要列：" + requiredFields);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, String> record : records) {
                try {
                    String httpOpen = record.get("httpOpen");
                    String predictionResult;
                    if ("No".equals(httpOpen)) {
                        predictionResult = "NORMAL";
                    } else {
                        double httpOpenValue = parseDouble(record.get("httpOpen"), 0.0);
                        int tcpWindowSize = parseInteger(record.get("tcpWindowSize"), 0);
                        int zeroWindow = parseInteger(record.get("zeroWindow"), 0);
                        int duration = parseInteger(record.get("duration"), 0);

                        httpOpenValue = httpOpenValue == 1.0 ? 1.0 : 0.0;
                        double tcpWindowSizeLe1024Value = tcpWindowSize <= 1024 ? 1.0 : 0.0;
                        double zeroWindowGt10Value = zeroWindow > 10 ? 1.0 : 0.0;
                        double durationGt300Value = duration > 300 ? 1.0 : 0.0;

                        Map<String, Double> data = new HashMap<>();
                        data.put("httpOpen", httpOpenValue);
                        data.put("tcpWindowSize<=1024", tcpWindowSizeLe1024Value);
                        data.put("zeroWindow>10/min", zeroWindowGt10Value);
                        data.put("duration>300s", durationGt300Value);

                        predictionResult = performPrediction(evaluator, data);
                    }
                    results.add(new HashMap<String, Object>() {{
                        put("input", record);
                        put("prediction", predictionResult);
                    }});
                } catch (NumberFormatException e) {
                    return Result.error("400", "数据格式错误：" + e.getMessage());
                }
            }
            return Result.success(results);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("500", "文件读取失败：" + e.getMessage());
        }
    }

    private List<Map<String, String>> parseCsv(String content) {
        List<Map<String, String>> records = new ArrayList<>();
        String[] lines = content.split("\n");
        if (lines.length < 2) {
            return records;
        }

        String[] headers = lines[0].split(",");
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            Map<String, String> record = new LinkedHashMap<>();
            for (int j = 0; j < headers.length; j++) {
                record.put(headers[j].trim(), j < values.length ? values[j].trim() : "");
            }
            records.add(record);
        }
        return records;
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String performPrediction(Evaluator evaluator, Map<String, Double> data) {
        List<InputField> inputFields = evaluator.getInputFields();
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName.getValue());
            FieldValue inputFieldValue = inputField.prepare(rawValue);
            arguments.put(inputFieldName, inputFieldValue);
        }

        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        TargetField targetField = evaluator.getTargetFields().get(0);
        FieldName targetFieldName = targetField.getName();

        Object targetFieldValue = results.get(targetFieldName);
        int primitiveValue = -1;
        if (targetFieldValue instanceof Computable) {
            Computable computable = (Computable) targetFieldValue;
            primitiveValue = ((Number) computable.getResult()).intValue();
        }
        return (primitiveValue == 1) ? "ABNORMAL" : "NORMAL";
    }




}