package xin.rexy.docubrain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication
@MapperScan("xin.rexy.docubrain.mapper")
public class DocuBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocuBrainApplication.class, args);
    }

}
