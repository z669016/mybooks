package com.putoet.mybooks;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.application.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class MybooksApplication {
	private final UserService userService;
	private final BookInquiryService inquiryService;
	private final BookUpdateService updateService;

	public static void main(String[] args) {
		SpringApplication.run(MybooksApplication.class, args);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.warn("Application context refresh:");
		log.warn("UserService({})", userService);
		log.warn("BookInquiryService({})", inquiryService);
		log.warn("BookUpdateService({})", updateService);
	}
}
