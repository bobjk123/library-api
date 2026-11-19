package com.gmao.itm.library_api.repository;

import com.gmao.itm.library_api.model.Loan;
import com.gmao.itm.library_api.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByStatus(LoanStatus status);
}