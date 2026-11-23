package com.gmao.itm.library_api.controller;

import com.gmao.itm.library_api.dto.LoanRequest;
import com.gmao.itm.library_api.dto.LoanResponse;
import com.gmao.itm.library_api.service.LoanService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Master-detail loan transactions and book returns")
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "List all loans", description = "Returns all loans with their nested loan details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<LoanResponse>> findAll() {
        return ResponseEntity.ok(loanService.findAll());
    }

    @Operation(summary = "Get loan by ID", description = "Returns a single loan and its line items by loan ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> findById(
            @Parameter(description = "Loan ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(loanService.findById(id));
    }

    @Operation(
            summary = "Create a master-detail loan",
            description = "Creates a loan header and nested loan details in a single transaction, validating member status and book availability."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan created with cascade details"),
            @ApiResponse(responseCode = "400", description = "Invalid dates, inactive member, or insufficient copies"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Member or book not found")
    })
    @PostMapping
    public ResponseEntity<LoanResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "memberId": 1,
                                              "loanDate": "2026-11-22",
                                              "dueDate": "2026-11-29",
                                              "details": [
                                                {
                                                  "bookId": 1,
                                                  "quantity": 1
                                                },
                                                {
                                                  "bookId": 2,
                                                  "quantity": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoanRequest request
    ) {
        LoanResponse created = loanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Return a loan",
            description = "Marks an active loan as RETURNED, sets the return date, and restores available book copies."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan returned and copies restored"),
            @ApiResponse(responseCode = "400", description = "Loan is already returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(
            @Parameter(description = "Loan ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(loanService.returnLoan(id));
    }

    @Operation(summary = "Delete a loan", description = "Deletes a loan by ID (requires ROLE_ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Loan deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Loan ID", example = "1") @PathVariable Long id
    ) {
        loanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}