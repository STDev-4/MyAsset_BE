package io.api.myasset.domain.approval.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long> {

	List<CardApproval> findByConnectedIdAndApprovalDateBetween(
		String connectedId,
		String startDate,
		String endDate);
}
