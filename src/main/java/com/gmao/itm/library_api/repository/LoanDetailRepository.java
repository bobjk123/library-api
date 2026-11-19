package com.gmao.itm.library_api.repository;

import com.gmao.itm.library_api.model.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {

    List<LoanDetail> findByLoanId(Long loanId);

    List<LoanDetail> findByBookId(Long bookId);
}