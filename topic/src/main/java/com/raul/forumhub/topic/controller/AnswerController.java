package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.request.AnswerDTO;
import com.raul.forumhub.topic.dto.response.AnswerResponseDTO;
import com.raul.forumhub.topic.dto.response.HttpMessage;
import com.raul.forumhub.topic.security.IsAuthenticated;
import com.raul.forumhub.topic.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forumhub.io/api/v1/topics")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @IsAuthenticated
    @PostMapping("/{topic_id}/answer")
    public ResponseEntity<HttpMessage> answerTopic(@PathVariable Long topic_id, @Valid @RequestBody AnswerDTO answerDTO,
                                                   @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.answerTopic(topic_id, user_id, answerDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @IsAuthenticated
    @PostMapping("/{topic_id}/markBestAnswer")
    public ResponseEntity<HttpMessage> markBestAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                      @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.markBestAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));
    }

    @PreAuthorize("hasAuthority('SCOPE_answer:edit')")
    @PutMapping("/{topic_id}/answers/edit")
    public ResponseEntity<AnswerResponseDTO> updateAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                          @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AnswerDTO answerDTO) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        AnswerResponseDTO answerResponseDTO = this.answerService.updateAnswer(topic_id, answer_id, user_id, answerDTO);

        return ResponseEntity.ok(answerResponseDTO);
    }

    @PreAuthorize("hasAuthority('SCOPE_answer:delete')")
    @DeleteMapping("/{topic_id}/answers/delete")
    public ResponseEntity<HttpMessage> deleteAnswer(@PathVariable Long topic_id, @RequestParam Long answer_id,
                                                    @AuthenticationPrincipal Jwt jwt) {

        Long user_id = Long.parseLong(jwt.getClaim("user_id"));
        this.answerService.deleteAnswer(topic_id, answer_id, user_id);

        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));
    }

}
