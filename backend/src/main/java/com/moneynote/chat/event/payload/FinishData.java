package com.moneynote.chat.event.payload;

/** finish 事件载荷：唯一成功终态，发出前已确保落库完成。 */
public record FinishData(String finishReason) {

}
