package ma.plxm.webflux.demo.services;

import lombok.extern.log4j.Log4j2;
import ma.plxm.webflux.demo.entities.Profile;
import ma.plxm.webflux.demo.repositories.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@DataMongoTest
@Log4j2
@Import(ProfileService.class)
class ProfileServiceTest {

    private final ProfileService service;
    private final ProfileRepository repository;

    public ProfileServiceTest(
        @Autowired
            ProfileService service,
        @Autowired
            ProfileRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Test
    void allTest() {
        Flux<Profile> saved = repository.saveAll(
            Flux.just(new Profile(null, "Ema"), new Profile(null, "John"),
                new Profile(null, "Konan")));
        Flux<Profile> composite = service.all().thenMany(saved);
        Predicate<Profile> match = profile -> saved
            .any(saveItem -> saveItem.equals(profile)).block();
        StepVerifier.create(composite).expectNextMatches(match)
            .expectNextMatches(match).expectNextMatches(match).verifyComplete();
    }

    @Test
    void getTest() {
        String test = "testEmail";
        Mono<Profile> inserted = this.service.create(test)
            .flatMap(saved -> this.service.get(saved.getId()));
        StepVerifier.create(inserted).expectNextMatches(
            profile -> StringUtils.hasText(profile.getId()) && test
                .equalsIgnoreCase(profile.getEmail())).verifyComplete();
    }

    @Test
    void updateTest() {
        Mono<Profile> saved = this.service.create("test")
            .flatMap(p -> this.service.update(p.getId(), "test1"));
        StepVerifier.create(saved)
            .expectNextMatches(p -> p.getEmail().equalsIgnoreCase("test1"))
            .verifyComplete();
    }

    @Test
    void deleteTest() {
        String test = "test";
        Mono<Profile> deleted = this.service.create(test)
            .flatMap(saved -> this.service.delete(saved.getId()));
        StepVerifier.create(deleted).expectNextMatches(
            profile -> profile.getEmail().equalsIgnoreCase(test))
            .verifyComplete();
    }

    @Test
    void createTest() {
        Mono<Profile> profileMono = this.service.create("email@email.com");
        StepVerifier.create(profileMono)
            .expectNextMatches(saved -> StringUtils.hasText(saved.getId()))
            .verifyComplete();
    }
}