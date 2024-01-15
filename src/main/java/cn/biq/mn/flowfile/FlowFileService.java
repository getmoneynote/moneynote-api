package cn.biq.mn.flowfile;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FlowFileService {

    private final FlowFileRepository flowFileRepository;
    private final SessionUtil sessionUtil;
    private final BaseService baseService;

    @Transactional(readOnly = true)
    public FlowFile getFile(FlowFileViewForm form) {
        FlowFile flowFile =  flowFileRepository.getReferenceById(form.getId());
        if (!flowFile.getCreateTime().equals(form.getCreateTime())) {
            throw new FailureMessageException();
        }
        return flowFile;
    }

    public boolean remove(Integer id) {
        FlowFile flowFile =  flowFileRepository.getReferenceById(id);
        BalanceFlow flow = baseService.findFlowById(flowFile.getFlow().getId());
        if (flow == null) {
            throw new FailureMessageException("auth.error");
        }
        flowFileRepository.delete(flowFile);
        return true;
    }

}
