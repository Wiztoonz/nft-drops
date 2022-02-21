package com.nft.drops.services.impl;

import com.nft.drops.dao.NftHistoryRepository;
import com.nft.drops.dto.Nft;
import com.nft.drops.models.NftHistory;
import com.nft.drops.services.NftHistoryService;
import com.nft.drops.services.NftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NftHistoryServiceImpl implements NftHistoryService {

    private final NftHistoryRepository nftHistoryRepository;
    private final NftService nftService;

    @Autowired
    public NftHistoryServiceImpl(NftHistoryRepository nftHistoryRepository, NftService nftService) {
        this.nftHistoryRepository = nftHistoryRepository;
        this.nftService = nftService;
    }

    @Override
    public void save(List<NftHistory> nftHistory) {
        nftHistoryRepository.saveAll(nftHistory);
    }

    @Override
    public List<Nft> findLastNfts() {
        List<Nft> nftsScrap = nftService.scrapNFT();
        List<NftHistory> nfts = nftsScrap.stream()
                .map(e -> new NftHistory(e.name()))
                .toList();

        List<NftHistory> lastNfts = nftHistoryRepository.findLastNfts(PageRequest.of(0, nfts.size()));
        List<NftHistory> nftsList = new ArrayList<>(nfts);

        nftsList.removeIf(nft -> lastNfts
                .stream()
                .anyMatch(lastNft -> nft.getName().equals(lastNft.getName())));

        if (!nftsList.isEmpty()) {
            save(nftsList);
        }

        List<Nft> nftList = new ArrayList<>(nftsScrap);
        nftList.removeIf(nft -> lastNfts
                .stream()
                .anyMatch(lastNft -> nft.name().equals(lastNft.getName())));

        return nftList;
    }

}
