package com.backend.api.forumhub.controller;

import com.backend.api.forumhub.dto.request.CreateUserDTO;
import com.backend.api.forumhub.dto.response.HttpMessage;
import com.backend.api.forumhub.dto.request.UpdateUser;
import com.backend.api.forumhub.dto.response.UserResponse;
import com.backend.api.forumhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-forum/v1/forumhub/users")
public class UserController {

    private final UserService userService;

    private UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/create/")
    private ResponseEntity<HttpMessage> createUser(@Valid @RequestBody CreateUserDTO createUserDTO){

        this.userService.createUser(createUserDTO);
        return new ResponseEntity<>(new HttpMessage("HttpStatusCode OK"), HttpStatus.CREATED);
    }

    @GetMapping("/{user_id}/")
    private ResponseEntity<UserResponse> getInfoUser(@PathVariable Long user_id) {

        UserResponse userResponse = new UserResponse(this.userService.getInfoUser(user_id));
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/")
    private PagedModel<EntityModel<UserResponse>> getAllUser(@PageableDefault Pageable pageable,
                                                             PagedResourcesAssembler<UserResponse> assembler){

        Page<UserResponse> userResponse = userService.getAllUser(pageable);
        return assembler.toModel(userResponse);
    }

    @PutMapping("/update_info/{user_id}/")
    private ResponseEntity<UserResponse> updateUser(@PathVariable Long user_id, @Valid @RequestBody UpdateUser updateUser) {

        UserResponse userResponse = this.userService.updateUser(user_id, updateUser);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/delete/{user_id}/")
    private ResponseEntity<HttpMessage> deleteUser(@PathVariable Long user_id){

        this.userService.deleteUser(user_id);
        return ResponseEntity.ok(new HttpMessage("HttpStatusCode OK"));
    }

}
