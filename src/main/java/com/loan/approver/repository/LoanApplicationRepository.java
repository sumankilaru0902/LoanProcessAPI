package com.loan.approver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.approver.model.LoanApplication;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {
  Optional<List<LoanApplication>> findBySsnNumberOrderByApplicationDateTimeDesc(String ssnNumber);

  void deleteBySsnNumber(String ssnNumber);
}
