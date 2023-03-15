package crazair.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import crazair.telegrambot.model.NotificationTasks;
import crazair.telegrambot.repository.TasksRepository;
import crazair.telegrambot.service.NotificationTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger LOG = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final static Pattern PATTERN_MESS = Pattern.compile("([\\d.:\\s]{16})(\\s)([\\W+]+)");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    TasksRepository repository;

    @Autowired
    NotificationTaskService notificationTaskService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                LOG.info("Processing update: {}", update);

                if (update.message() == null) {
                    return;
                }

                long chatId = update.message().chat().id();
                String text = update.message().text();
                Matcher matcher = PATTERN_MESS.matcher(text);

                if ("/start".equals(text)) {
                    SendMessage message = new SendMessage(chatId,
                            "Привет товарищъ! Ты можешь создать напоминание в формате *01.01.2022 20:00 Сделать домашнюю работу!*");
                    message.parseMode(ParseMode.Markdown);

                    telegramBot.execute(message);
                } else if (matcher.matches()) {
                    LocalDateTime localDateTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    notificationTaskService.addNotificationTask(localDateTime, matcher.group(3), chatId);

                    telegramBot.execute(new SendMessage(chatId, "Готово! Напоминание придёт в срок!"));
                } else {
                    telegramBot.execute(new SendMessage(chatId, "Неверно пишешь товарищъ!"));
                }
            });
        } catch (RuntimeException e) {
            LOG.error("Ошибка при ответе!", e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
