package com.example.user_service.model;

import com.example.user_service.util.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient extends User {

    @Column(name = "address", length = 500)
    String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    Gender gender;
}
