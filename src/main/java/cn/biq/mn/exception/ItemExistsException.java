package cn.biq.mn.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ItemExistsException extends RuntimeException {

    public ItemExistsException(String message) {
        super(message);
    }

}
