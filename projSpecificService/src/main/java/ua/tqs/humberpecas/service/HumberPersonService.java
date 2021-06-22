package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

@Service
@Log4j2
public class HumberPersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public Person register(PersonDTO user) throws DuplicatedObjectException {

        if (personRepository.findByEmail(user.getEmail()).isEmpty()) {
            Person p = new Person(user.getName(), bcryptEncoder.encode(user.getPwd()), user.getEmail());

            log.info("HUMBER PERSON SERVICE: Successfully registered a new person");
            personRepository.saveAndFlush(p);
            return p;
        }

        log.error("HUMBER PERSON SERVICE: A person with that email is already registered");
        throw new DuplicatedObjectException("User email is already in use.");
    }

}
