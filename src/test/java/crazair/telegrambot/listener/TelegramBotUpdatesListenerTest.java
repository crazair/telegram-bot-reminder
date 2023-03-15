package crazair.telegrambot.listener;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import crazair.telegrambot.repository.TasksRepository;
import crazair.telegrambot.service.NotificationTaskService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    TasksRepository repository;
    @Mock
    NotificationTaskService notificationTaskService;

    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener;

    @Test
    public void handleStartTest() throws URISyntaxException, IOException {
        String json = Files.readString(Paths.get(TelegramBotUpdatesListenerTest.class.getResource("text_update.json").toURI()));
        Update update = getUpdate(json, "/start");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(
                "Привет товарищъ! Ты можешь создать напоминание в формате *01.01.2022 20:00 Сделать домашнюю работу!*");
        Assertions.assertThat(actual.getParameters().get("parse_mode")).isEqualTo(ParseMode.Markdown.name());
    }

    @Test
    public void handleValidMessage() throws URISyntaxException, IOException {
        String json = Files.readString(Paths.get(TelegramBotUpdatesListenerTest.class.getResource("text_update.json").toURI()));
        Update update = getUpdate(json, "31.12.2022 20:00 Сделать домашнюю работу");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> sendMessageArgumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(sendMessageArgumentCaptor.capture());
        SendMessage actualSendMessage = sendMessageArgumentCaptor.getValue();

        ArgumentCaptor<LocalDateTime> localDateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<String> stringTimeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verify(notificationTaskService).addNotificationTask(
                localDateTimeArgumentCaptor.capture(),
                stringTimeArgumentCaptor.capture(),
                longArgumentCaptor.capture()
        );

        LocalDateTime actualLocalDateTime = localDateTimeArgumentCaptor.getValue();
        String actualString = stringTimeArgumentCaptor.getValue();
        Long actualLong = longArgumentCaptor.getValue();

        Assertions.assertThat(actualLocalDateTime)
                .isEqualTo(LocalDateTime.of(2022, Month.DECEMBER, 31, 20, 0));
        Assertions.assertThat(actualString).isEqualTo("Сделать домашнюю работу");
        Assertions.assertThat(actualLong).isEqualTo(123L);

        Assertions.assertThat(actualSendMessage.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actualSendMessage.getParameters().get("text")).isEqualTo("Готово! Напоминание придёт в срок!");
    }

    private Update getUpdate(String json, String replaced) {
        return BotUtils.fromJson(json.replace("%command%", replaced), Update.class);
    }

}
