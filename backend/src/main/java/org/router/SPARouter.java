package org.router;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class SPARouter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        // Si la requête n'est pas pour une ressource statique ni une API
        if (!path.startsWith("/api") && !path.contains(".") && !path.startsWith("/assets")) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}