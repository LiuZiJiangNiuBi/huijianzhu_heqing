package com.huijianzhu.heqing.controller;

import com.huijianzhu.heqing.service.LoginService;
import com.huijianzhu.heqing.vo.SystemResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * 说明：用户登录请求控制器
 * <p>
 * 作者          时间                    注释
 * 刘梓江    2020/5/6  13:54            创建
 * =================================================================
 **/
@Slf4j      //日志使用
@Validated  //数据校验
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;      //注入用户业务接口


    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param password    用户密码
     * @return
     */
    @PostMapping("/login")
    public SystemResult login(String userAccount, String password) {
        return loginService.login(userAccount, password);
    }

    /**
     * 用户退出登录
     *
     * @return
     */
    @PostMapping("/loginOut")
    public SystemResult loginOut() {
        return loginService.loginOut();
    }


}
