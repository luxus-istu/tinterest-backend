package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.admin.UserSummaryResponseDto;
import com.luxus.tinterest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

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
}
