package numble.team4.shortformserver.member.member.domain;

import numble.team4.shortformserver.member.auth.domain.OauthProvider;
import numble.team4.shortformserver.member.member.infrastructure.MemberCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUserIdAndProvider(Long userId, OauthProvider provider);
    boolean existsById(Long id);
}
