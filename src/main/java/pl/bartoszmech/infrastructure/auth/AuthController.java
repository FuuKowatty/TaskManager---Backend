package pl.bartoszmech.infrastructure.auth;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartoszmech.domain.user.UserFacade;
import pl.bartoszmech.domain.user.dto.CreateAndUpdateUserRequestDto;
import pl.bartoszmech.domain.user.dto.UserDto;
import pl.bartoszmech.infrastructure.auth.dto.JwtResponseDto;
import pl.bartoszmech.infrastructure.auth.dto.TokenRequestDto;
import pl.bartoszmech.infrastructure.auth.dto.TokenResponseDto;
import pl.bartoszmech.infrastructure.security.jwt.JwtAuthenticatorFacade;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static pl.bartoszmech.domain.user.UserRoles.ADMIN;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AuthController {
    private final JwtAuthenticatorFacade jwtAuthenticatorFacade;
    private final UserFacade userFacade;
    PasswordEncoder passwordEncoder;
    @PostMapping("/token")
    public ResponseEntity<TokenResponseDto> authenticateAndGenerateToken(@Valid@RequestBody TokenRequestDto tokenRequestDto) {
        final JwtResponseDto jwtDto = jwtAuthenticatorFacade.authenticateAndGenerateToken(tokenRequestDto);
        String email = jwtDto.username();
        return ResponseEntity.status(OK).body(TokenResponseDto.builder()
                .token(jwtDto.token())
                .email(email)
                .id(userFacade.findByEmail(email).id())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerAdmin(@Valid @RequestBody CreateAndUpdateUserRequestDto user) {
        return ResponseEntity.status(CREATED).body(userFacade.registerAdmin(CreateAndUpdateUserRequestDto.builder()
                .firstName(user.firstName())
                .lastName(user.lastName())
                .email(user.email())
                .password(passwordEncoder.encode(user.password()))
                .role(ADMIN)
                .build()));
    }
}
