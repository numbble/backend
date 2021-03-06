package numble.team4.shortformserver.likevideo.application;

import lombok.RequiredArgsConstructor;
import numble.team4.shortformserver.likevideo.domain.LikeVideo;
import numble.team4.shortformserver.likevideo.domain.LikeVideoRepository;
import numble.team4.shortformserver.likevideo.exception.AlreadyExistLikeVideoException;
import numble.team4.shortformserver.likevideo.exception.NotExistLikeVideoException;
import numble.team4.shortformserver.likevideo.exception.NotMemberOfLikeVideoException;
import numble.team4.shortformserver.likevideo.ui.dto.LikeVideoExistResponse;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.video.domain.Video;
import numble.team4.shortformserver.video.domain.VideoRepository;
import numble.team4.shortformserver.video.exception.NotExistVideoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeVideoService {

    private final LikeVideoRepository likeVideoRepository;
    private final VideoRepository videoRepository;

    public LikeVideoExistResponse existLikeVideo(Member member, Long videoId) {
        Optional<Long> existLikeVideoInfo = likeVideoRepository.findIdByMemberAndVideoId(member, videoId);
        return LikeVideoExistResponse.from(existLikeVideoInfo);
    }

    @Transactional
    public void saveLikeVideo(Member member, Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(NotExistVideoException::new);

        if (likeVideoRepository.existsByMember_IdAndVideo_Id(member.getId(), videoId)) {
            throw new AlreadyExistLikeVideoException();
        }

        LikeVideo likeVideo = LikeVideo.fromMemberAndVideo(member, video);
        likeVideoRepository.save(likeVideo);
        likeVideo.like();
    }

    @Transactional
    public void deleteLikeVideo(Member member, Long likesId) {
        LikeVideo likeVideo = likeVideoRepository.findById(likesId)
                .orElseThrow(NotExistLikeVideoException::new);

        if (!likeVideo.isMemberOf(member)) {
            throw new NotMemberOfLikeVideoException();
        }
        likeVideoRepository.delete(likeVideo);
        likeVideo.unlike();
    }

}
