package com.example.user_service.model;

import com.example.user_service.util.DoctorRank;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "doctors")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Doctor extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "rank", length = 30)
    DoctorRank rank;

    @Column(name = "ava", columnDefinition = "TEXT")
    String ava;
}
