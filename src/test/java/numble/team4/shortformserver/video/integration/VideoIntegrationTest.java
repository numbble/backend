package numble.team4.shortformserver.video.integration;


import static numble.team4.shortformserver.member.member.domain.Role.ADMIN;
import static numble.team4.shortformserver.member.member.domain.Role.MEMBER;
import static numble.team4.shortformserver.video.ui.VideoResponseMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import numble.team4.shortformserver.aws.application.AmazonS3Uploader;
import numble.team4.shortformserver.common.dto.CommonResponse;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.MemberRepository;
import numble.team4.shortformserver.member.member.exception.NoAccessPermissionException;
import numble.team4.shortformserver.testCommon.BaseIntegrationTest;
import numble.team4.shortformserver.video.category.domain.Category;
import numble.team4.shortformserver.video.category.domain.CategoryRepository;
import numble.team4.shortformserver.video.category.exception.NotFoundCategoryException;
import numble.team4.shortformserver.video.domain.Video;
import numble.team4.shortformserver.video.domain.VideoRepository;
import numble.team4.shortformserver.video.dto.VideosResponse;
import numble.team4.shortformserver.video.dto.VideoRequest;
import numble.team4.shortformserver.video.dto.VideoResponse;
import numble.team4.shortformserver.video.dto.VideoUpdateRequest;
import numble.team4.shortformserver.video.exception.NotExistVideoException;
import numble.team4.shortformserver.video.ui.VideoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

@BaseIntegrationTest
public class VideoIntegrationTest {

    @Autowired
    private AmazonS3Uploader amazonS3Uploader;

    @Autowired
    private VideoController videoController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member author;
    private Member tester;
    private Video video;
    private Category category;
    private VideoUpdateRequest videoUpdateRequest;

    @BeforeEach
    void setUp() {
        category = categoryRepository.findByName("??????")
            .orElseThrow(NotFoundCategoryException::new);
        List<Member> members = List.of(
            createMember("author"),
            createMember("tester")
        );
        author = members.get(0);
        tester = members.get(1);

        memberRepository.saveAll(members);

        List<Video> videos = List.of(
            createVideo(10L, "????????????", "?????? ????????????"),
            createVideo(5L, "?????????", "?????? ????????? ??????"),
            createVideo(5L, "?????????", "?????? ????????? ???????????? ?????????, ????????? ??????"),
            createVideo(3L, "?????? 992", "?????? ???????????? 992 ??????"),
            createVideo(3L, "????????????", "?????? ????????? ????????????95 ??????"),
            createVideo(1L, "?????????", "?????? ??????")
        );

        videoRepository.saveAll(videos);

        video = videos.get(0);

        videoUpdateRequest = VideoUpdateRequest.builder()
            .title(video.getTitle())
            .description("?????? ??????")
            .category("??????")
            .price(100)
            .used_status(false)
            .build();
    }

    @Nested
    @DisplayName("?????? ?????? ?????????")
    class UploadVideoTest {

        MockMultipartFile videoFile;
        MockMultipartFile thumbnailFile;
        VideoRequest videoRequest;

        @Test
        @DisplayName("?????? ?????? ??????")
        void uploadVideo_success() {
            // given
            videoFile = new MockMultipartFile("video", "video".getBytes());
            thumbnailFile = new MockMultipartFile("thumbnail", "thumbnail".getBytes());

            videoRequest = new VideoRequest(videoFile, thumbnailFile, "??????", 100, false, "??????", "");

            // when
            CommonResponse<Long> response = videoController.saveVideo(
                videoRequest, author);

            Video savedVideo = videoRepository.findById(response.getData())
                .orElseThrow(NotExistVideoException::new);

            // then
            assertThat(response.getMessage()).isEqualTo(UPLOAD_VIDEO.getMessage());
            assertThat(savedVideo).isNotNull();

            amazonS3Uploader.deleteToS3(savedVideo.getVideoUrl());
            amazonS3Uploader.deleteToS3(savedVideo.getThumbnailUrl());
        }
    }

    @Nested
    @DisplayName("?????? ???????????? ?????? ?????????")
    class UpdateVideoTest {

