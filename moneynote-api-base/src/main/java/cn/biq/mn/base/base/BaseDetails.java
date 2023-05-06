package cn.biq.mn.base.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 将id抽出来，方便以后更改id类型
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDetails {

    private Integer id;

}
