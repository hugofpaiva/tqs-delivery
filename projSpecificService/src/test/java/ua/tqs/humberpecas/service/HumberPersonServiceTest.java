package ua.tqs.humberpecas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HumberPersonServiceTest {
    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    @InjectMocks
    private HumberPersonService personService;

    private PersonDTO personDTO;
    private Person person;

    @BeforeEach
    void setUp() throws IOException {
        person = new Person("Fernando", "12345678","fernando@ua.pt");
        personDTO = new PersonDTO("Fernando", "12345678","fernando@ua.pt");
    }

    @Test
    @DisplayName("Register: email already in use")
    public void testRegister_whenEmailAlreadyInUse_thenDuplicateObjException() {
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(this.person));

        assertThrows( DuplicatedObjectException.class, () -> {
            personService.register(personDTO);
        } );

        verify(personRepository, times(0)).saveAndFlush(any());
    }

    @Test
    @DisplayName("Register: everything is ok, then return")
    public void testRegister_whenEverythingIsOK_thenReturn() throws DuplicatedObjectException {
        when(personRepository.findByEmail(personDTO.getEmail())).thenReturn(Optional.empty());
        when(bcryptEncoder.encode(anyString())).thenReturn("passwordencrypted");

        Person response = personService.register(personDTO);

        assertThat(response.getEmail(), equalTo(person.getEmail()));
        assertThat(response.getName(), equalTo(person.getName()));

        verify(personRepository, times(1)).saveAndFlush(any());
    }

}
