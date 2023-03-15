package crazair.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import crazair.telegrambot.model.NotificationTasks;
import crazair.telegrambot.repository.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Component
public class SchedulerTasks {

    private final Logger LOG = LoggerFactory.getLogger(SchedulerTasks.class);

    @Autowired
    private TasksRepository repository;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "0 0/1 * * * *") //Запуск в 00 секунд каждой минуты
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LOG.info("Run SchedulerTasks in " + localDateTime);
        Collection<NotificationTasks> tasks = repository.findAllByDateTime(localDateTime.plusMinutes(1));
        if (!tasks.isEmpty()) {
            LOG.info("sendNotification!");
            tasks.forEach(task -> {
                telegramBot.execute(new SendMessage(task.getChatId(), task.getDateTime().
                        format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " " + task.getDescription()));
                LOG.info("notificationTask: " + task);
                repository.delete(task);
            });
        }
    }

}