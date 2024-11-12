package com.raul.forumhub.topic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "hub")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    @JsonIgnore
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "profile")
    private ProfileName profileName;

    public Profile(ProfileName profileName){
        this.profileName = profileName;
    }


    public enum ProfileName {

        ADM,
        MOD,
        BASIC
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", profileName=" + profileName +
                '}';
    }
}