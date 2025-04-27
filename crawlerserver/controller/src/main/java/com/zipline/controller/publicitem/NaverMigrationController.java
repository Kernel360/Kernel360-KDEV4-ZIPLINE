package com.zipline.controller.publicitem;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
//TODO: 임포트 분리
import org.springframework.web.bind.annotation.*;

import com.zipline.global.response.ApiResponse;
import com.zipline.global.util.CrawlingStatusManager;
import com.zipline.service.publicItem.NaverRawArticleMigrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/NaverMigration")
@RequiredArgsConstructor
public class NaverMigrationController {

	private final NaverRawArticleMigrationService migrationService;
	private final CrawlingStatusManager crawlingStatusManager;

	@PostMapping("/start")
	public ResponseEntity<ApiResponse<Void>> startMigration() {
		CompletableFuture.runAsync(() -> {
			crawlingStatusManager.executeWithLock(() -> {
				migrationService.NaverMigration();
				return null;
			});
		});
		return ResponseEntity.ok(ApiResponse.ok("네이버 원본 매물 데이터 마이그레이션이 시작되었습니다."));
	}

	@PostMapping("/region/{cortarNo}")
	public ResponseEntity<ApiResponse<Void>> migrateRegion(@PathVariable Long cortarNo) {
		CompletableFuture.runAsync(() -> {
			crawlingStatusManager.executeWithLock(() -> {
				migrationService.migrateRawArticlesForRegion(cortarNo);
				return null;
			});
		});
		return ResponseEntity.ok(ApiResponse.ok("지역 " + cortarNo + " 네이버 원본 매물 데이터 마이그레이션이 시작되었습니다."));
	}

	@PostMapping("/retry")
	public ResponseEntity<ApiResponse<Void>> retryFailedMigrations() {
		CompletableFuture.runAsync(() -> {
			crawlingStatusManager.executeWithLock(() -> {
				migrationService.retryFailedMigrations();
				return null;
			});
		});
		return ResponseEntity.ok(ApiResponse.ok("실패한 마이그레이션 재시도가 시작되었습니다."));
	}
}
