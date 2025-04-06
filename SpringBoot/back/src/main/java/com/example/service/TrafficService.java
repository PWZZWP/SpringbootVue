package com.example.service;

import java.util.Map;
import cn.hutool.core.date.DateUtil;
import com.example.entity.Account;
import com.example.entity.TrafficEntity;
import com.example.mapper.TrafficMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrafficService {

    @Resource
    TrafficMapper trafficMapper;

    public List<Map<String, Object>> getLabelCount() {
        return trafficMapper.countByLabel();
    }

    public List<Map<String, Object>> getLabelCountByUserId(Integer userId) {
        return trafficMapper.countByLabelAndUserId(userId);
    }

    public void add(TrafficEntity trafficEntity) {
        Account currentUser = TokenUtils.getCurrentUser();
        trafficEntity.setUserId(currentUser.getId());
        trafficEntity.setDate(DateUtil.now());
        trafficEntity.setRole(currentUser.getRole());
        trafficMapper.insert(trafficEntity);
    }

    public List<TrafficEntity> selectAll(TrafficEntity trafficEntity) {
        return trafficMapper.selectAll(trafficEntity);
    }

    public PageInfo<TrafficEntity> selectPage(TrafficEntity trafficEntity, Integer pageNum, Integer pageSize) {
        //开启分页查询
        Account currentUser = TokenUtils.getCurrentUser();
        if ("USER".equals(currentUser.getRole())) {
            trafficEntity.setUserId(currentUser.getId());
            trafficEntity.setRole(currentUser.getRole());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<TrafficEntity> list = trafficMapper.selectAll(trafficEntity);
        return PageInfo.of(list);
    }

    public void update(TrafficEntity trafficEntity) {
        trafficMapper.updateById(trafficEntity);
    }

    public void deleteById(Integer id) {
        trafficMapper.deleteById(id);
    }

    public void deleteBatch(List<TrafficEntity> list) {
        for (TrafficEntity trafficEntity : list) {
            this.deleteById(trafficEntity.getId());
        }
    }

    //模型加载和预测
}