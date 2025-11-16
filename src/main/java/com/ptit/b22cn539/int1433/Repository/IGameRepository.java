package com.ptit.b22cn539.int1433.Repository;

import com.ptit.b22cn539.int1433.Models.GameEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IGameRepository extends JpaRepository<GameEntity, Long> {
    @Query(value = """
            SELECT username, SUM(scoreUser) AS total_score
            FROM gameEntity
            GROUP BY username
            ORDER BY total_score DESC
            """, nativeQuery = true)
    List<Object[]> topRankingRaw();

    List<GameEntity> findByMatchCode(String matchCode);
}
