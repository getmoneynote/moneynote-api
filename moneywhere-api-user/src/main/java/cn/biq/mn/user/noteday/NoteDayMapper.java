package cn.biq.mn.user.noteday;

import cn.biq.mn.user.utils.EnumUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import cn.biq.mn.base.utils.MessageSourceUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class NoteDayMapper {

    private final MessageSourceUtil messageSourceUtil;
    private final EnumUtils enumUtils;

    public static NoteDay toEntity(NoteDayAddForm form) {
        NoteDay noteDay = new NoteDay();
        noteDay.setTitle( form.getTitle() );
        noteDay.setNotes( form.getNotes() );
        noteDay.setStartDate( form.getStartDate() );
        noteDay.setEndDate( form.getEndDate() );
        noteDay.setRepeatType( form.getRepeatType() );
        noteDay.setInterval( form.getInterval() );
        return noteDay;
    }

    public static void updateEntity(NoteDayUpdateForm form, NoteDay noteDay) {
        if (form.getRepeatType() != null) {
            noteDay.setRepeatType( form.getRepeatType() );
        }
        if (StringUtils.hasText(form.getTitle())) {
            noteDay.setTitle( form.getTitle() );
        }
        noteDay.setNotes( form.getNotes() );
        if (form.getStartDate() != null) {
            noteDay.setStartDate( form.getStartDate() );
        }
        noteDay.setEndDate( form.getEndDate() );
        noteDay.setInterval( form.getInterval() );
    }

    public NoteDayDetails toDetails(NoteDay entity) {
        if (entity == null) return null;
        var details = new NoteDayDetails();
        details.setRepeatDescription( repeatDescription( entity ) );
        details.setCountDown( countDown( entity ) );
        details.setId( entity.getId() );
        details.setTitle( entity.getTitle() );
        details.setNotes( entity.getNotes() );
        details.setStartDate( entity.getStartDate() );
        details.setEndDate( entity.getEndDate() );
        details.setRepeatType( entity.getRepeatType() );
        details.setInterval( entity.getInterval() );
        details.setNextDate( entity.getNextDate() );
        details.setTotalCount( entity.getTotalCount() );
        details.setRunCount( entity.getRunCount() );
        details.setRemainCount( entity.getTotalCount() - entity.getRunCount() );
        return details;
    }

    private String repeatDescription(NoteDay entity) {
        if (entity.getRepeatType() == 0) {
            return messageSourceUtil.getMessage("note.day.run.once");
        } else {
            return messageSourceUtil.getMessage(
                    "note.day.run.interval",
                    new Object[] {
                            entity.getInterval() != 1 ? entity.getInterval() : "",
                            enumUtils.translateNoteDayRepeatType(entity.getRepeatType())
                    }
            );
        }
    }

    private long countDown(NoteDay entity) {
        LocalDate now = LocalDate.now();
        LocalDate nextDate = Instant.ofEpochMilli(entity.getNextDate()).atZone(ZoneId.systemDefault()).toLocalDate();
        return ChronoUnit.DAYS.between(
                LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()),
                LocalDate.of(nextDate.getYear(), nextDate.getMonth(), nextDate.getDayOfMonth())
        );
    }

}
