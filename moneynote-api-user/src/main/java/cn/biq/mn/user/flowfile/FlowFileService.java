package cn.biq.mn.user.flowfile;

import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.user.balanceflow.BalanceFlow;
import cn.biq.mn.user.base.BaseService;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.utils.SessionUtil;
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
