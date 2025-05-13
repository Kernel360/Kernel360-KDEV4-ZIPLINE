package com.zipline.service.migration;

import java.util.concurrent.CompletableFuture;

import com.zipline.global.exception.migration.MigrationException;
import com.zipline.global.exception.migration.errorcode.MigrationErrorCode;
import com.zipline.global.exception.task.TaskException;
import com.zipline.global.exception.task.errorcode.TaskErrorCode;
import com.zipline.service.task.Task;
import com.zipline.service.task.TaskDefinition;
import com.zipline.service.task.TaskExecutionHandler;
import com.zipline.service.task.TaskManager;
import com.zipline.service.task.dto.TaskResponseDto;
import com.zipline.service.task.enums.TaskType;
import com.zipline.infrastructure.naver.NaverRawArticleRepository;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverMigrationServiceImpl implements NaverMigrationService {

	private final TaskManager taskManager;
	private final NaverRawArticleRepository naverRawArticleRepository;
	private final TaskExecutionHandler taskExecutionHandler;

	@Override
	public TaskResponseDto startFullMigration() {
		return taskExecutionHandler.execute(
				TaskDefinition.of(
						TaskType.MIGRATION,
						"전체 지역 마이그레이션",
						() -> executeFullMigrationAsync())
		);
	}

	@Override
	public TaskResponseDto migrateRegion(Long regionId) {
		return taskExecutionHandler.execute(
				TaskDefinition.of(
						TaskType.MIGRATION,
						"특정 지역 마이그레이션",
						() -> executeRegionMigrationAsync(regionId))
		);
	}

	@Override
	public TaskResponseDto retryFailedMigrations() {
		return taskExecutionHandler.execute(
				TaskDefinition.of(
						TaskType.MIGRATION,
						"실패한 마이그레이션 재시도",
						() -> executeRetryFailedMigrationsAsync())
		);
	}

	@Override
	public TaskResponseDto getTaskStatus(TaskType taskName) {
		Task task = taskManager.getTaskByType(taskName);
		return TaskResponseDto.fromTask(task);
	}

	private void executeFullMigrationAsync() {
		log.info("=== 전체 지역 마이그레이션 시작 (저장 프로시저 호출) ===");
		try {
			naverRawArticleRepository.migrateAllPendingArticles();
			log.info("전체 마이그레이션 성공적으로 완료");
		} catch (Exception e) {
			log.error("전체 마이그레이션 실패: {}", e.getMessage(), e);
			throw new MigrationException(MigrationErrorCode.MIGRATION_FAILED);
		} finally {
			taskManager.removeTask(TaskType.MIGRATION);
		}
	}

	private void executeRegionMigrationAsync(Long regionId) {
		log.info("지역 코드 {} 마이그레이션 시작 (저장 프로시저 호출)", regionId);
		try {
			naverRawArticleRepository.migrateArticlesByRegion(regionId);
			log.info("지역 코드 {} 마이그레이션 완료", regionId);
		} catch (Exception e) {
			log.error("지역 코드 {} 마이그레이션 실패: {}", regionId, e.getMessage(), e);
			throw new MigrationException(MigrationErrorCode.MIGRATION_FAILED);
		} finally {
			taskManager.removeTask(TaskType.MIGRATION);
		}
	}

	private void executeRetryFailedMigrationsAsync() {
		log.info("실패한 마이그레이션 재시도 시작");
		try {
			naverRawArticleRepository.retryFailedMigrations();
			log.info("실패한 마이그레이션 재시도 완료");
		} catch (Exception e) {
			log.error("실패한 마이그레이션 재시도 실패: {}", e.getMessage(), e);
			throw new MigrationException(MigrationErrorCode.MIGRATION_FAILED);
		} finally {
			taskManager.removeTask(TaskType.MIGRATION);
		}
	}
}