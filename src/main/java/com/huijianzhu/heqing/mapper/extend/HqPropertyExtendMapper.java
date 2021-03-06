package com.huijianzhu.heqing.mapper.extend;

import com.huijianzhu.heqing.entity.HqProperty;
import com.huijianzhu.heqing.mapper.HqPropertyMapper;
import com.huijianzhu.heqing.pojo.PropertyTree;
import com.huijianzhu.heqing.pojo.PropertyUpdateContent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * ================================================================
 * 说明：操作属性表信息扩展mapper接口
 * <p>
 * 作者          时间                    注释
 * 刘梓江    2020/5/7  11:40            创建
 * =================================================================
 **/
public interface HqPropertyExtendMapper extends HqPropertyMapper {


    /**
     * 查询指定属性名信息下所有有效属性列表信息
     *
     * @param delFlag
     * @return
     */
    @Select({"<script>",
            " select * from hq_property where ",
            " del_flag=#{delFlag}  ",
            " and property_type=#{propertyType}  ",
            " <if test='searchName!=null'>",
            " and  property_name like concat('%',#{searchName},'%') ",
            "</if>",
            "</script>"})
    List<PropertyTree> getValidpropertyS(String searchName, String delFlag, String propertyType);


    /**
     * 获取指定属性名对应的属性信息
     *
     * @param propertyName 属性名称
     * @param delFlag      删除标志
     * @param propertyId   属性id
     * @return
     */
    @Select({"<script> ",
            " select * from hq_property where  del_flag=#{delFlag} and property_name=#{propertyName} ",
            " <if test='propertyId!=null'> ",
            " and  property_id!=#{propertyId} ",
            " </if> ",
            " </script>"
    })
    HqProperty getPropertyContent(String propertyName, String delFlag, Integer propertyId);

    /**
     * 获取有效属性id对应的属性信息
     *
     * @param delFlag    删除标志
     * @param propertyId 属性id
     * @return
     */
    @Select(" select * from hq_property where  del_flag=#{delFlag} and property_id=#{propertyId}  ")
    PropertyUpdateContent getPropertyById(String delFlag, Integer propertyId);


    /**
     * 判断当前父节点下是否有子节点信息
     *
     * @param delFlag  删除标志
     * @param parentId 属性内容
     * @return
     */
    @Select(" select * from hq_property where parent_id=#{parentId} and del_flag=#{delFlag} ")
    List<HqProperty> childrenPropertiesExist(String delFlag, Integer parentId);


    /**
     * 批量修改删除操作
     *
     * @param propertyS 属性id集合
     * @param delFlag   删除标志
     */

    @Update({

            "<script> ",
            " UPDATE  hq_property",
            " <trim prefix ='set' prefixOverrides=',' > ",

            "<trim prefix ='del_flag = case' suffix='end,'>",
            "<foreach collection ='propertyS' item ='item' index = 'index'> ",
            "when property_id = #{item.propertyId} then #{delFlag} ",
            "</foreach>",
            "</trim> ",

            "<trim prefix ='update_time = case' suffix='end,'>",
            "<foreach collection ='propertyS' item ='item' index = 'index'> ",
            "when property_id = #{item.propertyId} then #{item.updateTime} ",
            "</foreach>",
            "</trim> ",


            "<trim prefix ='update_user_name = case' suffix='end'>",
            "<foreach collection ='propertyS' item ='item' index = 'index'> ",
            "when property_id = #{item.propertyId} then #{item.updateUserName} ",
            "</foreach>",
            "</trim> ",

            " </trim> ",

            " WHERE property_id in ",
            " (",
            "<foreach collection='propertyS' item='item' index='index' separator=','>",
            " #{item.propertyId}",
            "</foreach>",
            " )",
            "</script>"
    })
    void batchDelete(@Param("propertyS") List<HqProperty> propertyS, String delFlag);


}