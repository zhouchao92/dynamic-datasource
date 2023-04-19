package com.zhou.util;

import lombok.Getter;
import lombok.Setter;

/**
 * HeadThreadLocal
 *
 * @author 周超
 * @since 2022/6/18 14:02
 */
@Setter
@Getter
public class HeadThreadLocal {

    private HeadThreadLocal() {
    }

    private static final ThreadLocal<HeadThreadLocal> THREAD_LOCAL = ThreadLocal.withInitial(HeadThreadLocal::new);

    public static HeadThreadLocal getInstance() {
        return THREAD_LOCAL.get();
    }

    public void remove() {
        THREAD_LOCAL.remove();
    }

    private Integer dataId;

}