        @Test
        @DisplayName("?????? ?????? ??????")
        void updateVideo_success() {
            // when
            CommonResponse<Long> res = videoController.updateVideo(
                videoUpdateRequest, author, video.getId());

            // then
            assertThat(video.getDescription()).isEqualTo(videoUpdateRequest.getDescription());
        }

        @Test
        @DisplayName("?????? ?????? ??????, ???????????? ????????? ????????? ????????? ??? ??????.")
        void updateVideo_notAuthor() {
            // given
            Long videoId = video.getId();

            // when, then
            assertThrows(NoAccessPermissionException.class,
                () -> videoController.updateVideo(videoUpdateRequest, tester,
                    videoId));
        }

        @Test
        @DisplayName("?????? ?????? ??????, ???????????? ?????? ????????? ????????? ??? ??????.")
        void updateVideo_notExistVideo()  {
            // when, then
            assertThrows(NotExistVideoException.class,
                () -> videoController.updateVideo(videoUpdateRequest, author, 918367461L));
        }
    }

    @Nested
    @DisplayName("?????? ?????? ?????????")
    class DeleteVideoTest {

        @Test
        @DisplayName("?????? ?????? ??????")
        void deleteVideo_success() {
            // given
            Long videoId = video.getId();

            // when
            CommonResponse<VideoResponse> res = videoController.deleteVideo(videoId, author);

            // then
            assertThat(res.getMessage()).isEqualTo(DELETE_VIDEO.getMessage());
            assertThat(videoRepository.existsById(videoId)).isFalse();
        }

        @Test
        @DisplayName("?????? ?????? ??????, ???????????? ????????? ????????? ????????? ??? ??????.")
        void deleteVideo_notAuthor()  {
            // given
            Long videoId = video.getId();

            // when, then
            assertThrows(NoAccessPermissionException.class,
                () -> videoController.deleteVideo(videoId, tester));
        }

        @Test
        @DisplayName("?????? ?????? ??????, ???????????? ?????? ????????? ????????? ??? ??????.")
        void deleteVideo_notExistVideo()  {
            assertThrows(NotExistVideoException.class,
                () -> videoController.deleteVideo(1234567890L, author));
        }
    }

    @Nested
    @DisplayName("?????? ?????? ?????????")
    class GetVideoTest {
        @Test
        @DisplayName("?????? ?????? ?????? ??????, ???????????? ?????? ????????? ????????? ??? ??????.")
        void findById_notExistVideo()  {
            assertThrows(NotExistVideoException.class, () -> videoController.findVideoById(100L));
        }

        @Test
        @DisplayName("?????? ?????? ??????")
        void getAllVideo() {
            // given
            int size = videoRepository.findAll().size();

            // when
            List<VideosResponse> all = videoController.getAllVideos().getData();

            // then
            assertThat(all).hasSize(size);
        }
    }

    @Nested
    @DisplayName("????????? ?????? ?????????")
    class AdminPermissionTest {

        @Test
        @DisplayName("???????????? ????????? ????????????.")
        void updateVideoByAdmin() {
            // given
            Member admin = Member.builder()
                .role(ADMIN)
                .email("admin@test.com")
                .build();
            memberRepository.save(admin);
            Long videoId = video.getId();

            // when
            CommonResponse<Long> res = videoController.updateVideo(
                videoUpdateRequest, admin, videoId);

            // then
            assertThat(res.getMessage()).isEqualTo(UPDATE_VIDEO.getMessage());
        }

        @Test
        @DisplayName("???????????? ????????? ????????????.")
        void deleteVideoByAdmin() {
            // given
            Member admin = Member.builder()
                .role(ADMIN)
                .email("admin@test.com")
                .build();
            memberRepository.save(admin);
            Long videoId = video.getId();

            // when
            CommonResponse<VideoResponse> res = videoController.deleteVideo(videoId, admin);

            // then
            assertThat(res.getMessage()).isEqualTo(DELETE_VIDEO.getMessage());
        }
    }


    private Member createMember(String name) {
        return Member.builder()
            .name(name)
            .role(MEMBER)
            .build();
    }

    private Video createVideo(long viewCount, String title, String description) {
        return Video.builder()
            .title(title)
            .description(description)
            .price(10000)
            .category(category)
            .usedStatus(false)
            .videoUrl("video URL")
            .thumbnailUrl("thumbnail URL")
            .member(author)
            .viewCount(viewCount)
            .build();
    }
}
