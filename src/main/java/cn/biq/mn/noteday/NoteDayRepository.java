package cn.biq.mn.noteday;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.user.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteDayRepository extends BaseRepository<NoteDay> {

    Optional<NoteDay> findOneByUserAndId(User user, Integer id);

    boolean existsByUserAndTitle(User user, String title);

}
