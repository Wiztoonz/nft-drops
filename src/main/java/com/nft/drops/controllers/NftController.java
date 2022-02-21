package com.nft.drops.controllers;

import com.nft.drops.dto.Nft;
import com.nft.drops.services.NftHistoryService;
import com.nft.drops.services.NftService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/nfts")
public class NftController {

    private final NftService nftService;
    private final NftHistoryService nftHistoryService;

    public NftController(NftService nftService, NftHistoryService nftHistoryService) {
        this.nftService = nftService;
        this.nftHistoryService = nftHistoryService;
    }

    @GetMapping("/get")
    public List<Nft> getAllNFTs() {
        return nftService.scrapNFT();
    }

    @GetMapping("/last")
    public List<Nft> getLastNFTs() {
        return nftHistoryService.findLastNfts();
    }

}
