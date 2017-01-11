package com.app.superxlcr.mypaintboard.tools;

import com.app.superxlcr.mypaintboard.model.Protocol;

/**
 * Created by superxlcr on 2017/1/6.
 * 信息监听器
 */

public interface ProtocolListener {

    /**
     * 接收消息的回调方法
     * @param protocol 协议信息
     * @return 是否处理信息完毕，false则继续传递消息
     */
    boolean onReceive(Protocol protocol);
}
