package cn.biq.mn.base.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Violation {

    private String fieldName;

    private String message;

}
