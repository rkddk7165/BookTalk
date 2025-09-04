package myproject.booktalk.user.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {
            String target = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null) target += "?" + query;
            String redirect = "/login?redirect=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
            response.sendRedirect(redirect);
            return false;
        }
        return true;
    }
}
