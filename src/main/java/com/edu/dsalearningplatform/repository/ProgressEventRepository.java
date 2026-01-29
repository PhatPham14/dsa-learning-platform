package com.edu.dsalearningplatform.repository;

import com.edu.dsalearningplatform.entity.ProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressEventRepository extends JpaRepository<ProgressEvent, Long> {
}
