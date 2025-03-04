package com.raul.forumhub.topic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.dto.request.AnswerDTO;
import com.raul.forumhub.topic.dto.response.AnswerResponseDTO;
import com.raul.forumhub.topic.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.topic.security.TopicSecurityConfig;
import com.raul.forumhub.topic.service.AnswerService;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
@ActiveProfiles(value = "test")
@ContextConfiguration(classes = {AnswerController.class,
        TopicSecurityConfig.class, GlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnswerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AnswerService answerService;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    private static final Jwt JWT;

    static {
        JWT = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_id", "1")
                .build();
    }


    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final AnswerDTO answerDTO = new AnswerDTO("Resposta teste");

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/ans", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final AnswerDTO answerDTO = new AnswerDTO("Resposta teste");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerDTO)))
                .andExpect(status().isBadRequest());

    }

    @DisplayName("Should fail with status code 401 when answer topic if user unauthenticated")
    @Test
    void shouldFailToAnswerTopicIfUnauthenticated() throws Exception {
        final AnswerDTO answerDTO = new AnswerDTO("Resposta teste");

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerDTO)))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.answerService);

    }


    @DisplayName("Should answer topic with success if user is authenticated")
    @Test
    void shouldAnswerTopicWithSuccessIfAuthenticated() throws Exception {
        final AnswerDTO answerDTO = new AnswerDTO("Resposta teste");

        BDDMockito.doNothing().when(this.answerService).answerTopic(1L, 1L, answerDTO);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerDTO)))
                .andExpect(status().isCreated());

        BDDMockito.verify(this.answerService).answerTopic(1L, 1L, answerDTO);
        BDDMockito.verifyNoMoreInteractions(this.answerService);


    }


    @DisplayName("Should fail with status code 401 when mark answer best if user unauthenticated")
    @Test
    void shouldFailToMarkAnswerBestIfUnauthenticated() throws Exception {
        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/markBestAnswer", 1)
                        .queryParam("answer_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.answerService);

    }


    @DisplayName("Should fail with status code 400 when attempt mark answer best if " +
            "answer_id property of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenMarkAnswerBest() throws Exception {
        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/markBestAnswer", 1)
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.answerService);

    }

    @DisplayName("Should fail with status code 400 when mark best answer with" +
            " param different of type number")
    @Test
    void shouldFailToRequestTopicIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/markBestAnswer", 1)
                        .queryParam("answer_id", "unexpected")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

    }


    @DisplayName("Should mark answer best with success if user is authenticated")
    @Test
    void shouldMarkAnswerBestWithSuccessIfAuthenticated() throws Exception {
        BDDMockito.doNothing().when(this.answerService).markBestAnswer(1L, 1L, 1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/markBestAnswer", 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.answerService).markBestAnswer(1L, 1L, 1L);
        BDDMockito.verifyNoMoreInteractions(this.answerService);


    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:edit'" +
            "when edit answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditAnswer() throws Exception {
        final AnswerDTO answerUpdateDTO =
                new AnswerDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/edit", 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.answerService);
    }

    @DisplayName("Should fail with status code 400 when edit answer with" +
            " param different of type number")
    @Test
    void shouldFailToEditAnswerIfParamDifferentOfTypeNumber() throws Exception {
        final AnswerDTO answerUpdateDTO =
                new AnswerDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/edit", 1)
                        .queryParam("answer_id", "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isBadRequest());
    }


    @DisplayName("Should fail with status code 400 when attempt update answer if answer_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenUpdateAnswer() throws Exception {
        final AnswerDTO answerUpdateDTO =
                new AnswerDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/edit", 1)
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.answerService);

    }


    @DisplayName("Should edit answer with success if user authenticated has authority 'answer:edit'")
    @Test
    void shouldEditAnswerWithSuccessIfUserHasSuitableAuthority() throws Exception {
        final AnswerDTO answerUpdateDTO =
                new AnswerDTO("Primeiro teste de edição de uma resposta");

        Answer answer = TestsHelper.AnswerHelper.answerList().get(0);
        answer.setSolution("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.answerService.updateAnswer(1L, 1L, 2L, answerUpdateDTO))
                .willReturn(new AnswerResponseDTO(answer));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/edit", 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer.solution",
                        is("Primeiro teste de edição de uma resposta")));

        BDDMockito.verify(this.answerService).updateAnswer(1L, 1L, 2L, answerUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.answerService);

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:delete'" +
            " when delete answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteAnswer() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/delete", 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.answerService);

    }


    @DisplayName("Should fail with status code 400 when attempt delete answer if answer_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenDeleteAnswer() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/delete", 1)
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.answerService);

    }

    @DisplayName("Should fail with status code 400 when delete answer with" +
            " param different of type number")
    @Test
    void shouldFailToDeleteTopicIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/delete", 1)
                        .queryParam("answer_id", "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }


    @DisplayName("Should delete answer with success if user authenticated has authority 'answer:delete'")
    @Test
    void shouldDeleteAnswerWithSuccessIfUserHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.answerService).deleteAnswer(1L, 1L, 2L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/delete", 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.answerService).deleteAnswer(1L, 1L, 2L);
        BDDMockito.verifyNoMoreInteractions(this.answerService);

    }


}
