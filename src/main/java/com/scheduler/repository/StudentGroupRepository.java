package com.scheduler.repository;

import com.scheduler.domain.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByCreatedByUsername(String username);
    List<StudentGroup> findByMemberUsernamesContaining(String username);
}
