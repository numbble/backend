package numble.team4.shortformserver.video.infrastructure;

import numble.team4.shortformserver.likevideo.domain.LikeVideo;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.Role;
import numble.team4.shortformserver.testCommon.BaseDataJpaTest;
import numble.team4.shortformserver.video.domain.Video;
import numble.team4.shortformserver.video.domain.VideoRepository;
import numble.team4.shortformserver.video.dto.VideoListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BaseDataJpaTest
class VideoCustomRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Member member;

    @BeforeEach
    void init() {
        member = Member.builder().name("from").role(Role.MEMBER).build();
        testEntityManager.persist(member);
        createVideo();
    }

    void createVideo() {
        for (int i = 0; i < 8; i++) {
            Video build = Video.builder()
                    .member(member)
                    .videoUrl("videoUrl")
                    .thumbnailUrl("thumnailUrl")
                    .description("테스트 비디오")
                    .title("테스트 비디오")
                    .build();
            videoRepository.save(build);
        }
    }

    void createLikeVideo() {
        List<Video> all = videoRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            LikeVideo likeVideo = LikeVideo.fromMemberAndVideo(member, all.get(i));
            testEntityManager.persist(likeVideo);
        }
    }

    @Test
    @DisplayName("[성공] 사용자의 동영상 목록을 조회 (videoId가 null일 때)")
    void findAllByMemberAndMaxVideoId_returnListHasSizeLimitNum_success() {

        //when
        List<Video> videos = videoRepository.findAllByMemberAndMaxVideoId(member, null, 5);

        assertThat(videoRepository.count()).isEqualTo(8);
        //then
        assertThat(videos).hasSize(5);
        for (int i = 0; i < 4; i++) {
            assertTrue(videos.get(i).getId() > videos.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("[성공] 사용자의 동영상 목록을 조회 (videoId가 null이 아닐 때)")
    void findAllByMemberAndMaxVideoId_returnListHasLeftNum_success() {
        List<Video> all = videoRepository.findAll()
                .stream()
                .sorted((a, b) -> (a.getId() > b.getId()) ? 1 : -1)
                .collect(Collectors.toList());

        Long id = all.get(3).getId();

        List<Video> videos = videoRepository.findAllByMemberAndMaxVideoId(member, id, 5);

        assertThat(videos).hasSize(3);
        for (int i = 0; i < videos.size() - 1; i++) {
            assertTrue(videos.get(i).getId() > videos.get(i + 1).getId());
        }
    }

    static Stream<Long> valueSources() {
        return Stream.of(null, 3L, 5L, 9L);
    }

    @ParameterizedTest
    @DisplayName("[성공] 사용자가 좋아요한 동영상 목록을 조회")
    @MethodSource("valueSources")
    void findAllLikeVideoByMemberAndMaxVideoId_returnListHasSizeLimitNum_success(Long intVal) {
        //given
        createLikeVideo();
        long count = 5;
        if (intVal != null) {
            count = videoRepository.findAll().stream().map(x -> x.getId())
                    .filter(x -> x < intVal)
                    .count();
        }

        //when
        List<Video> res = videoRepository.findAllLikeVideoByMemberAndMaxVideoId(member, intVal, 5);

        //then
        assertThat(res).hasSize((int) count);
        for (int i = 0; i < res.size() - 1; i++) {
            assertTrue(res.get(i).getId() > res.get(i + 1).getId());
        }
    }

}