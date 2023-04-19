package com.zhou.filter;

import com.zhou.util.HeadThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class ServletTraceInfoAttachmentFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String[] sources = request.getParameterMap().get("source");
            if (sources == null || sources.length == 0) {
                return;
            }
            HeadThreadLocal.getInstance().setDataId(Integer.valueOf(sources[0]));

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.info("error message ==> {}", e.getMessage(), e);
        } finally {
            HeadThreadLocal.getInstance().remove();
        }
    }

    @Override
    public void destroy() {

    }

}
