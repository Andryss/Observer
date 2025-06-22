package ru.andryss.observer.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class DisclaimerCommandExecutor extends AbstractCommandExecutor {

    @SuppressWarnings("checkstyle:LineLength")
    private static final String DEFAULT_DISCLAIMER = """
            Я никоим образом НЕ стремлюсь кого-либо оскорбить, задеть или унизить. Мои высказывания — это исключительно выражение моего личного субъективного мнения, которое НЕ претендует на истину в последней инстанции и НЕ направлено против каких-либо людей, групп или сообществ.
            Я искренне уважаю труд всех людей — независимо от национальности, цвета кожи, вероисповедания, пола, гендерной идентичности, сексуальной ориентации, возраста, внешности, социального статуса или места проживания. Я глубоко убеждён, что каждый человек и его деятельность важны для функционирования и развития общества. Любой труд — физический или умственный, творческий или технический — достоин признания и уважения.
            Я также понимаю, что у каждого — свой путь, опыт, культура и ценности, и именно в этом заключается богатство и сложность человеческого общества. Я НЕ считаю себя вправе судить или обесценивать чужой взгляд, а лишь делюсь собственным, сформированным под влиянием личного опыта, окружения и восприятия мира.
            Прошу воспринимать мои слова НЕ как категоричные утверждения, а как открытый и уважительный взгляд на происходящее с моей позиции. Если мои формулировки вызывают непонимание или кажутся резкими — прошу прощения и открыт к диалогу.
            """;

    private final KeyStorageService keyStorageService;

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/disclaimer");

    @Override
    public boolean isActive() {
        return keyStorageService.get("disclaimerCommandExecutor.active", false);
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        String disclaimer = keyStorageService.get("disclaimer.text", DEFAULT_DISCLAIMER);
        sender.execute(new SendMessage(update.getMessage().getChatId().toString(), disclaimer));
    }
}
