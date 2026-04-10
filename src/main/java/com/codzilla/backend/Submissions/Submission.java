package com.codzilla.backend.Submissions;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue
    @UuidGenerator
    UUID id;
    UUID userId;

    @Transient
    byte[] content;
    SubmissionLanguage language;
    SubmissionStatus status;
}