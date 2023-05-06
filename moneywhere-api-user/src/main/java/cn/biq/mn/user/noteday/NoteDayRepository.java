package cn.biq.mn.user.noteday;

import cn.biq.mn.user.user.User;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;

@Repository
public interface NoteDayRepository extends BaseRepository<NoteDay> {

    Optional<NoteDay> findOneByUserAndId(User user, Integer id);

    boolean existsByUserAndTitle(User user, String title);

}
