package org.router;

import org.example.PetitionEndpoint;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class SPARouter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        final Logger logger = Logger.getLogger(SPARouter.class.getName());
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();
        logger.info("SPA Filter, path=" + path);

        if (
                !path.startsWith("/api") &&
                        !path.startsWith("/_ah/api") &&
                        !path.equals("/") &&
                        !path.equals("/index.html") &&
                        !path.matches(".*\\.[a-zA-Z0-9]+$") &&
                        !path.startsWith("/assets")
        ) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}