package cn.biq.mn.user.tagrelation;

import cn.biq.mn.user.tag.Tag;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.List;


@Repository
public interface TagRelationRepository extends BaseRepository<TagRelation> {

    boolean existsByTagIn(List<Tag> tags);

    boolean existsByTag(Tag tag);

}
