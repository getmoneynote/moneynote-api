package cn.biq.mn.user.noteday;


import cn.biq.mn.user.base.BaseService;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemExistsException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteDayService {

    private final SessionUtil sessionUtil;
    private final NoteDayRepository noteDayRepository;
    private final NoteDayMapper noteDayMapper;
    private final BaseService baseService;

    // 计算总执行次数
    private void calTotalCount(NoteDay noteDay) {
        LocalDate startDate = Instant.ofEpochMilli(noteDay.getStartDate()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(noteDay.getEndDate()).atZone(ZoneId.systemDefault()).toLocalDate();
        int totalCount = 0;
        switch (noteDay.getRepeatType()) {
            case 1 -> {
                Long days = ChronoUnit.DAYS.between(
                        LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth()),
                        LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth())
                );
                totalCount = (int) (days / noteDay.getInterval());
            }
            case 2 -> {
                Long months = ChronoUnit.MONTHS.between(
                        LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth()),
                        LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth())
                );
                totalCount = (int) (months / noteDay.getInterval());
            }
            case 3 -> {
                Long years = ChronoUnit.YEARS.between(
                        LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth()),
                        LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth())
                );
                totalCount = (int) (years / noteDay.getInterval());
            }
        }
        noteDay.setTotalCount(totalCount + 1);
    }

    public boolean add(NoteDayAddForm form) {
        User user = sessionUtil.getCurrentUser();
        if (noteDayRepository.existsByUserAndTitle(user, form.getTitle())) {
            throw new ItemExistsException();
        }
        NoteDay entity = NoteDayMapper.toEntity(form);
        entity.setUser(user);
        entity.setNextDate(form.getStartDate());
        entity.setRunCount(0);
        if (form.getRepeatType() == 0) {
            entity.setTotalCount(1);
            entity.setEndDate(form.getStartDate());
        } else {
            calTotalCount(entity);
        }
        noteDayRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public Page<NoteDayDetails> query(NoteDayQueryForm form, Pageable page) {
        User user = sessionUtil.getCurrentUser();
        Page<NoteDay> entityPage = noteDayRepository.findAll(form.buildPredicate(user), page);
        return entityPage.map(noteDayMapper::toDetails);
    }

    public boolean update(Integer id, NoteDayUpdateForm form) {
        User user = sessionUtil.getCurrentUser();
        NoteDay entity = baseService.findNoteDayById(id);
        if (!Objects.equals(entity.getTitle(), form.getTitle())) {
            if (StringUtils.hasText(form.getTitle())) {
                if (noteDayRepository.existsByUserAndTitle(user, form.getTitle())) {
                    throw new ItemExistsException();
                }
            }
        }
        if (form.getRepeatType() != null) {
            // 非单次该为单次，不允许。
            if (entity.getRepeatType() != 0 && form.getRepeatType() == 0) {
                throw new FailureMessageException("valid.fail");
            }
            // 单次该为非单次，不允许。
            if (entity.getRepeatType() == 0 && form.getRepeatType() != 0) {
                throw new FailureMessageException("valid.fail");
            }
        }
        NoteDayMapper.updateEntity(form, entity);
        if (entity.getRepeatType() == 0) {
            entity.setTotalCount(1);
            entity.setEndDate(entity.getStartDate());
            entity.setNextDate(form.getStartDate());
        } else {
            calTotalCount(entity);
        }
        noteDayRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        NoteDay entity = baseService.findNoteDayById(id);
        noteDayRepository.delete(entity);
        return true;
    }

    public boolean run(Integer id) {
        NoteDay entity = baseService.findNoteDayById(id);
        Calendar nextDate = Calendar.getInstance();
        nextDate.setTimeInMillis(entity.getNextDate());
        switch (entity.getRepeatType()) {
            case 1 -> nextDate.add(Calendar.DATE, entity.getInterval());
            case 2 -> nextDate.add(Calendar.MONTH, entity.getInterval());
            case 3 -> nextDate.add(Calendar.YEAR, entity.getInterval());
        }
        entity.setNextDate(nextDate.getTimeInMillis());
        entity.setRunCount(entity.getRunCount() + 1);
        noteDayRepository.save(entity);
        return true;
    }

    public boolean recall(Integer id) {
        NoteDay entity = baseService.findNoteDayById(id);
        Calendar nextDate = Calendar.getInstance();
        nextDate.setTimeInMillis(entity.getNextDate());
        switch (entity.getRepeatType()) {
            case 1 -> nextDate.add(Calendar.DATE, entity.getInterval() * (-1));
            case 2 -> nextDate.add(Calendar.MONTH, entity.getInterval() * (-1));
            case 3 -> nextDate.add(Calendar.YEAR, entity.getInterval() * (-1));
        }
        entity.setNextDate(nextDate.getTimeInMillis());
        entity.setRunCount(entity.getRunCount() - 1);
        noteDayRepository.save(entity);
        return true;
    }

}
