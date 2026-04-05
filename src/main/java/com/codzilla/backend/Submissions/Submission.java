package com.codzilla.backend.Submissions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Submission {
    @Id
    UUID id;
    UUID userId;

    @Transient
    byte[] content;
    SubmissionLanguage language;
    SubmissionStatus status;
}