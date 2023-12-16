package pl.bartoszmech.infrastructure.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartoszmech.domain.accountidentifier.AccountIdentifierFacade;
import pl.bartoszmech.domain.accountidentifier.dto.CreateUserRequestDto;
import pl.bartoszmech.domain.accountidentifier.dto.UserDto;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    AccountIdentifierFacade accountIdentifierFacade;
    @GetMapping
    public ResponseEntity<List<UserDto>> list() {
        return ResponseEntity.status(OK).body(accountIdentifierFacade.listUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable("id") long id) {
        return ResponseEntity.status(OK).body(accountIdentifierFacade.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody CreateUserRequestDto requestDto) {
        return ResponseEntity.status(CREATED).body(accountIdentifierFacade.createUser(requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteById(@PathVariable("id") long id) {
        return ResponseEntity.status(OK).body(accountIdentifierFacade.deleteById(id));    }
}
