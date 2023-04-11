package com.xxjs.framework.web.service;


import com.xxjs.common.constant.CacheConstants;
import com.xxjs.common.constant.Constants;
import com.xxjs.common.core.domain.model.LoginUser;
import com.xxjs.common.core.redis.RedisCache;
import com.xxjs.common.utils.StringUtils;
import com.xxjs.common.utils.uuid.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * token验证处理
 *
 * @author ruoyi
 */
@Component
public class TokenService
{
    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        // 该 token 不包括前缀 Bearer
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                //   token 解析器，获取 token 中的身份信息
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                // uuid 用来区别不同的用户，对应不同的 token，也就是对应不同的 loginUser
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUser user = redisCache.getCacheObject(userKey);
                return user;
            }
            catch (Exception e)
            {
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建令牌
     * 相当于是用户的唯一标识
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(LoginUser loginUser)
    {
        //随机生成一串字符串
        String token = IdUtils.fastUUID();
        //将 token 存到 loginUser 中，就是一个 uuid
        loginUser.setToken(token);
        setUserAgent(loginUser);
        //刷新有效期
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        //根据 uuid 注入到 token 的声明信息中，以后反解析的时候，可以从 token 中获取的该 claims 声明的所有信息
        //不同的 uuid 对应着不同的 login用户
        claims.put(Constants.LOGIN_USER_KEY, token);
        //真正的生成令牌 token
        //根据这些数据，生成 token
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser
     * @return 令牌
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        // uuid 用来缓存不同的 user 用户的token，也就是一台电脑可以登录多个账户
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent(LoginUser loginUser)
    {
        //UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        //String ip = IpUtils.getIpAddr();
        //loginUser.setIpaddr(ip);
        //loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        //loginUser.setBrowser(userAgent.getBrowser().getName());
        //loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                //声明的数据信息，用它来加密生成 token
                .setClaims(claims)
                //签名，  secret 密钥，位于 yml 文件中
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request)
    {
        //获取前端携带的 token 值
        String token = request.getHeader(header);
        //判断 token 是否是以 Bearer 前缀为开头
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            //将 前缀 Bearer 去掉
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        //返回真正的 Token
        return token;
    }

    private String getTokenKey(String uuid)
    {
        //   redis 拼接,  uuid 就是普通的 uuid
        // login_tokens: + uuid
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}
