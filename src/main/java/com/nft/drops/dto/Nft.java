package com.nft.drops.dto;


import java.time.LocalDate;
import java.util.Map;

public record Nft(String name,
                  String imgUrl,
                  LocalDate start,
                  LocalDate end,
                  Map<String, String> links,
                  String creator,
                  String marketplace,
                  String blockchain,
                  String description,
                  String tags) {
}
