package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {

    Optional<Theme> findByNameIgnoreCase(String name);
}