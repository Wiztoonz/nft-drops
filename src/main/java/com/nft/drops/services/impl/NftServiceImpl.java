package com.nft.drops.services.impl;

import com.nft.drops.dto.Nft;
import com.nft.drops.services.NftService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NftServiceImpl implements NftService {

    private final RestTemplate rest;

    @Autowired
    public NftServiceImpl(RestTemplate rest) {
        this.rest = rest;
    }

    @Override
    public List<Nft> scrapNFT() {
        String url = "https://nftcalendar.io";
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "text/html");
        headers.set("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36");
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchangeNfts = rest.exchange(url + "/events/newest/", HttpMethod.GET, httpEntity, String.class);
        String nftsHtml = exchangeNfts.getBody();

        Document parseNftsHtml = Jsoup.parse(nftsHtml);
        Elements blockElements = parseNftsHtml.select("div[class~=^$] > div.flex > div.flex > div.w-full");
        List<Nft> nftList = blockElements.parallelStream().map(blockElement -> {
            Nft nft = null;
            if (blockElement.select("div.w-full > a > img").hasAttr("src")) {
                String nftDetails = blockElement.select("div.w-full > a").attr("href");
                String detailsUrl = url + nftDetails;
                ResponseEntity<String> exchangeNftDetails = rest.exchange(detailsUrl, HttpMethod.GET, httpEntity, String.class);
                String nftDetailsHtml = exchangeNftDetails.getBody();
                Document parseDetailsHtml = Jsoup.parse(nftDetailsHtml);

                Elements details = Jsoup.parse(parseDetailsHtml.select("div.container").last().html())
                        .select("div.w-full > div.p-6");

                String name = details.select("h1").text();
                String imgUrl = details.select("img").attr("src");
                String date = details.select("div > div.text-lg").text();
                Map<String, LocalDate> dateSchedule = getDate(date);
                LocalDate start = dateSchedule.get("start");
                LocalDate end = dateSchedule.get("end");
                Map<String, String> siteMap = new HashMap<>();

                for (Element linkElement : details.select("div > div.flex > a")) {
                    String siteName = linkElement.select("span").text();
                    String siteHref = linkElement.attr("href").trim();
                    siteMap.put(siteName, siteHref);
                }
                String platformInfo = details.select("div > div.flex > div.w-full > div").text().trim();
                String platforms[] = Arrays.stream(platformInfo.split("\s+")).map(e -> {
                    switch (e) {
                        case "Creator(s):", "Marketplace:", "Blockchain:" -> e = "\n" + e;
                    }
                    return e;
                }).collect(Collectors.joining(" ")).split("\n");

                String creator = "";
                String marketplace = "";
                String blockchain = "";
                for (String platformInf : platforms) {
                    if (platformInf.startsWith("Creator(s):")) {
                        creator = platformInf.trim();
                    } else if (platformInf.startsWith("Marketplace:")) {
                        marketplace = platformInf.trim();
                    } else if (platformInf.startsWith("Blockchain:")) {
                        blockchain = platformInf.trim();
                    }
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                Elements paragraphs = details.select("div.content > p");
                for (Element paragraph : paragraphs) {
                    for (Attribute attribute : paragraph.attributes()) {
                        paragraph.removeAttr(attribute.getKey());
                    }
                    Elements a = paragraph.select("a");
                    for (Element element : a) {
                        element.removeAttr("rel");
                        for (Attribute attribute : element.attributes()) {
                            String key = attribute.getKey();
                            if (!key.equals("href")) {
                                element.removeAttr(key);
                            }
                        }
                    }
                    Elements span = paragraph.select("span");
                    for (Element element : span) {
                        for (Attribute attribute : element.attributes()) {
                            element.removeAttr(attribute.getKey());
                        }
                    }
//                    String lineSeparator = System.lineSeparator();
//                    descriptionBuilder.append(paragraph.outerHtml()).append(lineSeparator);
                    descriptionBuilder.append(paragraph.outerHtml());
                }
                String description = String.valueOf(descriptionBuilder).trim();

                Elements tagsBlock = details.select("div.mt-8");
                Elements tagsAbbr = tagsBlock.select("div.mt-8 > span");
                Elements tagNames = tagsBlock.select("div.mt-8 > a");
                StringBuilder tagsBuilder = new StringBuilder();
                tagsBuilder.append(tagsAbbr.text()).append(" ");
                for (Element tagName : tagNames) {
                    tagsBuilder.append(tagName.text()).append(" ");
                }
                String tags = String.valueOf(tagsBuilder).trim();

                nft = new Nft(name, imgUrl, start, end, siteMap, creator, marketplace, blockchain, description, tags);
            }
            return nft;
        }).filter(Objects::nonNull).toList();
        return nftList;
    }

    private Map<String, LocalDate> getDate(String date) {
        String[] dates = date.split("\\–");
        String start = dates[0].trim();
        String end = dates[1].trim();

        String[] startDetails = start.split(",");
        String[] startMonthAndDate = startDetails[0].split("\s+");
        String startDate = startMonthAndDate[1].trim();
        String startYear = startDetails[1].trim();
        String startMonth = startMonthAndDate[0].substring(0, 3).trim();

        String[] endDetails = end.split(",");
        String[] endMothAndDate = endDetails[0].split("\s+");
        String endDate = endMothAndDate[1].trim();
        String endYear = endDetails[1].trim();
        String endMonth = endMothAndDate[0].substring(0, 3).trim();

        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MMM-yyyy")
                .toFormatter(Locale.ENGLISH);

        String startData = String.format("%s-%s-%s", startDate, startMonth, startYear);
        String endData = String.format("%s-%s-%s", endDate, endMonth, endYear);

        LocalDate startDataInfo = LocalDate.parse(startData, dateTimeFormatter);
        LocalDate endDataInfo = LocalDate.parse(endData, dateTimeFormatter);

        Map<String, LocalDate> dateScheduleMap = new HashMap<>();
        dateScheduleMap.put("start", startDataInfo);
        dateScheduleMap.put("end", endDataInfo);
        return dateScheduleMap;
    }

}
