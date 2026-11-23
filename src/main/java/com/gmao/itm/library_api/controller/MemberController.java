package com.gmao.itm.library_api.controller;

import com.gmao.itm.library_api.dto.MemberRequest;
import com.gmao.itm.library_api.dto.MemberResponse;
import com.gmao.itm.library_api.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Operations for managing library members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "List all members", description = "Returns all registered library members.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAll() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @Operation(summary = "Get member by ID", description = "Returns a member by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> findById(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @Operation(summary = "Create a member", description = "Registers a new library member.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate email"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<MemberResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "fullName": "Elena Morales",
                                              "email": "elena.morales@library.edu.mx",
                                              "phone": "999-234-5678",
                                              "registrationDate": "2026-11-22",
                                              "active": true
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody MemberRequest request
    ) {
        MemberResponse created = memberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a member", description = "Updates member details and active status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member updated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate email"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody MemberRequest request
    ) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @Operation(summary = "Delete a member", description = "Deletes a member by ID (requires ROLE_ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "409", description = "Member has existing loans")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id
    ) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}