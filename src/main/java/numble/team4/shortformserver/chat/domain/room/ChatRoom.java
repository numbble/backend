package numble.team4.shortformserver.chat.domain.room;

import lombok.Getter;
import lombok.NoArgsConstructor;
import numble.team4.shortformserver.common.domain.BaseTimeEntity;
import numble.team4.shortformserver.member.member.domain.Member;
import numble.team4.shortformserver.member.member.exception.NotAuthorException;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Member seller;

    private ChatRoom(Member buyer, Member seller) {
        this.buyer = buyer;
        this.seller = seller;
    }

    public void validateAuthorization(Member member) {
        if (!(buyer.equals(member) || seller.equals(member))) {
            throw new NotAuthorException();
        }
    }

    public static ChatRoom of(Member buyer, Member seller) {
        return new ChatRoom(buyer, seller);
    }
}