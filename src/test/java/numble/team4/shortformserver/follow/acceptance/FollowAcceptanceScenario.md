## 팔로우 시나리오

### 취소

* 초기 설정 : user1, user2를 생성

### 성공

1. 본인이 아닌 다른 사용자를 팔로우한 상태로 팔로우 취소 요청
    * given  
        user1, user2를 생성한다.  
        user1 -> user2 팔로우를 생성한다.
    * when  
        user1은 user2 대해 팔로우 취소를 요청한다.
    * then  
        팔로우가 삭제되었다는 메시지와 함께 성공.
    
### 실패

1. 존재하지 않는 사용자에 대해 팔로우 취소 요청
    * given  
        user1, user2를 생성한다.
    * when  
        user1은 user2에 대해 팔로우 취소를 요청한다.
    * then  
        존재하지 않는 사용자라는 메시지와 함께 에러가 발생한다.

2. 팔로우하지 않는 사용자에 대해 팔로우 취소 요청
    * given  
        user1, user2를 생성한다.
    * when  
        "user1"은 "user2"에 대한 팔로우 취소를 요청한다.
    * then  
        "user1"은 "user2"를 팔로우하고 있지 않기 때문에, 팔로우하지 않은 사용자라는 메시지와 함께 에러가 발생한다.