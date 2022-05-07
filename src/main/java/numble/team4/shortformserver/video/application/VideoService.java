package numble.team4.shortformserver.video.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import numble.team4.shortformserver.aws.application.AmazonS3Uploader;
import numble.team4.shortformserver.aws.dto.S3UploadDto;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.MemberRepository;
import numble.team4.shortformserver.member.member.exception.NotExistMemberException;
import numble.team4.shortformserver.video.domain.Video;
import numble.team4.shortformserver.video.domain.VideoRepository;
import numble.team4.shortformserver.video.dto.VideoRequest;
import numble.team4.shortformserver.video.dto.VideoResponse;
import numble.team4.shortformserver.video.dto.VideoUpdateRequest;
import numble.team4.shortformserver.video.exception.NotExistVideoException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {

    private final VideoRepository videoRepository;
    private final MemberRepository memberRepository;
    private final AmazonS3Uploader amazonS3Uploader;

    @Transactional
    public VideoResponse uploadVideo(VideoRequest videoRequest, Member loggedInMember) {
        S3UploadDto videoDto = amazonS3Uploader.saveToS3(videoRequest.getVideo(), "video");
        S3UploadDto thumbnailDto = amazonS3Uploader.saveToS3(videoRequest.getThumbnail(), "video/thumbnail");

        Video video = videoRequest.toVideo(videoDto.getFileUrl(), thumbnailDto.getFileUrl(), loggedInMember);

        Video saveVideo = videoRepository.save(video);
        return VideoResponse.from(saveVideo);
    }

    @Transactional
    public VideoResponse updateVideo(VideoUpdateRequest videoUpdateRequest, Member loggedInMember, Long videoId) {
        Video findVideo = videoRepository.findById(videoId).orElseThrow(NotExistVideoException::new);

        findVideo.update(videoUpdateRequest.getTitle(), videoUpdateRequest.getDescription(), loggedInMember);
        return VideoResponse.from(findVideo);
    }

    @Transactional
    public void deleteVideo(Long videoId, Member loggedMember) {
        Video findVideo = videoRepository.findById(videoId).orElseThrow(NotExistVideoException::new);

        loggedMember.removeVideo(findVideo);

        amazonS3Uploader.deleteToS3(findVideo.getVideoUrl());
        amazonS3Uploader.deleteToS3(findVideo.getThumbnailUrl());

        videoRepository.delete(findVideo);
    }

    public List<VideoResponse> findAllVideoOfMember(Long memberId, Long videoId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(NotExistMemberException::new);

        List<Video> videos;
        if (Objects.isNull(videoId)) {
            videos = videoRepository.findAllVideosOfMember(member, pageable);
        } else {
            videos = videoRepository.findAllVideosOfMember(videoId, member, pageable);
        }

        return videos.stream()
            .map(VideoResponse::from)
            .collect(Collectors.toList());
    }
}
