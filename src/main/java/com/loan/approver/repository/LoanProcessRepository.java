package com.loan.approver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.approver.model.LoanProcess;

@Repository
public interface LoanProcessRepository extends JpaRepository<LoanProcess, Integer> {
  Optional<List<LoanProcess>> findBySsnNumberOrderByApplicationDateTimeDesc(String ssnNumber);

  void deleteBySsnNumber(String ssnNumber);
}
