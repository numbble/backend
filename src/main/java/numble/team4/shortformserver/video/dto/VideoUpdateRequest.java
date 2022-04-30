package numble.team4.shortformserver.video.dto;

import static lombok.AccessLevel.PROTECTED;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import numble.team4.shortformserver.video.domain.Video;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class VideoUpdateRequest {

    private String category;
    private String title;
    private String description;

    public Video toVideo() {
        return Video.builder()
            .title(title)
            .description(description)
            .build();
    }
}