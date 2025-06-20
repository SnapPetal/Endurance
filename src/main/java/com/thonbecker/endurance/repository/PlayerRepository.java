package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.PlayerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {

    Optional<PlayerEntity> findByName(String name);

    List<PlayerEntity> findByNameContainingIgnoreCase(String namePart);

    boolean existsByName(String name);
}
