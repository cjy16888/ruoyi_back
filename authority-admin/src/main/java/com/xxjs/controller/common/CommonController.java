package com.xxjs.controller.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @BelongsProject: Ruoyi_back
 * @BelongsPackage: com.xxjs.controller
 * @Author: Master_XXJS
 * @CreateTime: 2023-04-01  09:57
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    @GetMapping
    @PreAuthorize("hasAuthority('system:post:list')")
    public String test(){
        return "hhhh哈哈哈";
    }
}

