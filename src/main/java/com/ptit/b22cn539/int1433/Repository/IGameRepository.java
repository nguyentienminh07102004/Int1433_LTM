package com.ptit.b22cn539.int1433.Repository;

import com.ptit.b22cn539.int1433.Models.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGameRepository extends JpaRepository<GameEntity, Long> {
}
