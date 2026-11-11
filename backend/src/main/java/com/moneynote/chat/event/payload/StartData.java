package com.moneynote.chat.event.payload;

/** start 事件载荷：agentCode = 菜单选定的 Agent（同时也是消息分区键），恒非空。 */
public record StartData(String model, String agentCode) {

}
