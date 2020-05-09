package com.huijianzhu.heqing.service.impl;

import cn.hutool.core.util.StrUtil;
import com.huijianzhu.heqing.cache.LoginTokenCacheManager;
import com.huijianzhu.heqing.definition.PlotOrHouseOrPipeAccpetDefinition;
import com.huijianzhu.heqing.definition.PlotOrHouseOrPipeUpdateAccpetDefinition;
import com.huijianzhu.heqing.entity.HqPlot;
import com.huijianzhu.heqing.entity.HqPropertyValue;
import com.huijianzhu.heqing.entity.HqPropertyValueWithBLOBs;
import com.huijianzhu.heqing.enums.GLOBAL_TABLE_FILED_STATE;
import com.huijianzhu.heqing.enums.LOGIN_STATE;
import com.huijianzhu.heqing.enums.PLOT_HOUSE_PIPE_TYPE;
import com.huijianzhu.heqing.enums.SYSTEM_RESULT_STATE;
import com.huijianzhu.heqing.lock.PlotLock;
import com.huijianzhu.heqing.lock.PropertyLock;
import com.huijianzhu.heqing.mapper.extend.HqPlotExtendMapper;
import com.huijianzhu.heqing.mapper.extend.HqPropertyValueExtendMapper;
import com.huijianzhu.heqing.pojo.*;
import com.huijianzhu.heqing.service.PlotService;
import com.huijianzhu.heqing.service.PropertyService;
import com.huijianzhu.heqing.service.PropertyValueService;
import com.huijianzhu.heqing.utils.CookieUtils;
import com.huijianzhu.heqing.vo.SystemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * 说明：操作地块信息业务接口实现
 * <p>
 * 作者          时间                    注释
 * 刘梓江    2020/5/8  15:41            创建
 * =================================================================
 **/
@Service
public class PlotServiceImpl implements PlotService {


    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginTokenCacheManager  loginTokenCacheManager;             //注入操作用户登录标识缓存管理

    @Autowired
    private HqPlotExtendMapper hqPlotExtendMapper;                      //操作地块信息表mapper接口

    @Autowired
    private HqPropertyValueExtendMapper hqPropertyValueExtendMapper;    //操作属性值信息表mapper接口

    @Autowired
    private PropertyService propertyService;                            //注入属性业务接口

    @Autowired
    private PropertyValueService propertyValueService;                  //注入属性值业务接口

    /**
     * 获取所有与地块名称相关的地块信息默认是查询出所有
     * @param plotName   关于地块的名称
     * @return
     */
    public SystemResult getPlotContentListByName(String plotName){
        //判断是否有指定查询的内容没有默认是为null
        plotName= StrUtil.hasBlank(plotName)?null:plotName;

        //获取对应的plotName所有地块信息
        return SystemResult.ok(hqPlotExtendMapper.getPlots(plotName, GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY));
    }

    /**
     * 添加地块信息
     * @param definition 封装了地块信息的实体对象
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemResult add(PlotOrHouseOrPipeAccpetDefinition definition) throws  Exception{
        //开启原子锁操作
        PlotLock.PLOT_UPDATE_LOCK.writeLock().lock();
        try{
            //判断当前新添加到名称是否在数据库中存在
            HqPlot plot = hqPlotExtendMapper.getPlotByName(definition.getContentName(), GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY,null);
            if(plot!=null){
                return SystemResult.build(SYSTEM_RESULT_STATE.UPDATE_FAILURE.KEY,"当前地块名称已经存在,请添加其他地块名称");
            }
            //获取当前登录用户信息
            //String cookieValue = CookieUtils.getCookieValue(request, LOGIN_STATE.USER_LOGIN_TOKEN.toString());
            //UserLoginContent loginUserContent = loginTokenCacheManager.getCacheUserByLoginToken(cookieValue);

            //创建一个地块信息
            HqPlot newPlot=new HqPlot();
            newPlot.setPlotName(definition.getContentName());           //地块名称
            newPlot.setPlotMark(definition.getPlotMark());              //地标信息
            newPlot.setCreateTime(new Date());                          //创建时间
            newPlot.setUpdateTime(new Date());                          //修改时间
            //newPlot.setUpdateUserName(loginUserContent.getUserName());  //最近一次谁操作了该记录
            newPlot.setDelFlag(GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY);//默认是有效信息

            //持久化到数据库中
            hqPlotExtendMapper.insertSelective(newPlot);

            return  SystemResult.ok("地块信息添加成功");
        }catch(Exception e) {
            throw e;
        }finally {
            //解锁操作
            PlotLock.PLOT_UPDATE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 获取指定id对应的地块信息
     * @param plotId  某一个地块信息id
     * @return
     */
    public SystemResult getPlotDescById(String plotId){

        //创建一个封装本次地块信息及对应的地块属性信息
        PlotOrHouseOrPipeDesc  pipeDesc=new PlotOrHouseOrPipeDesc();

        //默认获取所有地块相关的属性信息(树结构)
        SystemResult propertiesByName = propertyService.getPropertiesByName(null, PLOT_HOUSE_PIPE_TYPE.PLOT_TYPE.KEY);
        List<PropertyTree>  treeList=(List)propertiesByName.getResult();
        pipeDesc.setPropertyTrees(treeList); //封装属性信息

        //获取当前指定地块对应的属性值信息
        List<HqPropertyValueWithBLOBs> propertyValues = hqPropertyValueExtendMapper.getPropertyValues(PLOT_HOUSE_PIPE_TYPE.PLOT_TYPE.KEY, plotId, GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY);
        pipeDesc.setPropertyValues(propertyValues); //封装本次地块属性值信息
        return SystemResult.ok(pipeDesc);
    }


