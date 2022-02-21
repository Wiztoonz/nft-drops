package com.nft.drops.dao;

import com.nft.drops.models.NftHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NftHistoryRepository extends JpaRepository<NftHistory, Long> {

    List<NftHistory> findLastNfts(Pageable pageable);

}
