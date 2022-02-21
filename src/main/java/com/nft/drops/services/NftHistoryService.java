package com.nft.drops.services;

import com.nft.drops.dto.Nft;
import com.nft.drops.models.NftHistory;

import java.util.List;

public interface NftHistoryService {

    void save(List<NftHistory> nftHistory);

    List<Nft> findLastNfts();

}
