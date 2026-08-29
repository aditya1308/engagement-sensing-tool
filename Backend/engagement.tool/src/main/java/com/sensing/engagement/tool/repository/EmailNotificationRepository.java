package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.model.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailNotificationRepository
        extends JpaRepository<EmailNotification, UUID> {

}