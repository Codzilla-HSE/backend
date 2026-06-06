//package com.codzilla.backend;
//
//import com.codzilla.backend.User.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.MinIOContainer;
//import org.testcontainers.containers.PostgreSQLContainer;
//import tools.jackson.databind.ObjectMapper;
//
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class BaseIntegrationTest {
//    @Autowired
//    protected MockMvc mockMvc;
//
//    @Autowired
//    protected ObjectMapper objectMapper;
//
//    @Autowired
//    protected UserRepository userRepository;
//
//
//    @ServiceConnection
//    static PostgreSQLContainer<?> postgres
//            = new PostgreSQLContainer<>("postgres:16-alpine");
//
//
//    static MinIOContainer minio = new MinIOContainer("minio/minio:latest")
//            .withUserName("test-user")
//            .withPassword("test-password");
//
//    @DynamicPropertySource
//    static void overrideProps(DynamicPropertyRegistry registry) {
//        registry.add(
//                "app.s3.endpoint",
//                minio::getS3URL
//        );
//        registry.add(
//                "app.s3.access-key",
//                minio::getUserName
//        );
//        registry.add(
//                "app.s3.secret-key",
//                minio::getPassword
//        );
//    }
//
//    static {
//        postgres.start();
//        minio.start();
//    }
//}
