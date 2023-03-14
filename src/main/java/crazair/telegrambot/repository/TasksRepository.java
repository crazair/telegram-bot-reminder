package crazair.telegrambot.repository;

import crazair.telegrambot.model.NotificationTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface TasksRepository extends JpaRepository<NotificationTasks, Long> {
    Collection<NotificationTasks> findAllByDateTime(LocalDateTime dateTime);
}