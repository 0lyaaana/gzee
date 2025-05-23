package com.gzeeday.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 기본 시간 엔티티
 * - 모든 엔티티에서 공통으로 사용되는 생성일시와 수정일시 필드를 정의합니다.
 * - JPA Auditing 기능을 사용하여 자동으로 시간 정보가 기록됩니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;    // 생성일시 (수정 불가)

    @LastModifiedDate
    private LocalDateTime updatedAt;    // 수정일시
} 