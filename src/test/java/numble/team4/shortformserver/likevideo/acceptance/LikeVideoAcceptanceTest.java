package numble.team4.shortformserver.likevideo.acceptance;


import static numble.team4.shortformserver.common.exception.ExceptionType.ALREADY_EXIST_LIKE_VIDEO;
import static numble.team4.shortformserver.common.exception.ExceptionType.NOT_EXIST_LIKE_VIDEO;
import static numble.team4.shortformserver.common.exception.ExceptionType.NOT_EXIST_VIDEO;
import static numble.team4.shortformserver.common.exception.ExceptionType.NOT_MEMBER_OF_LIKE_VIDEO;
import static numble.team4.shortformserver.likevideo.ui.LikeVideoResponseMessage.DELETE_LIKE_VIDEO;
import static numble.team4.shortformserver.likevideo.ui.LikeVideoResponseMessage.SAVE_LIKE_VIDEO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.persistence.EntityManager;
import numble.team4.shortformserver.likevideo.domain.LikeVideo;
import numble.team4.shortformserver.likevideo.domain.LikeVideoRepository;
import numble.team4.shortformserver.member.auth.domain.MemberAdapter;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.Role;
import numble.team4.shortformserver.testCommon.BaseAcceptanceTest;
import numble.team4.shortformserver.testCommon.mockUser.WithMockCustomUser;
import numble.team4.shortformserver.video.category.domain.Category;
import numble.team4.shortformserver.video.category.domain.CategoryRepository;
import numble.team4.shortformserver.video.category.exception.NotFoundCategoryException;
import numble.team4.shortformserver.video.domain.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;

public class LikeVideoAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LikeVideoRepository likeVideoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Video video;
    private Member member;

    @BeforeEach
    void init() {
        Category category = categoryRepository.findByName("??????").orElseThrow(
            NotFoundCategoryException::new);

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
            .category(category)
            .usedStatus(true)
            .price(99999)
            .likeCount(0L)
            .viewCount(0L)
            .build();
        entityManager.persist(video);
    }

    @Nested
    @WithMockCustomUser
    @DisplayName("????????? ????????? ?????? ?????? ?????? ?????????")
    class GetExistLikeVideoTest {

        @Test
        @DisplayName("[??????] ??????x")
        void existLikeVideo_false_isok_success() throws Exception {
            //when
            ResultActions res = mockMvc.perform(get("/v1/videos/{videoId}/likes", 1098902L));

            //then
            res.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.exist_like_video").value(false));
        }

        @Test
        @DisplayName("[??????] ??????")
        void existLikeVideo_true_isok_success() throws Exception {
            //given
            Member member = ((MemberAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMember();
            LikeVideo likeVideo = LikeVideo.fromMemberAndVideo(member, video);
            likeVideoRepository.save(likeVideo);

            //when
            ResultActions res = mockMvc.perform(get("/v1/videos/{videoId}/likes", video.getId()));

            //then
            res.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.exist_like_video").value(true))
                    .andExpect(jsonPath("$.data.likes_id").value(likeVideo.getId()));
        }
    }

    @Nested
    @WithMockCustomUser
    @DisplayName("????????? ????????? ?????? ?????????")
    class SaveLikeVideoTest {

        @Test
        @DisplayName("[??????] ???????????? ????????? ?????? ???????????? ????????? ?????? ??????")
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
        @DisplayName("[??????] ???????????? ?????? ???????????? ????????? ?????? ??????")
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
        @DisplayName("[??????] ?????? ???????????? ?????? ???????????? ????????? ?????? ??????")
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
    @DisplayName("????????? ????????? ?????? ?????????")
    class DeleteLikeVideoTest {

        @Test
        @DisplayName("[??????] ????????? ????????? ????????? ?????? ??????")
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
        @DisplayName("[??????] ???????????? ?????? ????????? ?????? ??????")
        void deleteLikeVideo_notExistLikeVideoException_fail() throws Exception {
            //when
            ResultActions res = mockMvc.perform(delete("/v1/videos/likes/{likesId}", 3924802L));

            //then
            res.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(NOT_EXIST_LIKE_VIDEO.getMessage()));
        }

        @Test
        @DisplayName("[??????] ????????? ???????????? ?????? ????????? ?????? ??????")
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