    /**
     * 修改地块,地地块属性信息
     * @param definition 封装了地块修改信息及对应的,地块属性信息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemResult updateContent(PlotOrHouseOrPipeUpdateAccpetDefinition definition) throws  Exception{
        //开启修改原子锁.防止修改同地块名
        PlotLock.PLOT_UPDATE_LOCK.writeLock().lock();
        try{
            //判断当前新修改的地块名称,是否已经存在
            HqPlot plot = hqPlotExtendMapper.getPlotByName(definition.getContentName(), GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY,definition.getContentId());
            if(plot!=null){
                return SystemResult.build(SYSTEM_RESULT_STATE.UPDATE_FAILURE.KEY,"本次修改失败,当前的地块名有相同");
            }

            //获取当前用户信息
            //String loginToken = CookieUtils.getCookieValue(request, LOGIN_STATE.USER_LOGIN_TOKEN.toString());
            //UserLoginContent userContent = loginTokenCacheManager.getCacheUserByLoginToken(loginToken);

            //修改地块信息
            HqPlot updateHqplot=new HqPlot();
            updateHqplot.setUpdateTime(new Date());                     //最近的一次修改时间
            //updateHqplot.setUpdateUserName(userContent.getUserName());  //记录谁操作了本次记录
            updateHqplot.setPlotId(definition.getContentId());          //修改指定的地块id
            updateHqplot.setPlotName(definition.getContentName());      //修改新的地块名称
            updateHqplot.setPlotMark(definition.getPlotMark());         //修改新的地标信息

            //将地块信息持久化到数据库中
            hqPlotExtendMapper.updateByPrimaryKeySelective(updateHqplot);

            //更新地块对应的属性值信息
            propertyValueService.updatePropertyValue(definition.getPropertyValueList());
            return SystemResult.ok("地块信息修改成功");
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
           //关闭原子锁
            PlotLock.PLOT_UPDATE_LOCK.writeLock().lock();
        }
    }



    /**
     * 删除地块信息
     * @param plotId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemResult deleteById(Integer plotId)throws  Exception{

        //开启原子锁操作,防止修改地块信息时有可能是已经被删除的地块信息
        PlotLock.PLOT_UPDATE_LOCK.writeLock().lock();
        try{
            //判断当前地块是否有对应的房屋、管道信息
            List<HouseOrPipeContent> houseOrPipeList = hqPlotExtendMapper.getHouseOrPipeContentByPlotId(plotId, GLOBAL_TABLE_FILED_STATE.DEL_FLAG_NO.KEY);
            if(houseOrPipeList.size()>0){
                //代表当前地块不能删除,应为有 有效的地块房屋信息,或地块管道信息
                return SystemResult.build(SYSTEM_RESULT_STATE.DELETE_FAILURE.KEY,"当前删除的地块信息,有对应的管道或房屋信息,所以不能删除");
            }

            //获取当前登录用户信息
            String cookieValue = CookieUtils.getCookieValue(request, LOGIN_STATE.USER_LOGIN_TOKEN.toString());
            UserLoginContent loginUserContent = loginTokenCacheManager.getCacheUserByLoginToken(cookieValue);

            HqPlot deletePlot=new HqPlot();
            deletePlot.setPlotId(plotId);
            deletePlot.setUpdateUserName(loginUserContent.getUserName());   //记录谁操作了本次记录信息
            deletePlot.setUpdateTime(new Date());                           //记录最近的一次修改时间

            //持久化到数据库中
            hqPlotExtendMapper.updateByPrimaryKeySelective(deletePlot);

            return  SystemResult.ok("地块信息删除成功");
        }catch(Exception e) {
            throw e;
        }finally {
            //解锁操作
            PlotLock.PLOT_UPDATE_LOCK.writeLock().unlock();
        }
    }
}
