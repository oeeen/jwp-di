package nextstep.di.factory;

import com.google.common.collect.Maps;
import nextstep.di.factory.example.MyJdbcTemplate;
import nextstep.di.factory.example.MyQnaService;
import nextstep.di.factory.example.QnaController;
import nextstep.di.factory.example.QnaController2;
import org.apache.commons.dbcp2.BasicDataSource;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactoryTest.class);

    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        beanFactory = new BeanFactory("nextstep.di.factory.example");
        beanFactory.initialize();
    }

    @Test
    public void di() {
        QnaController qnaController = beanFactory.getBean(QnaController.class);

        assertNotNull(qnaController);
        assertNotNull(qnaController.getQnaService());

        MyQnaService qnaService = qnaController.getQnaService();
        assertNotNull(qnaService.getUserRepository());
        assertNotNull(qnaService.getQuestionRepository());
    }

    @Test
    void getController() {
        Set<Class<?>> controllers = beanFactory.getControllers();
        Set<Class<?>> expected = Sets.newHashSet();
        expected.add(QnaController.class);
        expected.add(QnaController2.class);
        assertThat(controllers.size()).isEqualTo(2);
        assertThat(controllers).isEqualTo(expected);
    }

    @Test
    @DisplayName("Configuration annotation bean 등록 확인")
    void getConfig() {
        BasicDataSource dataSource = (BasicDataSource)beanFactory.getBean(DataSource.class);
        String userName = "sa";
        assertThat(dataSource.getUsername()).isEqualTo(userName);

        MyJdbcTemplate myJdbcTemplate = beanFactory.getBean(MyJdbcTemplate.class);
        assertThat(myJdbcTemplate.getDataSource()).isEqualTo(dataSource);
    }
}
