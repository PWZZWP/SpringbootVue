package com.example.mapper;

import com.example.entity.TrafficEntity;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface TrafficMapper {
    List<TrafficEntity> selectAll(TrafficEntity trafficEntity);

    void insert(TrafficEntity trafficEntity);

    void updateById(TrafficEntity trafficEntity);

    @Delete("delete from `traffic_data` where id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT label, COUNT(*) as count FROM `traffic_data` GROUP BY label")
    List<Map<String, Object>> countByLabel();

    @Select("SELECT label, COUNT(*) as count FROM `traffic_data` WHERE user_id = #{userId} GROUP BY label")
    List<Map<String, Object>> countByLabelAndUserId(Integer userId);
}