package cn.biq.mn.user.currency;

import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CurrencyRepository extends BaseRepository<Currency> {

    Optional<Currency> findOneByName(String name);

    List<Currency> findAllByEnable(Boolean enable);

}
