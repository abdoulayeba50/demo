package demo.services;

import demo.entity.Client;
import demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return findEntity(id);
    }

    @Transactional
    public Client create(Client client) {
        if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé : " + client.getEmail());
        }
        client.setId(null);
        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, Client client) {
        Client existing = findEntity(id);
        clientRepository.findByEmail(client.getEmail())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé : " + client.getEmail());
                });
        existing.setFirstname(client.getFirstname());
        existing.setLastname(client.getLastname());
        existing.setEmail(client.getEmail());
        return clientRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.delete(findEntity(id));
    }

    private Client findEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable : " + id));
    }
}
