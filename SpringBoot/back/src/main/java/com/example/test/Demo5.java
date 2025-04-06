package com.example.test;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Target;
import org.jpmml.evaluator.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Demo5 {
    private Evaluator loadPmml() {
        PMML pmml = new PMML();
        InputStream inputStream = null;
        try {
            // 修改为你的 PMML 文件路径
            inputStream = new FileInputStream("A:\\Desktop\\SpringBoot\\back\\src\\main\\resources\\PMML\\cart_model2.pmml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            return null;
        }
        InputStream is = inputStream;
        try {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (JAXBException e2) {
            e2.printStackTrace();
        } finally {
            // 关闭输入流
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        pmml = null;
        return evaluator;
    }

    private int predict(Evaluator evaluator, String httpOpen, int tcpWindowSize, int zeroWindow, int duration) {
        // 对输入数据进行预处理，将其转换为 PMML 所需的格式
        double httpOpenValue = httpOpen.equals("Yes") ? 1.0 : 0.0;
        double tcpWindowSizeLe1024Value = tcpWindowSize <= 1024 ? 1.0 : 0.0;
        double zeroWindowGt10Value = zeroWindow > 10 ? 1.0 : 0.0;
        double durationGt300Value = duration > 300 ? 1.0 : 0.0;

        // 按照 PMML 文件中的特征名来构建输入数据
        Map<String, Double> data = new HashMap<>();
        data.put("httpOpen", httpOpenValue);
        data.put("tcpWindowSize<=1024", tcpWindowSizeLe1024Value);
        data.put("zeroWindow>10/min", zeroWindowGt10Value);
        data.put("duration>300s", durationGt300Value);

        List<InputField> inputFields = evaluator.getInputFields();

        // 模型的原始特征，从画像中获取数据，作为模型输入
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName.getValue());
            FieldValue inputFieldValue = inputField.prepare(rawValue);
            arguments.put(inputFieldName, inputFieldValue);
        }

        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        List<TargetField> targetFields = evaluator.getTargetFields();

        TargetField targetField = targetFields.get(0);
        FieldName targetFieldName = targetField.getName();

        Object targetFieldValue = results.get(targetFieldName);
        System.out.println("target: " + targetFieldName.getValue() + " value: " + targetFieldValue);
        int primitiveValue = -1;
        if (targetFieldValue instanceof Computable) {
            Computable computable = (Computable) targetFieldValue;
            primitiveValue = ((Number) computable.getResult()).intValue();
        }
        String result = (primitiveValue == 1) ? "ABNORMAL" : "NORMAL";
        System.out.println("httpOpen:"+httpOpen + " tcpWindowSize:" + tcpWindowSize + " zeroWindow:" + zeroWindow + " duration:" + duration + " 预测结果：" + result);
        return primitiveValue;
    }

    public static void main(String[] args) {
        Demo5 demo = new Demo5();
        Evaluator model = demo.loadPmml();
        if (model != null) {
            // 调用预测方法，传入 example_data 中的值
            demo.predict(model, "Yes", 490, 20, 200);
        }
    }
}