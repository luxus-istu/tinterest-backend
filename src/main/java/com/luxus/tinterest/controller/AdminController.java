package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.admin.AddInterestRequestDto;
import com.luxus.tinterest.dto.admin.AdminStatisticsResponseDto;
import com.luxus.tinterest.dto.admin.UserSummaryResponseDto;
import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.service.AdminStatisticsService;
import com.luxus.tinterest.service.InterestService;
import com.luxus.tinterest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService statisticsService;
    private final InterestService interestService;

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsResponseDto> getStatistics() {
        log.info("Admin requested system statistics");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryResponseDto>> getAllUsers(
            @RequestParam(required = false) String email,
            Pageable pageable) {
        log.info("Admin requested user list, email filter: {}, pageable: {}", email, pageable);
        return ResponseEntity.ok(userService.getAllUsers(email, pageable));
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        log.info("Admin blocking user ID: {}", userId);
        userService.blockUser(userId);
        log.info("User ID: {} blocked successfully", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        log.info("Admin unblocking user ID: {}", userId);
        userService.unblockUser(userId);
        log.info("User ID: {} unblocked successfully", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/interests/add")
    public ResponseEntity<InterestResponseDto> addInterest(@Valid @RequestBody AddInterestRequestDto addInterestRequestDto) {
        log.info("Admin adding new interest: {}", addInterestRequestDto.getName());
        InterestResponseDto response = interestService.addInterest(addInterestRequestDto.getName());
        log.info("New interest added: {} (ID: {})", response.name(), response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/interests/delete/{interestId}")
    public ResponseEntity<Void> deleteInterest(@PathVariable Long interestId) {
        log.info("Admin deleting interest ID: {}", interestId);
        interestService.deleteInterest(interestId);
        log.info("Interest ID: {} deleted successfully", interestId);
        return ResponseEntity.noContent().build();
    }
}
