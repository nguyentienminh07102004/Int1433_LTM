package com.ptit.b22cn539.int1433.Repository;

import com.ptit.b22cn539.int1433.Models.SessionUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISessionUserRepository extends JpaRepository<SessionUserEntity, Long> {
    SessionUserEntity findBySessionId(String sessionId);
    SessionUserEntity findByUsername(String username);

    void deleteByUsername(String username);
}
