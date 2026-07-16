package edu.cit.mabini.meditrack.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/nurses")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllNurses() {
        return ResponseEntity.ok(userService.getAllNurses());
    }

    @PostMapping("/nurses")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<NurseDto> createNurse(@Valid @RequestBody RegisterNurseRequest request) {
        return ResponseEntity.status(201).body(userService.createNurse(request));
    }

    @PutMapping("/nurses/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<NurseDto> updateNurse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNurseRequest request) {
        return ResponseEntity.ok(userService.updateNurse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

