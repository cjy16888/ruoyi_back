package com.xxjs.controller.common;

import com.google.code.kaptcha.Producer;
import com.xxjs.common.config.BaseConfig;
import com.xxjs.common.constant.CacheConstants;
import com.xxjs.common.constant.Constants;
import com.xxjs.common.core.domain.AjaxResult;
import com.xxjs.common.core.redis.RedisCache;
import com.xxjs.common.utils.sign.Base64;
import com.xxjs.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 * 
 * @author xxjs
 */
@RestController
@SuppressWarnings("all")
public class CaptchaController
{
    //字符串验证码
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    //数字math验证码
    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;
    
    //@Autowired
    //private ISysConfigService configService;

    @Autowired
    private BaseConfig baseConfig;
    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException
    {
        AjaxResult ajax = AjaxResult.success();
        //是否开启验证码验证功能
        //boolean captchaEnabled = configService.selectCaptchaEnabled();
        boolean captchaEnabled = true;
        ajax.put("captchaEnabled", captchaEnabled);
        //如果没有开启验证码验证这个功能的话，直接进行返回就行
        if (!captchaEnabled)
        {
            return ajax;
        }

        // 保存验证码信息
        // 每一个用户碰到验证码可能不一样，所以验证码有需要一个 UUID
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        //根据需要进行选择我们需要的  登陆的验证码的方式
        //后期因该是我们自己进行传递一个参数，进行选择，或者是通过配置文件进行配置
        // 生成验证码
        baseConfig.setCaptchaType("math");
        String captchaType = baseConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            //math，数学验证码
            //这个 createText 被重写了，自己实现的算法，生成数学公式
            String capText = captchaProducerMath.createText();
            //因为上面重写的方法中，生成的  数字公式验证码   是 @ 结尾的，@ 后面是 result ，不进行显示
            capStr = capText.substring(0, capText.lastIndexOf("@"));   //计算验证码的数学公式
            code = capText.substring(capText.lastIndexOf("@") + 1);  //验证码的正确答案
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            //text，字符串文本验证码
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        /**
         * key、value、timeout、timeunit
         * String类型， 存放验证码答案 code 到  redis 中
         */
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            return AjaxResult.error(e.getMessage());
        }

        //response响应请求的验证码的信息
        ajax.put("uuid", uuid);
        //转成 base64 编码格式传递到前端
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
}
