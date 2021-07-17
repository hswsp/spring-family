package geektime.spring.reactor.webclient;

import geektime.spring.reactor.webclient.model.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class WebclientDemoApplication implements ApplicationRunner {
	@Autowired
	private WebClient webClient;

	public static void main(String[] args) {
		new SpringApplicationBuilder(WebclientDemoApplication.class)
				.web(WebApplicationType.NONE)//禁用自带的tomcat,使用webClient的Builder启动
				.bannerMode(Banner.Mode.OFF)
				.run(args);
	}

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.baseUrl("http://localhost:8080").build();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		CountDownLatch cdl = new CountDownLatch(2);

		webClient.get()
				.uri("/coffee/{id}", 1)//1会替换{id}
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.retrieve()//获取结果
				.bodyToMono(Coffee.class)
				.doOnError(t -> log.error("Error: ", t))
				.doFinally(s -> cdl.countDown())
				.subscribeOn(Schedulers.single())
				.subscribe(c -> log.info("Coffee 1: {}", c));

		Mono<Coffee> americano = Mono.just(
				Coffee.builder()
						.name("americano")
						.price(Money.of(CurrencyUnit.of("CNY"), 25.00))
						.build()
		);
		webClient.post()
				.uri("/coffee/")
				.body(americano, Coffee.class)
				.retrieve()
				.bodyToMono(Coffee.class)
				.doFinally(s -> cdl.countDown())
				.subscribeOn(Schedulers.single())
				.subscribe(c -> log.info("Coffee Created: {}", c));

		cdl.await();

		webClient.get()
				.uri("/coffee/")
				.retrieve()
				.bodyToFlux(Coffee.class)
				.toStream()//这是个block的操作
				.forEach(c -> log.info("Coffee in List: {}", c));
	}
}
