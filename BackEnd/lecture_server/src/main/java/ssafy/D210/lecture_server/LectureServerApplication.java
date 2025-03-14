package ssafy.D210.lecture_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LectureServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LectureServerApplication.class, args);
	}

}
