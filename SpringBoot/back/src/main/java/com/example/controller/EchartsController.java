package com.example.controller;

import com.example.common.Result;
import com.example.service.TrafficService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/echarts")
public class EchartsController {
    @Resource
    public TrafficService trafficService;

    @GetMapping("/pie")
    public Result pie(@RequestParam(required = false) Integer userId) {
        List<Map<String, Object>> labelCountList;
        if (userId != null) {
            labelCountList = trafficService.getLabelCountByUserId(userId);
        } else {
            labelCountList = trafficService.getLabelCount();
        }
        return Result.success(labelCountList);
    }
}