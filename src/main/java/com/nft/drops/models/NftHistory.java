package com.nft.drops.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "nft_history", schema = "nft")
@NamedQuery(name = "NftHistory.findLastNfts",
        query = "SELECT n FROM NftHistory n ORDER BY n.id DESC")
public class NftHistory implements Comparable<NftHistory> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "name")
    private String name;

    public NftHistory(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(NftHistory o) {
        return this.name.compareTo(o.getName());
    }
}
