package com.gzeeday.domain.review;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Review> findAllByUserOrderByCreatedAtDesc(User user);
    Optional<Review> findByConfirmedPlan(ConfirmedPlan confirmedPlan);
    boolean existsByConfirmedPlan(ConfirmedPlan confirmedPlan);
} 