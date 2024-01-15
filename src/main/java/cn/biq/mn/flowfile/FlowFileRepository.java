package cn.biq.mn.flowfile;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.balanceflow.BalanceFlow;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowFileRepository extends BaseRepository<FlowFile> {

    List<FlowFile> findByFlow(BalanceFlow flow);

    void deleteByFlow(BalanceFlow flow);

}
