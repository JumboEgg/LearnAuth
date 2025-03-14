package ssafy.D210.report_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReportServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportServerApplication.class, args);
	}

}
