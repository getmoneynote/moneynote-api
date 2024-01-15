package cn.biq.mn.tagrelation;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.tag.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TagRelationRepository extends BaseRepository<TagRelation> {

    boolean existsByTagIn(List<Tag> tags);

    boolean existsByTag(Tag tag);

}
