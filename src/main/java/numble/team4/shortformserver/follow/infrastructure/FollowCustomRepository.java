package numble.team4.shortformserver.follow.infrastructure;

import numble.team4.shortformserver.follow.ui.dto.FollowResponse;

import java.util.List;

public interface FollowCustomRepository {

    List<FollowResponse> getFollowersByMemberId(Long id);

    List<FollowResponse> getFollowingsByMemberId(Long id);
}
