package com.gzeeday.domain.review;

import com.gzeeday.domain.plan.ConfirmedPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Review> findAllByAuthorNameOrderByCreatedAtDesc(String authorName);
    Optional<Review> findByConfirmedPlan(ConfirmedPlan confirmedPlan);
    boolean existsByConfirmedPlan(ConfirmedPlan confirmedPlan);
} 