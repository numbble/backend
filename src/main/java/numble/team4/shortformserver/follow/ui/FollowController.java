package numble.team4.shortformserver.follow.ui;

import lombok.RequiredArgsConstructor;
import numble.team4.shortformserver.common.dto.CommonResponse;
import numble.team4.shortformserver.follow.application.FollowService;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.domain.MemberRepository;
import numble.team4.shortformserver.member.member.exception.NotExistMemberException;
import org.springframework.web.bind.annotation.*;

import static numble.team4.shortformserver.follow.ui.FollowResponseMessage.CREATE_FOLLOW;
import static numble.team4.shortformserver.follow.ui.FollowResponseMessage.DELETE_FOLLOW;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/following")
public class FollowController {

    private final MemberRepository memberRepository;
    private final FollowService followService;

    @GetMapping("/{toUserId}")
    public CommonResponse createFollow(@RequestParam("from_member") Long fromMemberId, @PathVariable("toUserId") Long toMemberId) {
        Member fromMember = memberRepository.findById(fromMemberId).orElseThrow(NotExistMemberException::new);
        followService.createFollow(fromMember, toMemberId);
        return CommonResponse.from(CREATE_FOLLOW.getMessage());
    }

    @DeleteMapping("/{followId}")
    public CommonResponse deleteFollow(@RequestParam("from_member") Long fromMemberId, @PathVariable("followId") Long followId) {
        Member fromMember = memberRepository.findById(fromMemberId).orElseThrow(NotExistMemberException::new);
        followService.deleteFollow(fromMember, followId);
        return CommonResponse.from(DELETE_FOLLOW.getMessage());
    }
}
