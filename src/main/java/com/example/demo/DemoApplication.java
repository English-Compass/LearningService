package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	/**
	 * 애플리케이션 시작 시 자동으로 더미 데이터를 로딩하는 컴포넌트
	 */
	@Slf4j
	@Component
	@RequiredArgsConstructor
	public static class DataLoader implements CommandLineRunner {

		private final JdbcTemplate jdbcTemplate;

		@Override
		public void run(String... args) throws Exception {
			log.info("애플리케이션 시작 시 더미 데이터 자동 로딩 시작...");
			
			try {
				// 1. DDL 스크립트 실행 (테이블 및 뷰 생성)
				executeSqlScript("DB-scripts/init.sql");
				log.info("DDL 스크립트 실행 완료");
				
				// 2. 더미 데이터 스크립트 실행
				executeSqlScript("DB-scripts/init-dummy.sql");
				log.info("더미 데이터 스크립트 실행 완료");
				
				log.info("더미 데이터 자동 로딩 완료!");
				
			} catch (Exception e) {
				log.error("더미 데이터 자동 로딩 중 오류 발생", e);
			}
		}

		/**
		 * SQL 스크립트 파일을 읽어서 실행
		 */
		private void executeSqlScript(String scriptPath) throws Exception {
			ClassPathResource resource = new ClassPathResource(scriptPath);
			
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				
				String script = reader.lines().collect(Collectors.joining("\n"));
				
				// SQL 문을 세미콜론으로 분리하여 실행
				String[] statements = script.split(";");
				
				for (String statement : statements) {
					statement = statement.trim();
					if (!statement.isEmpty() && !statement.startsWith("--")) {
						try {
							jdbcTemplate.execute(statement);
						} catch (Exception e) {
							log.warn("SQL 실행 실패 (무시됨): {}", statement.substring(0, Math.min(50, statement.length())), e);
						}
					}
				}
			}
		}
	}
}
