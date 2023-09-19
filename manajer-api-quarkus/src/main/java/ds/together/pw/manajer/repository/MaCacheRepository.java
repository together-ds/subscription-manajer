package ds.together.pw.manajer.repository;

import ds.together.pw.manajer.entity.po.MaCache;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaCacheRepository implements PanacheRepository<MaCache> {
}
