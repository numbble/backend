package numble.team4.shortformserver.member.member.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import numble.team4.shortformserver.common.domain.BaseTimeEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue
    private Long id;
    private String email;
    private String name;
    private LocalDateTime lastLoginDate;
    private String profileImageUrl;
    private boolean emailVerified;
}