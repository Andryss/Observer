package ru.andryss.observer;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.andryss.observer.executor.TestClockConfig;

@SpringBootTest
@ActiveProfiles("functionalTest")
@AutoConfigureEmbeddedDatabase(
        refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD,
        type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES,
        provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY
)
@Import({MockBeansConfig.class, TestClockConfig.class})
public abstract class BaseDbTest {
}
