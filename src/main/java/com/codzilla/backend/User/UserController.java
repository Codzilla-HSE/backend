package com.codzilla.backend.User;


import com.codzilla.backend.Authentication.Exceptions.UserNotFoundException;
import com.codzilla.backend.S3.S3Settings;
import com.codzilla.backend.User.DTO.ChangeUserRequestDTO;
import com.codzilla.backend.User.DTO.UserInfoResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    S3Client s3Client;

    @Autowired
    S3Settings s3Settings;

    @Autowired
    S3Presigner presigner;

    @Autowired
    UserService userService;

    @GetMapping("/info")
    ResponseEntity<?> getUserInfo(@AuthenticationPrincipal User user) {
        var responseUser = userService.getByEmail(user.getEmail());

        UserInfoResponseDTO info = new UserInfoResponseDTO(
                responseUser.getNickname(),
                responseUser.getEmail(),
                responseUser.getRating()
        );
        return ResponseEntity.ok(info);
    }

    @GetMapping("/icon-url")
    public String getAvatarUrl(@AuthenticationPrincipal User user) {

        return createPresignedGetUrl(s3Settings.bucketName(), "icons/" + user.getEmail());
    }

    @PostMapping("/upload-icon")
    public ResponseEntity<?> uploadIcon(@AuthenticationPrincipal User user, @RequestParam("file") MultipartFile file) {
        try {

            String fileName = "icons/" + user.getEmail();

            s3Client.putObject(PutObjectRequest.builder()
                            .key(fileName)
                            .bucket(s3Settings.bucketName())
                            .contentType(file.getContentType())
                                               .build(),
                    RequestBody.fromBytes(file.getBytes())); // todo: make service
            return ResponseEntity.ok("Ok");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при чтении файла");
        }
    }

    private String createPresignedGetUrl(String bucketName, String keyName) {


        GetObjectRequest objectRequest = GetObjectRequest.builder()
                                                         .bucket(bucketName)
                                                         .key(keyName)
                                                         .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                                                        .signatureDuration(Duration.ofMinutes(10))
                                                                        .getObjectRequest(objectRequest)
                                                                        .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        log.info("Presigned URL: [{}]", presignedRequest.url().toString());
        log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();

    }

    @PostMapping("change-user")
    public ResponseEntity<?> changeUser(
            @AuthenticationPrincipal User user,
            @org.springframework.web.bind.annotation.RequestBody ChangeUserRequestDTO dto
    ) {
        userService.updateUser(user.getEmail(), dto);
        return ResponseEntity.ok("Ok");
    }
}
