package crazair.telegrambot.service;

import crazair.telegrambot.listener.TelegramBotUpdatesListener;
import crazair.telegrambot.model.NotificationTasks;
import crazair.telegrambot.repository.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationTaskService {

    private final Logger LOG = LoggerFactory.getLogger(NotificationTaskService.class);

    @Autowired
    private TasksRepository tasksRepository;

    @Transactional
    public void addNotificationTask(LocalDateTime localDateTime, String message, Long userId) {
        NotificationTasks notificationTask = new NotificationTasks();
        notificationTask.setDateTime(localDateTime);
        notificationTask.setDescription(message);
        notificationTask.setChatId(userId);

        tasksRepository.save(notificationTask);
        LOG.info("Save Task" + notificationTask);
    }

}