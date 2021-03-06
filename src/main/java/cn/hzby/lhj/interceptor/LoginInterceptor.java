package cn.hzby.lhj.interceptor;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.hzby.lhj.util.JwtUtil;



/**
 * @author lhj
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {


    /**  这个方法是在访问接口之前执行的，我们只需要在这里写验证登陆状态的业务逻辑，就可以在用户调用指定接口之前验证登陆状态了 **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //每一个项目对于登陆的实现逻辑都有所区别，我这里使用最简单的Session提取User来验证登陆。

    	Cookie[] cookies =  request.getCookies();
        //如果session中没有user，表示没登陆
        if (cookies != null){
        	for(Cookie cookie : cookies){
                if("userMsg".equals(cookie.getName())){
                	Map<String, String> result = JwtUtil.checkJwt(cookie.getValue(),response);
                	return Boolean.valueOf(result.get("code"));
                }
            }
            //这个方法返回false表示忽略当前请求，如果一个用户调用了需要登陆才能使用的接口，如果他没有登陆这里会直接忽略掉
            //当然你可以利用response给用户返回一些提示信息，告诉他没登陆
            response.setStatus(401);
            return false;
        }else {
            /**  如果session里有user，表示该用户已经登陆，放行，用户即可继续调用自己需要的接口  **/
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}
