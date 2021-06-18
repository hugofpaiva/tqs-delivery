package ua.tqs.humberpecas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

@Service
public class HumberPersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public Person register(PersonDTO user) throws DuplicatedObjectException {

        if (personRepository.findByEmail(user.getEmail()).isEmpty()) {
            Person p = new Person(user.getName(), bcryptEncoder.encode(user.getPwd()), user.getEmail());
            personRepository.saveAndFlush(p);
            return p;
        }

        throw new DuplicatedObjectException("User email is already in use.");
    }

}
