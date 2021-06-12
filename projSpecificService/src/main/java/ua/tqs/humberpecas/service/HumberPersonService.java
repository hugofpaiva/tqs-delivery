package ua.tqs.humberpecas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

@Service
public class HumberPersonService {

    @Autowired
    private PersonRepository repository;

    // TODO: terminar
    public void register(PersonDTO user) throws DuplicatedObjectException {

        Person p  = new Person(user.getName(), user.getPwd(), user.getEmail());
        repository.saveAndFlush(p);

        // validar os dados (verificar se email ja existe na bd)

        // se tudo bem guardar na bd

        // se não lançar excetion
    }

}
