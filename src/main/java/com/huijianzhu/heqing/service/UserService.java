package com.huijianzhu.heqing.service;

import com.huijianzhu.heqing.definition.UserAccpetDefinition;
import com.huijianzhu.heqing.vo.SystemResult;

/**
 * ================================================================
 * 说明：用户业务操作接口
 * <p>
 * 作者          时间                    注释
 * 刘梓江	2020/5/5  15:02            创建
 * =================================================================
 **/
public interface UserService {

    /**
     * 分页显示用户的数据
     *
     * @param startPage    起始页数
     * @param row          每页显示的行数
     * @param queryContent 筛选内容
     * @return
     */
    public SystemResult pageUsers(Integer startPage, Integer row, String queryContent);

    /**
     * 添加用户
     *
     * @param definition 接收添加用户属性信息实体
     * @return
     */
    public SystemResult addUser(UserAccpetDefinition definition) throws Exception;

    /**
     * 获取指定用户id对应的用户信息
     *
     * @param userId
     * @return
     */
    public SystemResult getUserById(Integer userId);


    /**
     * 添加用户
     *
     * @param definition 接收修改用户属性信息实体
     * @return
     */
    public SystemResult updateUser(UserAccpetDefinition definition) throws Exception;

    /**
     * 删除用户
     *
     * @param userIds 对应要删除的用户信息id集
     * @return
     */
    public SystemResult deleteUser(String userIds) throws Exception;


    /**
     * 获取用户权限信息
     *
     * @param userId 对应获取该id用户对应的权限信息
     * @return
     */
    public SystemResult userJurisdiction(Integer userId);

    /**
     * 修改对应用户的权限信息
     *
     * @param definition 接收修改用户属性信息实体
     * @return
     */
    public SystemResult updateJurisdiction(UserAccpetDefinition definition);
}
    
    
    