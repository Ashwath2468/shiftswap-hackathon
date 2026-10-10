
package com.project.HackthonStarter.repository;

import com.project.HackthonStarter.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, String> {

    List<Listing> findByStatus(String status);

    List<Listing> findByShiftId(String shiftId);
}
