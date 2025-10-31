package ru.netology.cloud;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.netology.cloud.controller.AuthController;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.dto.LoginRequest;
import ru.netology.cloud.service.TokenService;
import ru.netology.cloud.service.UserService;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {
    @Test
    void loginOk() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        TokenService tokenService = Mockito.mock(TokenService.class);

        User u = new User();
        u.setLogin("user");
        Mockito.when(userService.findByLogin("user")).thenReturn(Optional.of(u));
        Mockito.when(userService.matchesPassword("password", null)).thenReturn(true);
        Mockito.when(tokenService.issueToken(u)).thenReturn("token");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, tokenService)).build();
        mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }
}


