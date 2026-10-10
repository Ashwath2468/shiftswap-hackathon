
package com.project.HackthonStarter.repository;

import com.project.HackthonStarter.model.ActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionHistoryRepository
        extends JpaRepository<ActionHistory, String> {

    List<ActionHistory> findAllByOrderByTimestampDesc();
}
