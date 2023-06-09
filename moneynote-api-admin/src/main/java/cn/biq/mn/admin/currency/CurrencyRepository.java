package cn.biq.mn.admin.currency;

import cn.biq.mn.base.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends BaseRepository<Currency> {

    List<Currency> findAllByEnable(Boolean enable);

}
