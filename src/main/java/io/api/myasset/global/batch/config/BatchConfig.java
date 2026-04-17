package io.api.myasset.global.batch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Spring Batch 공통 설정.
 * <p>
 * 기본 {@link JobOperator} 를 비동기 Executor 기반으로 재등록한다.
 * HTTP 요청 스레드(신규 유저 연동 트리거)나 스케줄러 스레드가 Job 실행을 기다리지 않도록 분리한다.
 * <p>
 * Spring Batch 6 에서 {@code JobLauncher} 가 deprecated 되고 {@code JobOperator} 로 통합되었다.
 * {@link JobRegistry} 는 Spring Boot 자동 구성에 포함되지 않으므로 명시적으로 선언한다.
 */
@Configuration
public class BatchConfig {

	@Bean
	public TaskExecutor batchTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("codef-sync-");
		executor.setConcurrencyLimit(4);
		return executor;
	}

	/**
	 * Spring Batch 6 의 {@link MapJobRegistry} 는 {@code SmartInitializingSingleton} +
	 * {@code ApplicationContextAware} 를 직접 구현하여 컨텍스트 내 모든 {@code Job} Bean 을
	 * {@code afterSingletonsInstantiated} 시점에 자동 등록한다.
	 * 따라서 빈 인스턴스만 등록하고 수동 register 를 호출하지 않는다 (중복 등록 방지).
	 */
	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public JobOperator asyncJobOperator(
		JobRepository jobRepository,
		JobRegistry jobRegistry,
		TaskExecutor batchTaskExecutor) throws Exception {
		TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
		operator.setJobRepository(jobRepository);
		operator.setJobRegistry(jobRegistry);
		operator.setTaskExecutor(batchTaskExecutor);
		operator.afterPropertiesSet();
		return operator;
	}
}
