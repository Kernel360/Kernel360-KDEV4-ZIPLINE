package com.zipline.controller.migration;

import com.zipline.global.response.ApiResponse;
import com.zipline.service.task.dto.TaskResponseDto;
import com.zipline.service.task.enums.TaskType;
import com.zipline.service.migration.NaverMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/migration/naver")
@RequiredArgsConstructor
public class NaverMigrationController {
	private final NaverMigrationService migrationService;

	@GetMapping
	public ResponseEntity<ApiResponse<TaskResponseDto>> startMigration() {
		TaskResponseDto result = migrationService.startFullMigration();
		ApiResponse<TaskResponseDto> response = ApiResponse.ok("네이버 원본 매물 데이터 마이그레이션 시작", result);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/region/{cortarNo}")
	public ResponseEntity<ApiResponse<TaskResponseDto>> migrateRegion(@PathVariable Long cortarNo) {
		TaskResponseDto result = migrationService.migrateRegion(cortarNo);
		ApiResponse<TaskResponseDto> response = ApiResponse.ok("네이버 지역번호" + cortarNo + "마이그레이션 시작",result);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/retry/")
	public ResponseEntity<ApiResponse<TaskResponseDto>> retryFailedMigration() {
		TaskResponseDto result = migrationService.retryFailedMigrations();
		ApiResponse<TaskResponseDto> response = ApiResponse.ok("실패한 네이버 마이그레이션 재시도",result);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/status/{taskType}")
	public ResponseEntity<ApiResponse<TaskResponseDto>> getTaskStatus(@PathVariable TaskType taskType) {
		TaskResponseDto result = migrationService.getTaskStatus(taskType);
		ApiResponse<TaskResponseDto> response = ApiResponse.ok("마이그레이션 상태 조회",result);
		return ResponseEntity.ok(response);
	}
}