package com.integration.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class IntegrationMonitorApplication {
/* 
    @Bean
    CommandLineRunner init(IntegrationProcessService service) {
        return args -> {
            service.registerProcess(
                    new IntegrationProcess(
                            1L,
                            "SINPE Service",
                            ProcessType.OSB,
                            ProcessStatus.RUNNING
                    )
            );

            service.registerProcess(
                    new IntegrationProcess(
                            2L,
                            "Payment API",
                            ProcessType.REST_API,
                            ProcessStatus.SUCCESS
                    )
            );
        };
    }
*/
    public static void main(String[] args) {
        SpringApplication.run(IntegrationMonitorApplication.class, args);
    }
}
