package com.team.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false, length = 45)
    @Size(min = 1, max = 45)
    @NotBlank(message = "Wallet name is mandatory!")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_category_id", referencedColumnName="id", nullable = false)
    private WalletCategory walletCategory;

    @Column(length = 1000)
    @Size(max = 1000)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy="wallet", cascade = CascadeType.ALL)
    private Set<WalletUser> walletUserSet = new HashSet<>();

    public void addWalletUser(WalletUser walletUser) {
        walletUserSet.add(walletUser);
        walletUser.setWallet(this);
    }
}
