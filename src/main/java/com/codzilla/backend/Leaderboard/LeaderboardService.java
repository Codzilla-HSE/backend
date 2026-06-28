package com.codzilla.backend.Leaderboard;

import com.codzilla.backend.Leaderboard.dto.LeaderboardEntryDTO;
import com.codzilla.backend.Leaderboard.dto.LeaderboardResponseDTO;
import com.codzilla.backend.S3.S3Settings;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LeaderboardService {

    private static final int EDGE_SIZE = 20;

    private final UserRepository userRepository;
    private final S3Presigner presigner;
    private final S3Settings s3Settings;

    public LeaderboardService(UserRepository userRepository,
                              S3Presigner presigner,
                              S3Settings s3Settings) {
        this.userRepository = userRepository;
        this.presigner = presigner;
        this.s3Settings = s3Settings;
    }

    public LeaderboardResponseDTO getLeaderboard(String currentEmail) {
        int total = (int) userRepository.count();

        List<User> topUsers =
                userRepository.findAllByOrderByRatingDescIdAsc(PageRequest.of(
                        0,
                        EDGE_SIZE
                ));

        List<User> bottomUsers;
        if (total <= EDGE_SIZE) {
            bottomUsers = List.of();
        } else {
            int bottomCount = Math.min(
                    EDGE_SIZE,
                    total - EDGE_SIZE
            );
            List<User> bottomAsc =
                    userRepository.findAllByOrderByRatingAscIdDesc(PageRequest.of(
                            0,
                            bottomCount
                    ));
            bottomUsers = new ArrayList<>(bottomAsc);
            Collections.reverse(bottomUsers);
        }

        List<LeaderboardEntryDTO> top = mapWithRank(
                topUsers,
                1,
                currentEmail
        );
        int bottomStartRank = total - bottomUsers.size() + 1;
        List<LeaderboardEntryDTO> bottom = mapWithRank(
                bottomUsers,
                bottomStartRank,
                currentEmail
        );

        User me = userRepository.findByEmail(currentEmail).orElse(null);
        LeaderboardEntryDTO currentUser = null;
        if (me != null) {
            long above = userRepository.countByRatingGreaterThan(me.getRating());
            int myRank = (int) above + 1;
            currentUser = new LeaderboardEntryDTO(
                    myRank,
                    me.getNickname(),
                    me.getRating(),
                    avatarUrl(me.getEmail()),
                    true
            );
        }

        return new LeaderboardResponseDTO(
                top,
                bottom,
                currentUser,
                total
        );
    }

    private List<LeaderboardEntryDTO> mapWithRank(List<User> users, int startRank,
                                                  String currentEmail) {
        List<LeaderboardEntryDTO> result = new ArrayList<>(users.size());
        int rank = startRank;
        for (User u : users) {
            result.add(new LeaderboardEntryDTO(
                    rank++,
                    u.getNickname(),
                    u.getRating(),
                    avatarUrl(u.getEmail()),
                    u.getEmail().equals(currentEmail)
            ));
        }
        return result;
    }

    private String avatarUrl(String email) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                                                         .bucket(s3Settings.bucketName())
                                                         .key("icons/" + email)
                                                         .overrideConfiguration(cfg -> cfg.putHeader(
                                                                 "Host",
                                                                 "localhost:9000"
                                                         ))
                                                         .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                                                        .signatureDuration(Duration.ofMinutes(10))
                                                                        .getObjectRequest(objectRequest)
                                                                        .build();
        return presigner.presignGetObject(presignRequest).url().toExternalForm().replaceFirst(
                "minio",
                "localhost"
        );
    }
}