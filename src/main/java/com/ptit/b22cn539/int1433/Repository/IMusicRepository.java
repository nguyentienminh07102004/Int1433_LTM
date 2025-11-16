package com.ptit.b22cn539.int1433.Repository;

import com.ptit.b22cn539.int1433.Models.MusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMusicRepository extends JpaRepository<MusicEntity, Long> {
    @Query(value = "SELECT * FROM MusicEntity ORDER BY RANDOM() LIMIT 10", nativeQuery = true)
    List<MusicEntity> findRandom10Music();
}
