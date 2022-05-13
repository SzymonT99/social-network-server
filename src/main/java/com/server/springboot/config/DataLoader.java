package com.server.springboot.config;

import com.server.springboot.domain.entity.Interest;
import com.server.springboot.domain.entity.Role;
import com.server.springboot.domain.enumeration.AppRole;
import com.server.springboot.domain.repository.InterestRepository;
import com.server.springboot.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final InterestRepository interestRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository, InterestRepository interestRepository) {
        this.roleRepository = roleRepository;
        this.interestRepository = interestRepository;
    }

    public void run(ApplicationArguments args) {
        if (!roleRepository.existsByName(AppRole.ROLE_USER) && !roleRepository.existsByName(AppRole.ROLE_ADMIN)) {
            roleRepository.save(new Role(1, AppRole.ROLE_USER));
            roleRepository.save(new Role(2, AppRole.ROLE_ADMIN));
        }

        if (interestRepository.findAll().size() == 0) {
            interestRepository.save(new Interest("Programowanie"));
            interestRepository.save(new Interest("Testowanie oprogramowania"));
            interestRepository.save(new Interest("Sztuczna inteligencja"));
            interestRepository.save(new Interest("Piłka nożna"));
            interestRepository.save(new Interest("Siatkówka"));
            interestRepository.save(new Interest("Koszykówka"));
            interestRepository.save(new Interest("Tenis"));
            interestRepository.save(new Interest("Polityka"));
            interestRepository.save(new Interest("Geografia"));
            interestRepository.save(new Interest("Historia"));
            interestRepository.save(new Interest("Astronomia"));
            interestRepository.save(new Interest("Gry komputerowe"));
            interestRepository.save(new Interest("Gotowanie"));
            interestRepository.save(new Interest("Filmy science fiction"));
            interestRepository.save(new Interest("Filmy obyczajowe"));
            interestRepository.save(new Interest("Filmy historyczne"));
            interestRepository.save(new Interest("Filmy komediowe"));
            interestRepository.save(new Interest("Muzyka popularna"));
            interestRepository.save(new Interest("Muzyka klasyczna"));
            interestRepository.save(new Interest("Muzyka elektroniczna"));
            interestRepository.save(new Interest("Koty"));
            interestRepository.save(new Interest("Psy"));
            interestRepository.save(new Interest("Język Java"));
            interestRepository.save(new Interest("Tworzenie stron internetowych"));
        }
    }
}