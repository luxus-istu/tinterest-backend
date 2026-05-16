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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService statisticsService;
    private final InterestService interestService;

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsResponseDto> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryResponseDto>> getAllUsers(
            @RequestParam(required = false) String email,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(email, pageable));
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/interests/add")
    public ResponseEntity<InterestResponseDto> addInterest(@Valid @RequestBody AddInterestRequestDto addInterestRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interestService.addInterest(addInterestRequestDto.getName()));
    }

    @DeleteMapping("/interests/delete/{interestId}")
    public ResponseEntity<Void> deleteInterest(@PathVariable Long interestId) {
        interestService.deleteInterest(interestId);
        return ResponseEntity.noContent().build();
    }
}
