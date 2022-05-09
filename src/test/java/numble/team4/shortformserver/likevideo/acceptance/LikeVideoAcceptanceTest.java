package numble.team4.shortformserver.likevideo.acceptance;


import numble.team4.shortformserver.likevideo.domain.LikeVideo;
import numble.team4.shortformserver.likevideo.domain.LikeVideoRepository;
import numble.team4.shortformserver.likevideo.ui.LikeVideoResponseMessage;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.Role;
import numble.team4.shortformserver.testCommon.BaseAcceptanceTest;
import numble.team4.shortformserver.testCommon.mockUser.WithMockCustomUser;
import numble.team4.shortformserver.video.domain.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityManager;

import static numble.team4.shortformserver.common.exception.ExceptionType.*;
import static numble.team4.shortformserver.likevideo.ui.LikeVideoResponseMessage.DELETE_LIKE_VIDEO;
import static numble.team4.shortformserver.likevideo.ui.LikeVideoResponseMessage.SAVE_LIKE_VIDEO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LikeVideoAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LikeVideoRepository likeVideoRepository;

    private Video video;
    private Member member;

    @BeforeEach
    void init() {
        member = Member.builder()
                .role(Role.MEMBER)
                .emailVerified(true)
                .build();
        entityManager.persist(member);

        video = Video.builder()
                .member(member)
                .videoUrl("http://videourl.com")
                .thumbnailUrl("http://url.com")
                .title("title")
                .description("description")
                .build();
        entityManager.persist(video);
    }

    @Nested
    @Rollback(false)
    @WithMockCustomUser
    @DisplayName("동영상 좋아요 등록 테스트")
    class SaveLikeVideoTest {

        @Test
        @DisplayName("[성공] 좋아요를 누르지 않은 동영상에 좋아요 등록 요청")
        void saveLikeVideo_isok_success() throws Exception {
            //when
            ResultActions res = mockMvc.perform(
                    post("/v1/videos/{videoId}/likes", video.getId())
            );

            //then
            res.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(SAVE_LIKE_VIDEO.getMessage()))
                    .andDo(print());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 동영상에 좋아요 등록 요창")
        void saveLikeVideo_notExistLikeVideoException_fail() throws Exception {
            //when
            ResultActions res = mockMvc.perform(
                    post("/v1/videos/{videoId}/likes", 39842109L)
            );

            //then
            res.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(NOT_EXIST_VIDEO.getMessage()));
        }

        @Test
        @DisplayName("[실패] 이미 좋아요를 누른 동영상에 좋아요 등록 요청")
        void saveLikeVideo_alreadyExistLikeVideoExceiption_fail() throws Exception {
            //given
            mockMvc.perform(
                    post("/v1/videos/{videoId}/likes", video.getId())
            );

            //when
            ResultActions res = mockMvc.perform(
                    post("/v1/videos/{videoId}/likes", video.getId())
            );

            //then
            res.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ALREADY_EXIST_LIKE_VIDEO.getMessage()));
        }
    }

    @Nested
    @WithMockCustomUser
    @DisplayName("동영상 좋아요 삭제 테스트")
    class DeleteLikeVideoTest {

        @Test
        @DisplayName("[성공] 본인이 생성한 좋아요 삭제 요청")
        void deleteLikeVideo_isok_success() throws Exception {
            //given
            mockMvc.perform(post("/v1/videos/{videoId}/likes", video.getId()));
            LikeVideo likeVideo = likeVideoRepository.findAll().get(0);

            //when
            ResultActions res = mockMvc.perform(delete("/v1/videos/likes/{likesId}", likeVideo.getId()));

            //then
            res.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(DELETE_LIKE_VIDEO.getMessage()));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 좋아요 삭제 요청")
        void deleteLikeVideo_notExistLikeVideoException_fail() throws Exception {
            //when
            ResultActions res = mockMvc.perform(delete("/v1/videos/likes/{likesId}", 3924802L));

            //then
            res.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(NOT_EXIST_LIKE_VIDEO.getMessage()));
        }

        @Test
        @DisplayName("[실패] 본인이 생성하지 않은 좋아요 삭제 요청")
        void deleteLikeVideo_notMemberOfLikeVideoException_fail() throws Exception {
            //given
            LikeVideo likeVideo = LikeVideo.fromMemberAndVideo(member, video);
            likeVideoRepository.save(likeVideo);
            //when
            ResultActions res = mockMvc.perform(delete("/v1/videos/likes/{likesId}", likeVideo.getId()));

            //then
            res.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(NOT_MEMBER_OF_LIKE_VIDEO.getMessage()));
        }
    }
}
