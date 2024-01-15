package cn.biq.mn.flowfile;

import cn.biq.mn.base.BaseEntity;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_flow_file")
@Getter
@Setter
public class FlowFile extends BaseEntity {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] data;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User creator; //上传人

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id")
    private BalanceFlow flow;

    @Column(nullable = false)
    private Long createTime;

    @Column(length = 32, nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(length = 512, nullable = false)
    private String originalName;

}
