package com.gmao.itm.library_api.service;

import com.gmao.itm.library_api.dto.LoanDetailRequest;
import com.gmao.itm.library_api.dto.LoanRequest;
import com.gmao.itm.library_api.dto.LoanResponse;
import com.gmao.itm.library_api.exception.BusinessRuleException;
import com.gmao.itm.library_api.exception.ResourceNotFoundException;
import com.gmao.itm.library_api.mapper.LoanMapper;
import com.gmao.itm.library_api.model.Book;
import com.gmao.itm.library_api.model.Loan;
import com.gmao.itm.library_api.model.LoanDetail;
import com.gmao.itm.library_api.model.LoanStatus;
import com.gmao.itm.library_api.model.Member;
import com.gmao.itm.library_api.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final LoanMapper loanMapper;

    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    public LoanResponse findById(Long id) {
        return loanMapper.toResponse(getLoanOrThrow(id));
    }

    @Transactional
    public LoanResponse create(LoanRequest request) {
        if (request.dueDate().isBefore(request.loanDate())) {
            throw new BusinessRuleException("Due date cannot be earlier than loan date");
        }

        Member member = memberService.getMemberOrThrow(request.memberId());
        if (Boolean.FALSE.equals(member.getActive())) {
            throw new BusinessRuleException("Member is not active and cannot borrow books");
        }

        long loanDays = Math.max(1L, ChronoUnit.DAYS.between(request.loanDate(), request.dueDate()));

        Loan loan = Loan.builder()
                .member(member)
                .loanDate(request.loanDate())
                .dueDate(request.dueDate())
                .status(LoanStatus.ACTIVE)
                .estimatedFee(BigDecimal.ZERO)
                .build();

        BigDecimal totalEstimatedFee = BigDecimal.ZERO;

        for (LoanDetailRequest detailReq : request.details()) {
            Book book = bookService.getBookOrThrow(detailReq.bookId());

            if (book.getAvailableCopies() < detailReq.quantity()) {
                throw new BusinessRuleException(
                        "Insufficient available copies for book '" + book.getTitle() +
                                "' (requested: " + detailReq.quantity() + ", available: " + book.getAvailableCopies() + ")"
                );
            }

            book.setAvailableCopies(book.getAvailableCopies() - detailReq.quantity());

            BigDecimal subtotal = book.getLateFeePerDay()
                    .multiply(BigDecimal.valueOf(detailReq.quantity()))
                    .multiply(BigDecimal.valueOf(loanDays));

            LoanDetail detail = LoanDetail.builder()
                    .book(book)
                    .quantity(detailReq.quantity())
                    .subtotalFee(subtotal)
                    .build();

            loan.addDetail(detail);
            totalEstimatedFee = totalEstimatedFee.add(subtotal);
        }

        loan.setEstimatedFee(totalEstimatedFee);
        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toResponse(savedLoan);
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoanOrThrow(id);
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("Loan is already marked as RETURNED");
        }

        for (LoanDetail detail : loan.getDetails()) {
            Book book = detail.getBook();
            int restoredCopies = Math.min(book.getTotalCopies(), book.getAvailableCopies() + detail.getQuantity());
            book.setAvailableCopies(restoredCopies);
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Loan updated = loanRepository.save(loan);
        return loanMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Loan loan = getLoanOrThrow(id);
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            for (LoanDetail detail : loan.getDetails()) {
                Book book = detail.getBook();
                int restoredCopies = Math.min(book.getTotalCopies(), book.getAvailableCopies() + detail.getQuantity());
                book.setAvailableCopies(restoredCopies);
            }
        }
        loanRepository.delete(loan);
    }

    public Loan getLoanOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }
}