package crazair.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import crazair.telegrambot.model.NotificationTasks;
import crazair.telegrambot.repository.TasksRepository;
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

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final static Pattern PATTERN_MESS = Pattern.compile("([\\d.:\\s]{16})(\\s)([\\W+]+)");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    TasksRepository repository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                long chatId = update.message().chat().id();
                String text = update.message().text();
                Matcher matcher = PATTERN_MESS.matcher(text);
                if ("/start".equals(text)) {
                    telegramBot.execute(new SendMessage(chatId, "Привет товарищъ!"));
                    telegramBot.execute(new SendMessage(chatId,
                            "Ты можешь создать напоминание в формате '01.01.2022 20:00 Сделать домашнюю работу!'"));
                } else if (matcher.matches()) {
                    telegramBot.execute(new SendMessage(chatId, "Добавляем напоминание..."));

                    saveTask(matcher.group(1), matcher.group(3), update.message().chat().id());

                    telegramBot.execute(new SendMessage(chatId, "Готово! Напоминание придёт в срок!"));
                } else {
                    telegramBot.execute(new SendMessage(update.message().chat().id(), "Неверно пишешь товарищъ!"));
                }
            });
        } catch (RuntimeException e) {
            logger.error("Ошибка при ответе!", e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void saveTask(String dateTime, String description, long chatId) {
        NotificationTasks task = new NotificationTasks();
        task.setDateTime(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        task.setDescription(description);
        task.setChatId(chatId);
        repository.save(task);
    }

}
