package com.xxjs.controller.system;


import com.xxjs.common.constant.Constants;
import com.xxjs.common.core.domain.AjaxResult;
import com.xxjs.common.core.domain.entity.SysMenu;
import com.xxjs.common.core.domain.entity.SysUser;
import com.xxjs.common.core.domain.model.LoginBody;
import com.xxjs.common.utils.SecurityUtils;
import com.xxjs.framework.web.service.SysLoginService;
import com.xxjs.framework.web.service.SysPermissionService;
import com.xxjs.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 登录方法
     * uuid-唯一标识，前端生成的
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 前端异步请求
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        //获取当前的登录用户信息
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        //这玩意继承了  HashMap
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 前端异步请求
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        //根据用户的权限，查看管理权限
        Long userId = SecurityUtils.getUserId();
        //不同的用户的菜单显示可能是不一样的
        //获取当前用户可显示的菜单列表
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        //构建管理系统的左侧菜单列
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
