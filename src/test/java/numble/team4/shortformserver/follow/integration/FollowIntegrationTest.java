package numble.team4.shortformserver.follow.integration;

import numble.team4.shortformserver.follow.domain.Follow;
import numble.team4.shortformserver.follow.domain.FollowRepository;
import numble.team4.shortformserver.follow.exception.AlreadyExistFollowException;
import numble.team4.shortformserver.follow.exception.NotExistFollowException;
import numble.team4.shortformserver.follow.exception.NotFollowingException;
import numble.team4.shortformserver.follow.exception.NotSelfFollowableException;
import numble.team4.shortformserver.follow.ui.FollowController;
import numble.team4.shortformserver.follow.ui.dto.FollowExistResponse;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.MemberRepository;
import numble.team4.shortformserver.member.member.domain.Role;
import numble.team4.shortformserver.member.member.exception.NotExistMemberException;
import numble.team4.shortformserver.testCommon.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.*;

@BaseIntegrationTest
public class FollowIntegrationTest {

    @Autowired
    private FollowController followController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private EntityManager entityManager;

    private Member fromMember;
    private Member toMember;

    @BeforeEach
    void init() {
        fromMember = Member.builder().name("from").role(Role.MEMBER).build();
        toMember = Member.builder().name("to").role(Role.MEMBER).build();

        memberRepository.saveAll(Arrays.asList(fromMember, toMember));
    }
    
    @Nested
    @DisplayName("?????? ????????? ????????? ?????? ?????? ?????????")
    class ExistFollowTest {

        @Test
        @DisplayName("[??????] ??????????????? ?????? ???")
        void existFollow_returnValueIsTrueAndHasId_success() {
            //given
            Follow follow = Follow.fromMembers(fromMember, toMember);
            followRepository.save(follow);
            entityManager.flush();
            entityManager.clear();

            //when
            FollowExistResponse existFollowInfo = followController.existFollow(fromMember, toMember.getId()).getData();

            //then
            assertTrue(existFollowInfo.isExistFollow());
            assertThat(existFollowInfo.getFollowId()).isEqualTo(follow.getId());
        }


        @Test
        @DisplayName("[??????] ??????????????? ?????? ?????? ???")
        void existFollow_returnValueIsFalse_success() {
            //when
            FollowExistResponse existFollowInfo = followController.existFollow(fromMember, 2309489023L).getData();

            //then
            assertFalse(existFollowInfo.isExistFollow());
            assertThat(existFollowInfo.getFollowId()).isNull();
        }
    }

    @Nested
    @DisplayName("????????? ?????? ?????????")
    class SaveFollowTest {

        @Test
        @DisplayName("[??????] ????????? ????????? ?????? ?????? ???????????? ????????? ??????")
        void createFollow_followRepositoryFindAllHasSizeOne_success() {
            //when
            followController.createFollow(fromMember, toMember.getId());

            //then
            assertThat(followRepository.count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[??????] ????????? ?????????????????? ??????")
        void createFollow_notSelfFollowableException_fail() {
            //when, then
            assertThrows(
                    NotSelfFollowableException.class,
                    () -> followController.createFollow(fromMember, fromMember.getId())
            );
        }

        @Test
        @DisplayName("[??????] ?????? ???????????? ???????????? ?????? ????????? ??????")
        void createFollow_alreadyExistFollowException_fail () {
            //given
            Follow follow = Follow.fromMembers(fromMember, toMember);
            followRepository.save(follow);

            //when, then
            assertThrows(
                    AlreadyExistFollowException.class,
                    () -> followController.createFollow(fromMember, toMember.getId())
            );
        }
    }

    @Nested
    @DisplayName("????????? ?????? ?????????")
    class DeleteFollowTest {

        @Test
        @DisplayName("[??????] ?????? ?????? ?????? ???????????? ????????? ??? ???????????? ?????? ??????")
        void deleteFollow_isok_success() {
            //given
            Follow follow = followRepository.save(Follow.fromMembers(fromMember, toMember));

            //when
            followController.deleteFollow(fromMember , follow.getId());

            //then
            assertThat(followRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("[??????] ???????????? ?????? ???????????? ?????? ?????? ??????")
        void deleteFollow_notExistMemberException_fail() {
            //when, then
            assertThrows(
                    NotExistFollowException.class,
                    () -> followController.deleteFollow(fromMember, 9999L)
            );
        }

        @Test
        @DisplayName("[??????] ?????? ????????? ????????? ???????????? ?????? ?????? ??????")
        void deleteFollow_notFollowingException_fail() {
            //given
            Follow follow = followRepository.save(Follow.fromMembers(fromMember, toMember));

            //when, then
            assertThrows(
                    NotFollowingException.class,
                    () -> followController.deleteFollow(toMember, follow.getId())
            );
        }
    }

    @Nested
    @DisplayName("????????? ?????? ?????? ?????????")
    class GetFollowsTest {

        @Test
        @DisplayName("[??????] ?????????(?????? ???????????????) ?????? ??????")
        void getAllFollowings_getListSizeOne_success() {
            //given
            Follow follow = followRepository.save(Follow.fromMembers(fromMember, toMember));

            //when
            followController.getAllFollowings(fromMember.getId());

            //then
            assertThat(followRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("[??????] ?????????(?????? ???????????????) ?????? ??????")
        void getAllFollowers_getListSizeOne_success() {
            //given
            followRepository.save(Follow.fromMembers(fromMember, toMember));

            //when
            followController.getAllFollowers(toMember.getId());

            //then
            assertThat(followRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("[??????] ???????????? ?????? ???????????? ????????? ?????? ??????")
        void getAllFollowings_notExistMemberException_fail() {
            //when, then
            assertThrows(
                    NotExistMemberException.class,
                    () -> followController.getAllFollowings(13L)
            );
        }

        @Test
        @DisplayName("[??????] ???????????? ?????? ???????????? ????????? ?????? ??????")
        void getAllFollowerss_notExistMemberException_fail() {
            //when, then
            assertThrows(
                    NotExistMemberException.class,
                    () -> followController.getAllFollowers(13L)
            );
        }
    }
}
